/**
 * @author gongxuanzhangmelt@gmail.com
 **/
package club.shengsheng.bot.github;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

@Data
public class User {
    private String login;
    private int id;

    @JSONField(name = "avatar_url")
    private String avatarUrl;

    private String url;

    @JSONField(name = "html_url")
    private String htmlUrl;
}
