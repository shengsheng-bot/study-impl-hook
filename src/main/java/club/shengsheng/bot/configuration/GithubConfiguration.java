package club.shengsheng.bot.configuration;

import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
@Configuration
@Slf4j
public class GithubConfiguration {


    @Bean
    public GitHub gitHub() throws IOException {
        GitHub github = GitHubBuilder.fromEnvironment().build();
        log.info("GitHub client initialized");
        return github;
    }

}
