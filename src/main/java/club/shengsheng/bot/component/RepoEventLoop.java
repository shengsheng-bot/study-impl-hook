package club.shengsheng.bot.component;

import io.netty.channel.DefaultEventLoop;
import io.netty.channel.EventLoop;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.context.SmartLifecycle;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A wrapper of {@link EventLoop}
 * It is also a thread pool
 * This class has two core APIs:
 * {@link  this#execute(GHRepository, Runnable)} 
 * This API guarantees that the same repository logic is executed by the same thread
 * 
 * 
 * {@link this#execute(GHPullRequest, Runnable)}
 * This API guarantees that the same pull request logic is executed by the same thread
 *
 * @author gongxuanzhangmelt@gmail.com
 **/
@Slf4j
public class RepoEventLoop implements SmartLifecycle {

    private final AtomicBoolean running = new AtomicBoolean(false);

    private final EventLoop[] eventLoops;

    private final boolean isPowerOfTwo;

    private final Chooser chooser;

    private final GitHub gitHub;

    public RepoEventLoop(int threadNum, GitHub gitHub) {
        eventLoops = new EventLoop[threadNum];
        isPowerOfTwo = (threadNum & (threadNum - 1)) == 0;
        chooser = new Chooser();
        this.gitHub = gitHub;
    }


    @Override
    public void start() {
        if (!running.compareAndSet(false, true)) {
            log.warn("RepoEventLoop is already running");
            return;
        }
        Arrays.fill(eventLoops, new DefaultEventLoop());
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            log.info("RepoEventLoop stopped");
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }


    public void execute(GHRepository repository, Runnable runnable) {
        EventLoop eventLoop = this.chooser.choose(repository.getId());
        eventLoop.execute(runnable);
    }

    public void execute(GHPullRequest pullRequest, Runnable runnable) {
        long repoId = pullRequest.getRepository().getId();
        long number = pullRequest.getNumber();
        number <<= 40;
        long id = repoId | number;
        EventLoop eventLoop = this.chooser.choose(id);
        eventLoop.execute(runnable);
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
