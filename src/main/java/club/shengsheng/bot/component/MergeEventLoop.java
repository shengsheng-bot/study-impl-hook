package club.shengsheng.bot.component;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import io.netty.channel.DefaultEventLoop;
import io.netty.channel.EventLoop;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.context.SmartLifecycle;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

import static club.shengsheng.bot.github.GitHubConstant.GITHUB_EMAIL_KEY;
import static club.shengsheng.bot.github.GitHubConstant.GITHUB_TOKEN_KEY;
import static club.shengsheng.bot.github.GitHubConstant.GITHUB_USER_KEY;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
@Slf4j
public class MergeEventLoop implements SmartLifecycle {

    private final AtomicBoolean running = new AtomicBoolean(false);

    private final EventLoop[] eventLoops;

    private final boolean isPowerOfTwo;

    private final Chooser chooser;

    private final GitHub gitHub;

    public MergeEventLoop(int threadNum, GitHub gitHub) {
        eventLoops = new EventLoop[threadNum];
        isPowerOfTwo = (threadNum & (threadNum - 1)) == 0;
        chooser = new Chooser();
        this.gitHub = gitHub;
    }


    @Override
    public void start() {
        if (!running.compareAndSet(false, true)) {
            log.warn("MergeEventLoop is already running");
            return;
        }
        Arrays.fill(eventLoops, new DefaultEventLoop());
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            log.info("MergeEventLoop stopped");
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    public void mergeAndRevertAsync(GHPullRequest pullRequest) {
        GHRepository repository = pullRequest.getRepository();
        EventLoop eventLoop = this.chooser.choose(repository.getId());
        eventLoop.execute(() -> {
            try {
                pullRequest.merge("merge", null, GHPullRequest.MergeMethod.SQUASH);
                revert(repository, pullRequest.getMergeCommitSha());
            } catch (Exception e) {
                log.error("Merge and revert failed", e);
            }
        });
    }

    private void revert(GHRepository repository, String lastCommitSha1) throws IOException {
        File dir = new File(repository.getName());
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


    class Chooser {
        private final AtomicInteger index = new AtomicInteger(0);

        private final Map<Long, EventLoop> regedit = new ConcurrentHashMap<>();

        EventLoop choose(long repositoryId) {
            return regedit.computeIfAbsent(repositoryId, k -> next());
        }

        EventLoop next() {
            int currentIndex = index.getAndIncrement();
            if (isPowerOfTwo) {
                return eventLoops[currentIndex & (eventLoops.length - 1)];
            }
            return eventLoops[currentIndex % eventLoops.length];
        }
    }
}
