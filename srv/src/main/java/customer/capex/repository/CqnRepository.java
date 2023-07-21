package customer.capex.repository;

import java.util.List;
import java.util.UUID;

import cds.gen.capex.ApprovalQuery;
import cds.gen.capex.ApprovalQueryStatistics;
import cds.gen.capex.CERApproval;
import cds.gen.capex.Cer;
import cds.gen.capex.MasterTAT;

public interface CqnRepository {

    public ApprovalQuery saveApprovalQuery(ApprovalQuery approvalQuery);

    public void saveApprovalQueryMediaStoreId(String approvalQueryId, String mediaStoreId);

    public MasterTAT findMasterTATByCERTypeID(Integer cerTypeId);

    public void saveCERApprovals(List<CERApproval> approvals);

    public void updateCER(Cer view);

    public Double getAgainstBudgetaryTotalCost(String cerId);

    public ApprovalQueryStatistics getApprovalQueryStatistics(String cerApprovalId);

    public CERApproval findCERApproval(String cerApprovalId);

    public void updateCERApproval(CERApproval cerApproval);

    public CERApproval findCERApprovalByCerIdandLevel(String cerId, int level);

    public void updateCERStatusIdAndTATLevel(String cerId, int statusId, Integer currentTATLevel);

}