package club.shengsheng.bot.handler;


import org.kohsuke.github.GHEvent;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
public interface GitHubEventHandler {

    void handle(GHEvent event, String payload) throws Exception;
}
