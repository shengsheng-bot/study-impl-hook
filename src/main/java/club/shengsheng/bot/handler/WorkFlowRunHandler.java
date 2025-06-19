package club.shengsheng.bot.handler;

import club.shengsheng.bot.component.RepoEventLoop;
import club.shengsheng.bot.github.Repository;
import club.shengsheng.bot.github.WorkflowRun;
import club.shengsheng.bot.github.WorkflowRunEvent;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.kohsuke.github.GHEvent;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHWorkflowJob;
import org.kohsuke.github.GHWorkflowRun;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

import static club.shengsheng.bot.github.GitHubConstant.CI_APPROVE_LABEL_NAME;
import static club.shengsheng.bot.github.GitHubConstant.GITHUB_EMAIL_KEY;
import static club.shengsheng.bot.github.GitHubConstant.GITHUB_TOKEN_KEY;
import static club.shengsheng.bot.github.GitHubConstant.GITHUB_USER_KEY;
import static club.shengsheng.bot.github.GitHubConstant.SUCCESS_LABEL_NAME;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
@Component
@Github(GHEvent.WORKFLOW_RUN)
@Slf4j
public class WorkFlowRunHandler implements GitHubEventHandler {


    @Autowired
    private GitHub gitHub;

    @Autowired
    private RepoEventLoop repoEventLoop;

    private final Object dummy = new Object();

    /**
     * key is bit id {@link this#calculateId} , value is dummy object
     **/
    private final Cache<Long, Object> idempotentCache = CacheBuilder.newBuilder()
        .expireAfterWrite(2, TimeUnit.MINUTES)
        .build();

    @Override
    public void handle(GHEvent ghEvent, String payload) throws Exception {
        payload = URLDecoder.decode(payload, StandardCharsets.UTF_8);
        WorkflowRunEvent event = JSONObject.parseObject(payload, WorkflowRunEvent.class);
        if (!event.getAction().equals("completed")) {
            return;
        }
        GHWorkflowRun.Conclusion conclusion = event.getWorkflowRun().getConclusion();
        if (conclusion.equals(GHWorkflowRun.Conclusion.FAILURE)) {
            handleFailed(event);
            return;
        }
        if (conclusion.equals(GHWorkflowRun.Conclusion.SUCCESS)) {
            handleSuccess(event);
            return;
        }
    }

    private long calculateId(GHPullRequest pullRequest) {
        long repoId = pullRequest.getRepository().getId();
        long prId = pullRequest.getId();
        prId <<= 40;
        return repoId | prId;
    }

    private void handleSuccess(WorkflowRunEvent event) throws Exception {
        GHPullRequest pr = getPrFromWorkflow(event);
        if (pr == null) {
            return;
        }
        if (idempotentCache.asMap().putIfAbsent(calculateId(pr), dummy) != null) {
            return;
        }
        pr.comment(String.format("@%s ,恭喜你的代码通过了CI流程，完成了本次自测", pr.getUser().getLogin()));
        repoEventLoop.execute(pr.getRepository(), () -> {
            try {
                mergeAndRevertAsync(pr);
            } catch (IOException e) {
                try {
                    pr.comment(String.format("@%s ,但是你的PR不能合并，可能是有冲突，你可以sync一下你的分支刷新PR试试", pr.getUser().getLogin()));
                } catch (IOException ex) {
                    log.error("merge error and comment error");
                }
                log.error("merge and revert failed", e);
            }
        });
    }

    public void mergeAndRevertAsync(GHPullRequest pullRequest) throws IOException {
        GHRepository repository = pullRequest.getRepository();
        pullRequest.merge("merge", null, GHPullRequest.MergeMethod.SQUASH);
        pullRequest.addLabels(SUCCESS_LABEL_NAME);
        revert(repository, pullRequest.getMergeCommitSha());
    }

    private void revert(GHRepository repository, String lastCommitSha1) throws IOException {
        File dir = new File(repository.getName());
        log.info("revert {}", lastCommitSha1);
        try (Git git = Git.cloneRepository()
            .setURI(repository.getHttpTransportUrl())
            .setDirectory(dir)
            .call()) {
            StoredConfig config = git.getRepository().getConfig();
            config.setString("user", null, "name", System.getenv(GITHUB_USER_KEY));
            config.setString("user", null, "email", System.getenv(GITHUB_EMAIL_KEY));
            Iterable<RevCommit> commits = git.log().add(git.getRepository().resolve("HEAD")).call();
            RevCommit revCommit = StreamSupport.stream(commits.spliterator(), false)
                .filter(c -> c.getName().equals(lastCommitSha1))
                .findFirst()
                .orElseThrow(IllegalAccessError::new);
            git.revert().include(revCommit).call();
            var provider = new UsernamePasswordCredentialsProvider(System.getenv(GITHUB_TOKEN_KEY), "");
            git.push().setCredentialsProvider(provider).call();
        } catch (Exception e) {
            log.error("revert failed", e);
        } finally {
            MoreFiles.deleteRecursively(dir.toPath(), RecursiveDeleteOption.ALLOW_INSECURE);
        }
    }

    private void handleFailed(WorkflowRunEvent event) throws IOException {
        GHRepository repository = gitHub.getRepositoryById(event.getRepository().getId());
        GHWorkflowRun workflowRun = repository.getWorkflowRun(event.getWorkflowRun().getId());
        for (GHWorkflowJob job : workflowRun.listJobs()) {
            if (!Objects.equals(job.getConclusion(), GHWorkflowRun.Conclusion.FAILURE)) {
                continue;
            }
            if (Objects.equals(CI_APPROVE_LABEL_NAME, job.getName())) {
                // ignore ci approve job
                return;
            }
            String message = String.format("@%s ,你的代码貌似有问题，请修改之后重新提交",
                workflowRun.getHeadRepository().getOwner().getLogin());
            GHPullRequest pr = getPrFromWorkflow(event);
            if (pr == null) {
                return;
            }
            pr.comment(message);
            return;
        }
    }


    private GHPullRequest getPrFromWorkflow(WorkflowRunEvent event) throws IOException {
        GHRepository repository = gitHub.getRepositoryById(event.getRepository().getId());
        WorkflowRun workflowRun = event.getWorkflowRun();
        String headBranch = workflowRun.getHeadBranch();
        Repository prRepository = workflowRun.getHeadRepository();
        String login = prRepository.getOwner().getLogin();
        for (GHPullRequest pr : repository.queryPullRequests().base("main").head(login + ":" + headBranch).list()) {
            return pr;
        }
        return null;
    }


}
