package club.shengsheng.bot;

import club.shengsheng.bot.github.PrEvent;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHWorkflowRun;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

import static club.shengsheng.bot.github.GitHubConstant.GITHUB_TOKEN_KEY;

@SpringBootTest
class BotApplicationTests {

    @Test
    void contextLoads(@Autowired GitHub gitHub) throws IOException {
        GHRepository repository = gitHub.getRepository("implement-study/ci-test");
        PagedIterable<GHWorkflowRun> main = repository.queryWorkflowRuns().branch("patch-12").list();
        for (GHWorkflowRun ghWorkflowRun : main) {
            ghWorkflowRun.rerun();
        }
    }

    @Test
    void getPr(@Autowired GitHub gitHub) throws IOException {
        JSONObject jsonObject = JSON.parseObject(this.getClass().getClassLoader().getResourceAsStream("pr.json"));
        Assertions.assertNotNull(jsonObject);
        PrEvent event = jsonObject.toJavaObject(PrEvent.class);
        GHRepository repository = gitHub.getRepositoryById(event.getRepository().getId());
        GHPullRequest pullRequest = repository.getPullRequest(event.getNumber());
        pullRequest.comment(String.format("@%s ,感谢你的提交！ 接下来的流程将由我自动为你处理", pullRequest.getUser().getLogin()));
    }

    @Test
    void getPrFromSha(@Autowired GitHub github) throws Exception {
        GHRepository repository = github.getRepository("implement-study/ci-test");
        repository.queryPullRequests().base("main").head("shengsheng-bot:patch-16").list().forEach(pr -> {
            System.out.println(pr.getNumber());
        });
    }


    @Test
    void getRun(@Autowired GitHub gitHub) throws Exception {
        GHRepository repository = gitHub.getRepository("implement-study/ci-test");
        GHWorkflowRun workflowRun = repository.getWorkflowRun(15061973014L);
        workflowRun.listJobs().forEach(job -> {
            System.out.println(job.getName());
            System.out.println(job.getStatus());
            System.out.println(job.getConclusion());
        });
        System.out.println(1);
    }

    @Test
    void getLastCommit(@Autowired GitHub gitHub) throws Exception {
        GHRepository repository = gitHub.getRepository("implement-study/ci-test");
        for (GHCommit commit : repository.listCommits()) {
            System.out.println(commit.getSHA1());
            return;
        }
    }

    @Test
    void getPrCommit(@Autowired GitHub gitHub) throws Exception {
        GHRepository repository = gitHub.getRepository("implement-study/ci-test");
        System.out.println(repository.getId());
        System.out.println(gitHub.getRepository("spring-projects/spring-boot").getId());
    }


    @Test
    void testCache() {
        Cache<Long, Object> idempotentCache = CacheBuilder.newBuilder()
            .expireAfterWrite(2, TimeUnit.MINUTES)
            .build();
        System.out.println(idempotentCache.asMap().putIfAbsent(1L, new Object()));
    }

    @Test
    void revert(@Autowired GitHub gitHub) throws Exception {
        GHRepository repository = gitHub.getRepository("implement-study/ci-test");
        String lastCommitSha1 = "1ad4b0e6c47bcc73882c99d058a9d8ef0afc7593";
        File dir = new File(repository.getName());
        try (Git git = Git.cloneRepository()
            .setURI(repository.getHttpTransportUrl())
            .setDirectory(dir)
            .call()) {
            Iterable<RevCommit> commits = git.log().add(git.getRepository().resolve("HEAD")).call();
            RevCommit revCommit = StreamSupport.stream(commits.spliterator(), false)
                .filter(c -> c.getName().equals(lastCommitSha1))
                .findFirst()
                .orElseThrow(IllegalAccessError::new);
            git.revert()
                .include(revCommit).call();
            CredentialsProvider provider = new UsernamePasswordCredentialsProvider(System.getenv(GITHUB_TOKEN_KEY), "");
            git.push().setCredentialsProvider(provider).call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    void testGit() throws Exception{
        Git git = Git.cloneRepository()
            .setURI("https://github.com/implement-study/ci-test/")
            .setDirectory(new File("/Users/gongxuanzhang/dev/github/bot/ci-test"))
            .call();
        StoredConfig config = git.getRepository().getConfig();
        System.out.println(1);
    }

}
