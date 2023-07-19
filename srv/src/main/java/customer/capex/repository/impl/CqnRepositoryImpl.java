package customer.capex.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sap.cds.Result;
import com.sap.cds.ql.Update;
import com.sap.cds.ql.Upsert;
import com.sap.cds.ql.cqn.CqnUpdate;
import com.sap.cds.ql.cqn.CqnUpsert;
import com.sap.cds.services.persistence.PersistenceService;

import cds.gen.capex.ApprovalQuery;
import cds.gen.capex.ApprovalQuery_;
import customer.capex.repository.CqnRepository;

@Repository
public class CqnRepositoryImpl implements CqnRepository {

    @Autowired
    PersistenceService db;
    
    @Override
    public ApprovalQuery saveApprovalQuery(ApprovalQuery approvalQuery) {
        CqnUpsert upsert = Upsert.into(ApprovalQuery_.CDS_NAME).entry(approvalQuery);
        Result result = db.run(upsert);
		return result.first(ApprovalQuery.class).orElse(null);
    }

    @Override
    public void saveApprovalQueryMediaStoreId(String approvalQueryId, String mediaStoreId) {
        CqnUpdate update = Update.entity(ApprovalQuery_.CDS_NAME).data(ApprovalQuery.MEDIA_STORE_ID, mediaStoreId).byId(approvalQueryId);
        Result result = db.run(update);
    } 

}
