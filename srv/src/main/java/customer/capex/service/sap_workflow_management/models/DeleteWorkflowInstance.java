package customer.capex.service.sap_workflow_management.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteWorkflowInstance {

	private String id;

	private boolean deleted = true;

    public DeleteWorkflowInstance(String id) {
        this.id = id;
        this.deleted = true;
    }

}