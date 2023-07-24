package customer.capex.service.sap_workflow_management.models;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkflowRequest<T> {

	private String definitionId;

	private T context;

}