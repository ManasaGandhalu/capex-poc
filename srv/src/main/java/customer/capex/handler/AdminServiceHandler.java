package customer.capex.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sap.cds.services.cds.CdsCreateEventContext;
import com.sap.cds.services.cds.CdsReadEventContext;
import com.sap.cds.services.cds.CqnService;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.After;
import com.sap.cds.services.handler.annotations.ServiceName;
import com.sap.cds.services.persistence.PersistenceService;

import cds.gen.adminservice.AdminService_;
import cds.gen.adminservice.ApprovalQuery;
import cds.gen.adminservice.ApprovalQuery_;
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
     * After Event Listeners
     */

    @After(event = CqnService.EVENT_CREATE, entity = ApprovalQuery_.CDS_NAME)
    void afterCreateApprovalQuery(ApprovalQuery view, CdsCreateEventContext context) {
        cerService.afterCreateApprovalQuery(view, context);
    }

    @After(event = CqnService.EVENT_READ, entity = ApprovalQuery_.CDS_NAME)
    void afterReadApprovalQuery(ApprovalQuery view, CdsReadEventContext context) {
        cerService.afterReadApprovalQuery(view, context);
    }

}