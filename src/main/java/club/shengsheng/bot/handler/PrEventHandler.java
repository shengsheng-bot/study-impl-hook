package club.shengsheng.bot.handler;

import club.shengsheng.bot.github.PrEvent;
import club.shengsheng.bot.github.ShengShengRule;
import com.alibaba.fastjson2.JSONObject;
import org.kohsuke.github.GHEvent;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static club.shengsheng.bot.github.GitHubConstant.CI_APPROVE_LABEL_NAME;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
@Component
@Github(GHEvent.PULL_REQUEST)
public class PrEventHandler implements GitHubEventHandler {

    public static final String OPENED = "opened";
    public static final String SYNCHRONIZE = "synchronize";
    public static final String LABELED = "labeled";

    @Autowired
    private GitHub gitHub;

    @Override
    public void handle(GHEvent ghEvent, String payload) throws Exception {
        payload = URLDecoder.decode(payload, StandardCharsets.UTF_8);
        PrEvent event = JSONObject.parseObject(payload, PrEvent.class);
        GHRepository repository = gitHub.getRepositoryById(event.getRepository().getId());
        GHPullRequest pr = repository.getPullRequest(event.getNumber());
        switch (event.getAction()) {
            case OPENED -> handleOpened(pr);
            case SYNCHRONIZE -> handleSynchronize(pr);
            default -> {
                return;
            }
        }
        try {
            pr.removeLabel(CI_APPROVE_LABEL_NAME);
        } catch (Exception e) {
            // ignore
        }
        List<String> illegalFiles = validateModifyFile(pr);
        processIllegalFiles(pr, illegalFiles);
    }


    private void processIllegalFiles(GHPullRequest pr, List<String> illegalFiles) throws IOException {
        if (illegalFiles.isEmpty()) {
            pr.addLabels(CI_APPROVE_LABEL_NAME);
            pr.comment("初步审核已通过，请等待CI流程");
            return;
        }
        List<String> messageLines = new ArrayList<>();
        messageLines.add(String.format("@%s ,❌ 你提交的代码中包含不符合规范的文件，请检查后重新提交;", pr.getUser().getLogin()));
        messageLines.add("可能是因为：");
        illegalFiles.stream().map(filename -> "- " + filename).forEach(messageLines::add);
        messageLines.add("这些文件不允许被修改");
        pr.comment(String.join("\n", messageLines));
    }

    private void handleOpened(GHPullRequest pr) throws IOException {
        pr.comment(String.format("@%s ,感谢你的提交！ 接下来的流程将由我自动为你处理", pr.getUser().getLogin()));
    }

    private void handleSynchronize(GHPullRequest pr) throws IOException {
        pr.comment(String.format("@%s ,你更新了代码！我将进行初步校验", pr.getUser().getLogin()));
    }

    private List<String> validateModifyFile(GHPullRequest pr) throws IOException {
        ShengShengRule rule = ShengShengRule.from(pr.getRepository());
        List<String> illegalFiles = new ArrayList<>();
        pr.listFiles().forEach(modifyFile -> {
            if (!rule.pass(modifyFile.getFilename())) {
                illegalFiles.add(modifyFile.getFilename());
            }
        });
        return illegalFiles;
    }


}
