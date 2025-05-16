/**
 * @author gongxuanzhangmelt@gmail.com
 **/
package club.shengsheng.bot.github;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

@Data
public class Repository {
    private long id;
    private String name;

    @JSONField(name = "full_name")
    private String fullName;

    @JSONField(name = "private")
    private boolean isPrivate;

    private User owner;

    @JSONField(name = "html_url")
    private String htmlUrl;

    private String url;
    private String language;
}
