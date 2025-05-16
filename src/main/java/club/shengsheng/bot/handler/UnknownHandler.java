package club.shengsheng.bot.handler;

import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHEvent;
import org.springframework.stereotype.Component;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
@Component
@Slf4j
public class UnknownHandler implements GitHubEventHandler {


    @Override
    public void handle(GHEvent ghEvent, String payload) throws Exception {
        log.warn("遇到了未知的事件{}", ghEvent);
    }
}
