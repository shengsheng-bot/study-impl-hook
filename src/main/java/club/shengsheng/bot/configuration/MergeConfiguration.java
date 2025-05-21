package club.shengsheng.bot.configuration;

import club.shengsheng.bot.component.RepoEventLoop;
import org.kohsuke.github.GitHub;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
@Configuration
public class MergeConfiguration {


    @Bean
    public RepoEventLoop mergeEventLoop(GitHub gitHub) {
        return new RepoEventLoop(4, gitHub);
    }

}
