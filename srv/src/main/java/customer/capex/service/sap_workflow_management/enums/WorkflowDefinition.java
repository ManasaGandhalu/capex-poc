package customer.capex.service.sap_workflow_management.enums;

public enum WorkflowDefinition {
    
    approval("com.cipla.capex.approval");
    
    private String id;

    WorkflowDefinition(String definitionId) {
        id = definitionId;
    }

    public String definitionId() {
        return id;
    }

    public static WorkflowDefinition getEnum(String definitionId) {
        WorkflowDefinition[] enums = WorkflowDefinition.values();
        for(int i = 0; i < enums.length; i++) {
            if(enums[i].definitionId().equals(definitionId)) {
                return enums[i];
            }
        }
        return null;
    }

}
