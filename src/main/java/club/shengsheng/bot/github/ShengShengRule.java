package club.shengsheng.bot.github;

import lombok.Data;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.yaml.snakeyaml.Yaml;

import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
@Data
public class ShengShengRule {

    public static final String SHENG_SHENG_RULE_YML = "shengsheng.yml";

    private List<String> allow;

    private ShengShengRule() {

    }

    public void setAllow(List<String> allow) {
        if (allow.isEmpty()) {
            allow = List.of("src.main.java");
        }
        this.allow = allow;
    }

    @SuppressWarnings("unchecked")
    public static ShengShengRule from(GHRepository repository) {
        try {
            GHContent ci = repository.getFileContent(SHENG_SHENG_RULE_YML);
            Yaml yaml = new Yaml();
            Map<String, Object> load = yaml.load(new URL(ci.getDownloadUrl()).openStream());
            ShengShengRule rule = new ShengShengRule();
            List<String> allow = (List<String>) load.getOrDefault("allow", List.of("src.main.java"));
            rule.setAllow(allow);
            return rule;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean pass(String filename) {
        filename = filename.replace("/", ".");
        for (String prefix : this.allow) {
            if (filename.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
