/**
 * @author gongxuanzhangmelt@gmail.com
 **/
package club.shengsheng.bot.github;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

@Data
public class Sender {
    private String login;
    private long id;

    @JSONField(name = "avatar_url")
    private String avatarUrl;
}
