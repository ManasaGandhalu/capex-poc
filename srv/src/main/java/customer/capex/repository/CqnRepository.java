package customer.capex.repository;

import cds.gen.capex.ApprovalQuery;

public interface CqnRepository {

    public ApprovalQuery saveApprovalQuery(ApprovalQuery approvalQuery);

    public void saveApprovalQueryMediaStoreId(String approvalQueryId, String mediaStoreId);

}