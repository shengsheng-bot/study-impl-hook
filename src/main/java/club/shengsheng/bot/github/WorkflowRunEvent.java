package club.shengsheng.bot.github;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

/**
 * @author gongxuanzhang
 */
@Data
public class WorkflowRunEvent {

    private String action;

    @JSONField(name = "workflow_run")
    private WorkflowRun workflowRun;

    private Repository repository;

    @JSONField(name = "jobs_url")
    private String jobsUrl;

    @JSONField(name = "logs_url")
    private String logsUrl;

    private Sender sender;

    private Organization organization;
}
