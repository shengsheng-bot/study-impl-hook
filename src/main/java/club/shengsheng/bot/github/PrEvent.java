/**
 * @author gongxuanzhangmelt@gmail.com
 **/
package club.shengsheng.bot.github;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

@Data
public class PrEvent {
    private String action;
    private int number;

    @JSONField(name = "pull_request")
    private PullRequest pullRequest;

    private Repository repository;
    private Organization organization;
    private Sender sender;
}
