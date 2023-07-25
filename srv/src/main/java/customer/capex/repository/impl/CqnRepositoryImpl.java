package customer.capex.repository.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sap.cds.Result;
import com.sap.cds.ql.CQL;
import com.sap.cds.ql.Select;
import com.sap.cds.ql.Update;
import com.sap.cds.ql.Upsert;
import com.sap.cds.ql.cqn.CqnSelect;
import com.sap.cds.ql.cqn.CqnSelectListItem;
import com.sap.cds.ql.cqn.CqnUpdate;
import com.sap.cds.ql.cqn.CqnUpsert;
import com.sap.cds.services.persistence.PersistenceService;

import cds.gen.capex.AgainstBudgetaryStatistics;
import cds.gen.capex.ApprovalQuery;
import cds.gen.capex.ApprovalQueryStatistics;
import cds.gen.capex.ApprovalQuery_;
import cds.gen.capex.CERApproval;
import cds.gen.capex.CERApproval_;
import cds.gen.capex.Cer;
import cds.gen.capex.Cer_;
import cds.gen.capex.MasterTAT;
import cds.gen.capex.MasterTAT_;
import customer.capex.repository.CqnRepository;

@Repository
public class CqnRepositoryImpl implements CqnRepository {

    @Autowired
    PersistenceService db;
    
    @Override
    public ApprovalQuery saveApprovalQuery(ApprovalQuery approvalQuery) {
        CqnUpsert upsert = Upsert.into(ApprovalQuery_.class).entry(approvalQuery);
        Result result = db.run(upsert);
		return result.first(ApprovalQuery.class).orElse(null);
    }

    @Override
    public void saveApprovalQueryMediaStoreId(String approvalQueryId, String mediaStoreId) {
        CqnUpdate update = Update.entity(ApprovalQuery_.class).data(ApprovalQuery.MEDIA_STORE_ID, mediaStoreId).byId(approvalQueryId);
        Result result = db.run(update);
    }

    @Override
    public MasterTAT findMasterTATByCERTypeID(Integer cerTypeId) {
        CqnSelect select = Select.from(MasterTAT_.class).where(m -> 
			m.get(MasterTAT.CERTYPE_ID).eq(cerTypeId))
            .columns(m -> m._all(), m -> m.expand(MasterTAT.TATLEVELS));
		Result result = db.run(select);
		return result.first(MasterTAT.class).orElse(null);
    }

    @Override
    public void saveCERApprovals(List<CERApproval> approvals) {
        CqnUpsert upsert = Upsert.into(CERApproval_.class).entries(approvals);
        db.run(upsert);
    }

    @Override
    public void updateCER(Cer cer) {
        CqnUpdate update = Update.entity(Cer_.class).entry(cer).byId(cer.getId());
        db.run(update);
    }

    @Override
    public AgainstBudgetaryStatistics getAgainstBudgetaryStatistics(String cerId) {
        CqnSelect select = Select.from(Cer_.class)
            .where(c -> c.ParentCER_ID().eq(cerId))
            .columns(
                c -> CQL.count(c.ID()).as(AgainstBudgetaryStatistics.AGAINST_BUDGETARY_COUNT),
                c -> CQL.sum(c.BudgetaryTotalCost()).as(AgainstBudgetaryStatistics.AGAINST_BUDGETARY_TOTAL_COST)
            );
        Result result = db.run(select);
        if(result.rowCount() == 0) {
            return AgainstBudgetaryStatistics.create();
        }
        return result.single(AgainstBudgetaryStatistics.class);
    }

    @Override
    public ApprovalQueryStatistics getApprovalQueryStatistics(String cerApprovalId) {
        CqnSelectListItem TOTAL_ATTACHMENTS = CQL.plain("SUM(CASE WHEN MediaStoreId is not null THEN 1 ELSE 0 END)")
                .as(ApprovalQueryStatistics.TOTAL_ATTACHMENTS);

        CqnSelect select = Select.from(ApprovalQuery_.class).where(
            c -> c.CERApprovalId().eq(cerApprovalId)
        ).columns(
            c -> CQL.count(c.ID()).as(ApprovalQueryStatistics.TOTAL_QUERIES),
            c -> TOTAL_ATTACHMENTS
        );
        
        Result result = db.run(select);
        if(result.rowCount() == 0) {
            return ApprovalQueryStatistics.create();
        }
        return result.single(ApprovalQueryStatistics.class);
    }

    @Override
    public CERApproval findCERApproval(String cerApprovalId) {
        CqnSelect select = Select.from(CERApproval_.class).byId(cerApprovalId);
		Result result = db.run(select);
		return result.first(CERApproval.class).orElse(null);
    }

    @Override
    public void updateCERApproval(CERApproval cerApproval) {
        CqnUpdate update = Update.entity(CERApproval_.class).entry(cerApproval).byId(cerApproval.getId());
        db.run(update);
    }

    @Override
    public CERApproval findCERApprovalByCerIdandLevel(String cerId, int level) {
        CqnSelect select = Select.from(CERApproval_.class).where(c -> c.CER_ID().eq(cerId).and(c.Level().eq(level)));
		Result result = db.run(select);
		return result.first(CERApproval.class).orElse(null);
    }

    @Override
    public void updateCERApprovalDetails(String cerId, int statusId, Integer currentTATLevel, String currentTATUserEmail) {
        Map<String, Object> map = new HashMap<>();
        map.put(Cer.STATUS_ID, statusId);
        map.put(Cer.CURRENT_TATLEVEL, currentTATLevel);
        map.put(Cer.TATUSER_EMAIL, currentTATUserEmail);
        CqnUpdate update = Update.entity(Cer_.class)
            .data(map)
            .byId(cerId);
        db.run(update);
    }

    public String findWorkflowRequestIdByCerId(String cerId) {
        CqnSelect select = Select.from(Cer_.class).byId(cerId).columns(c -> c.WorkflowRequestId());
		Result result = db.run(select);
        if(result.rowCount() > 0) {
            return result.single(Cer.class).getWorkflowRequestId();
        }
		return null;
    }

}
