package club.shengsheng.bot.configuration;

import club.shengsheng.bot.component.MergeEventLoop;
import org.kohsuke.github.GitHub;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
@Configuration
public class MergeConfiguration {


    @Bean
    public MergeEventLoop mergeEventLoop(GitHub gitHub) {
        return new MergeEventLoop(4, gitHub);
    }

}
