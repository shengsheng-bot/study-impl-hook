package club.shengsheng.bot.configuration;

import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
@Configuration
public class GithubConfiguration {


    @Bean
    public GitHub gitHub() throws IOException {
        return GitHubBuilder.fromEnvironment().build();
    }

}
