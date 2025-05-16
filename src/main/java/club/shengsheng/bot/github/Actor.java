package club.shengsheng.bot.github;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

/**
 * @author gongxuanzhang
 */
@Data
public class Actor {

    private String login;
    private long id;

    @JSONField(name = "node_id")
    private String nodeId;

    @JSONField(name = "avatar_url")
    private String avatarUrl;

    @JSONField(name = "html_url")
    private String htmlUrl;

    private String type;

    @JSONField(name = "site_admin")
    private boolean siteAdmin;
}
