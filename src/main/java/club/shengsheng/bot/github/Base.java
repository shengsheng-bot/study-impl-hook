/**
 * @author gongxuanzhangmelt@gmail.com
 **/
package club.shengsheng.bot.github;

import lombok.Data;

@Data
public class Base {
    private String label;
    private String ref;
    private String sha;
    private User user;
    private Repository repo;
}
