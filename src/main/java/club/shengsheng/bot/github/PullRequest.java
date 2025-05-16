/**
 * @author gongxuanzhangmelt@gmail.com
 **/
package club.shengsheng.bot.github;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.util.Date;

@Data
public class PullRequest {
    private String url;
    private long id;

    @JSONField(name = "html_url")
    private String htmlUrl;

    @JSONField(name = "diff_url")
    private String diffUrl;

    @JSONField(name = "patch_url")
    private String patchUrl;

    private String state;
    private String title;
    private User user;
    private String body;

    @JSONField(name = "created_at")
    private Date createdAt;

    @JSONField(name = "updated_at")
    private Date updatedAt;

    @JSONField(name = "changed_files")
    private long changedFiles;

    private long additions;
    private long deletions;
    private long commits;
    private Head head;
    private Base base;
}
