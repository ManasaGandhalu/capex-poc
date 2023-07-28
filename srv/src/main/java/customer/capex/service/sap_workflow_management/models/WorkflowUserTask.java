package customer.capex.service.sap_workflow_management.models;

import java.util.HashMap;
import java.util.Map;

import customer.capex.service.sap_workflow_management.enums.WorkflowStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkflowUserTask {

	private Map<String, Object> context = new HashMap<>();

	private WorkflowStatus status;

    private String decision;

}

