package club.shengsheng.bot.github;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.kohsuke.github.GHWorkflowRun;

import java.util.List;
import java.util.Map;

/**
 * @author gongxuanzhang
 */
@Data
public class WorkflowRun {

    private long id;

    private String name;

    @JSONField(name = "head_branch")
    private String headBranch;

    @JSONField(name = "head_sha")
    private String headSha;

    private String path;

    @JSONField(name = "display_title")
    private String displayTitle;

    @JSONField(name = "run_number")
    private long runNumber;

    private String event;

    private String status;

    private GHWorkflowRun.Conclusion conclusion;

    @JSONField(name = "workflow_id")
    private long workflowId;

    @JSONField(name = "check_suite_id")
    private long checkSuiteId;

    @JSONField(name = "check_suite_node_id")
    private String checkSuiteNodeId;

    private String url;

    @JSONField(name = "html_url")
    private String htmlUrl;

    @JSONField(name = "pull_requests")
    private List<Object> pullRequests;

    @JSONField(name = "created_at")
    private String createdAt;

    @JSONField(name = "updated_at")
    private String updatedAt;

    private Actor actor;

    @JSONField(name = "run_attempt")
    private int runAttempt;

    @JSONField(name = "referenced_workflows")
    private List<Object> referencedWorkflows;

    @JSONField(name = "run_started_at")
    private String runStartedAt;

    @JSONField(name = "triggering_actor")
    private Actor triggeringActor;

    @JSONField(name = "jobs_url")
    private String jobsUrl;

    @JSONField(name = "logs_url")
    private String logsUrl;

    @JSONField(name = "check_suite_url")
    private String checkSuiteUrl;

    @JSONField(name = "artifacts_url")
    private String artifactsUrl;

    @JSONField(name = "cancel_url")
    private String cancelUrl;

    @JSONField(name = "rerun_url")
    private String rerunUrl;

    @JSONField(name = "previous_attempt_url")
    private String previousAttemptUrl;

    @JSONField(name = "workflow_url")
    private String workflowUrl;

    @JSONField(name = "head_commit")
    private Map<String, Object> headCommit;

    private Repository repository;

    @JSONField(name = "head_repository")
    private Repository headRepository;
}
