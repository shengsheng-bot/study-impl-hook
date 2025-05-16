package club.shengsheng.bot.handler;

import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHEvent;
import org.springframework.stereotype.Component;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
@Component
@Github(GHEvent.WORKFLOW_JOB)
@Slf4j
public class WorkFlowJobHandler implements GitHubEventHandler {

    @Override
    public void handle(GHEvent ghEvent, String payload) throws Exception {
        log.info("收到工作流事件{}", ghEvent);
    }


}
