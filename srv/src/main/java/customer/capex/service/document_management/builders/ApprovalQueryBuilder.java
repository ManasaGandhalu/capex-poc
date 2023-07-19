package customer.capex.service.document_management.builders;

import com.fasterxml.jackson.databind.ObjectMapper;

import cds.gen.capex.ApprovalQuery;

public class ApprovalQueryBuilder {
    
    public static ApprovalQuery fromView(cds.gen.adminservice.ApprovalQuery view) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(view, ApprovalQuery.class);
    }

    public static cds.gen.adminservice.ApprovalQuery toView(ApprovalQuery approvalQuery) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(approvalQuery, cds.gen.adminservice.ApprovalQuery.class);
    }

}
