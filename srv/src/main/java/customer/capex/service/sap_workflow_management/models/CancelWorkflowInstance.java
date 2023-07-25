package customer.capex.service.sap_workflow_management.models;

import customer.capex.service.sap_workflow_management.enums.WorkflowStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelWorkflowInstance {

	private WorkflowStatus status = WorkflowStatus.CANCELED;

	private boolean cascade = false;

    public CancelWorkflowInstance(boolean cascade) {
        this.cascade = cascade;
    }

}