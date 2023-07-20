package customer.capex.handler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sap.cds.services.cds.CdsCreateEventContext;
import com.sap.cds.services.cds.CdsReadEventContext;
import com.sap.cds.services.cds.CqnService;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.After;
import com.sap.cds.services.handler.annotations.On;
import com.sap.cds.services.handler.annotations.ServiceName;
import com.sap.cds.services.persistence.PersistenceService;

import cds.gen.adminservice.AdminService_;
import cds.gen.adminservice.ApprovalQuery;
import cds.gen.adminservice.ApprovalQuery_;
import cds.gen.adminservice.CERApproval;
import cds.gen.adminservice.CERApproval_;
import cds.gen.adminservice.Cer;
import cds.gen.adminservice.Cer_;
// import cds.gen.adminservice.UpdateApprovalStatusContext;
import customer.capex.service.CERService;

@Component
@ServiceName(AdminService_.CDS_NAME)
public class AdminServiceHandler implements EventHandler {

    private static final Logger log = LoggerFactory.getLogger(AdminServiceHandler.class);

    @Autowired
    PersistenceService db;

    @Autowired
    CERService cerService;

    /*
     * After Event Handlers
     */

    @After(event = CqnService.EVENT_CREATE, entity = ApprovalQuery_.CDS_NAME)
    void afterCreateApprovalQuery(ApprovalQuery view, CdsCreateEventContext context) {
        cerService.afterCreateApprovalQuery(view, context);
    }

    @After(event = CqnService.EVENT_READ, entity = ApprovalQuery_.CDS_NAME)
    void afterReadApprovalQuery(List<ApprovalQuery> list, CdsReadEventContext context) {
        cerService.afterReadApprovalQuery(list, context);
    }

    @After(event = CqnService.EVENT_CREATE, entity = Cer_.CDS_NAME)
    void afterCreateCER(Cer view, CdsCreateEventContext context) {
        cerService.afterCreateCER(view, context);
    }

    @After(event = CqnService.EVENT_READ, entity = Cer_.CDS_NAME)
    void afterReadCER(List<Cer> list, CdsReadEventContext context) {
        cerService.afterReadCER(list, context);
    }

    @After(event = CqnService.EVENT_READ, entity = CERApproval_.CDS_NAME)
    void afterReadCERApproval(List<CERApproval> list, CdsReadEventContext context) {
        cerService.afterReadCERApproval(list, context);
    }

    // /**
    //  * On Event Handlers
    //  */
    // @On(event = CqnService.EVENT_CREATE)
    // void onUpdateApprovalStatus(UpdateApprovalStatusContext context) {
    //     String cerApprovalId = context.getCerApprovalId();
    //     String status = context.getStatus();
    //     cerService.onUpdateApprovalStatus(cerApprovalId, status);
    //     context.setCompleted();
    // }

}