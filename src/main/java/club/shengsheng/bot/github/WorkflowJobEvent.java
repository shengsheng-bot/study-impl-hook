/**
 * @author gongxuanzhangmelt@gmail.com
 **/
package club.shengsheng.bot.github;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

@Data
public class WorkflowJobEvent {
    private String action;

    @JSONField(name = "workflow_job")
    private WorkflowJob workflowJob;

    private Repository repository;
    private Organization organization;
    private Sender sender;
}
