package club.shengsheng.bot;

import club.shengsheng.bot.github.PrEvent;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHWorkflowRun;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

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
    void getRun(@Autowired GitHub gitHub) throws Exception{
        GHRepository repository = gitHub.getRepository("implement-study/ci-test");
        GHWorkflowRun workflowRun = repository.getWorkflowRun(15061973014L);
        workflowRun.listJobs().forEach(job->{
            System.out.println(job.getName());
            System.out.println(job.getStatus());
            System.out.println(job.getConclusion());
        });
        System.out.println(1);
    }
}
