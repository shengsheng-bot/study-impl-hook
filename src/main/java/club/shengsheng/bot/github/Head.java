/**
 * @author gongxuanzhangmelt@gmail.com
 **/
package club.shengsheng.bot.github;

import lombok.Data;

@Data
public class Head {
    private String label;
    private String ref;
    private String sha;
    private User user;
    private Repository repo;
}
