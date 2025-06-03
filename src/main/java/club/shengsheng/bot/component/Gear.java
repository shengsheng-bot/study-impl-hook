package club.shengsheng.bot.component;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
public class Gear {

    private final int cycleCount;

    private final Runnable runnable;

    private int executeCount = 0;

    public Gear(int cycleCount, Runnable cycleRunnable) {
        this.cycleCount = cycleCount;
        this.runnable = cycleRunnable;
    }

    public void turn() {
        if ((executeCount++ % cycleCount) == 0) {
            runnable.run();
        }
    }
}
