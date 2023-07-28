package customer.capex.service.sap_workflow_management.contexts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApprovalContext {

    @JsonProperty("CER_ID")
    private String cerId;

}
