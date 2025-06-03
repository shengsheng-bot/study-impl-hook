package club.shengsheng.bot.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHIssueComment;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static club.shengsheng.bot.github.ShengShengRule.SHENG_SHENG_RULE_YML;


/**
 * @author gongxuanzhangmelt@gmail.com
 **/
@Component
@RequiredArgsConstructor
@Slf4j
public class PullRequestScanner {

    private static final long DELAY_MS = 5 * 60 * 1000; // aka 5 min

    private final GitHub gitHub;

    private final Set<GHRepository> hookRepo = new CopyOnWriteArraySet<>();

    private final Gear incrementRepoGear = new Gear(12, this::initRepo);

    @Scheduled(fixedDelay = DELAY_MS)
    public void scan() throws IOException {
        incrementRepoGear.turn();
        hookRepo.forEach(repo -> {
            repo.queryPullRequests().state(GHIssueState.OPEN).list().forEach(this::processNoResponsePr);
        });
    }


    public void initRepo() {
        GHOrganization organization;
        try {
            organization = gitHub.getOrganization("implement-study");
        } catch (IOException e) {
            log.warn("init repo error");
            return;
        }
        Set<GHRepository> currentRepo = new HashSet<>(hookRepo);
        List<String> incrementNames = new ArrayList<>();
        for (GHRepository repository : organization.listRepositories()) {
            try {
                repository.getFileContent(SHENG_SHENG_RULE_YML);
                if (currentRepo.add(repository)) {
                    incrementNames.add(repository.getName());
                }
            } catch (IOException e) {
                // ignore 
            }
        }
        if (!incrementNames.isEmpty()) {
            log.info("increment repo :{}", incrementNames);
            hookRepo.addAll(currentRepo);
        }
    }

    private void processNoResponsePr(GHPullRequest request) {
        try {
            List<GHIssueComment> comments = request.getComments();
            String myLogin = gitHub.getMyself().getLogin();
            for (GHIssueComment comment : comments) {
                if (Objects.equals(myLogin, comment.getUser().getLogin())) {
                    return;
                }
            }
        } catch (Exception e) {
            log.warn("REPO[{}] PR[{}] getComment error", request.getRepository().getName(), request.getNumber());
        }

    }
}

