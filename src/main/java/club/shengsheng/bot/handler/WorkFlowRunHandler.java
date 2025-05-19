package club.shengsheng.bot.handler;

import club.shengsheng.bot.component.MergeEventLoop;
import club.shengsheng.bot.github.Repository;
import club.shengsheng.bot.github.WorkflowRun;
import club.shengsheng.bot.github.WorkflowRunEvent;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.kohsuke.github.GHEvent;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHWorkflowJob;
import org.kohsuke.github.GHWorkflowRun;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static club.shengsheng.bot.github.GitHubConstant.CI_APPROVE_LABEL_NAME;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
@Component
@Github(GHEvent.WORKFLOW_RUN)
public class WorkFlowRunHandler implements GitHubEventHandler {


    @Autowired
    private GitHub gitHub;

    @Autowired
    private MergeEventLoop mergeEventLoop;

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
            System.out.println(payload);
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
        mergeEventLoop.mergeAndRevertAsync(pr);
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
