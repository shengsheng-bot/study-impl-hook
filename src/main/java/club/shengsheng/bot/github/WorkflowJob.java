/**
 * @author gongxuanzhangmelt@gmail.com
 **/
package club.shengsheng.bot.github;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class WorkflowJob {
    private long id;

    @JSONField(name = "run_id")
    private long runId;

    @JSONField(name = "workflow_name")
    private String workflowName;

    @JSONField(name = "head_branch")
    private String headBranch;

    @JSONField(name = "run_url")
    private String runUrl;

    @JSONField(name = "run_attempt")
    private long runAttempt;

    @JSONField(name = "node_id")
    private String nodeId;

    @JSONField(name = "head_sha")
    private String headSha;

    private String url;

    @JSONField(name = "html_url")
    private String htmlUrl;

    private String status;
    private String conclusion;

    @JSONField(name = "created_at")
    private Date createdAt;

    @JSONField(name = "started_at")
    private Date startedAt;

    @JSONField(name = "completed_at")
    private Date completedAt;

    private String name;
    private List<String> labels;

    @JSONField(name = "runner_id")
    private long runnerId;

    @JSONField(name = "runner_name")
    private String runnerName;

    @JSONField(name = "runner_group_id")
    private long runnerGroupId;

    @JSONField(name = "runner_group_name")
    private String runnerGroupName;

    @JSONField(name = "check_run_url")
    private String checkRunUrl;
}
