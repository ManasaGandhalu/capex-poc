package customer.capex.service.sap_workflow_management.models;

import java.time.Instant;

import customer.capex.service.sap_workflow_management.enums.WorkflowStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkflowInstance {

	private String id;
	private String definitionId;
	private String definitionVersion;
	private String subject;
	private String businessKey;
	private WorkflowStatus status;
	private Instant startedAt;
	private String startedBy;
	private Instant completedAt;
	private String rootInstanceId;
	private String parentInstanceId;
	
}
