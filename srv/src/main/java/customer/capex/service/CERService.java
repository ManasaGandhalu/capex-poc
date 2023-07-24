package customer.capex.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.sap.cds.Struct;
import com.sap.cds.services.cds.CdsCreateEventContext;
import com.sap.cds.services.cds.CdsReadEventContext;

import cds.gen.adminservice.ApprovalQuery;
import cds.gen.adminservice.CERApproval;
import cds.gen.adminservice.CERLineItem;
import cds.gen.adminservice.Cer;
import cds.gen.capex.AgainstBudgetaryStatistics;
import cds.gen.capex.ApprovalQueryStatistics;
import cds.gen.capex.MasterTAT;
import cds.gen.capex.MasterTATLevel;
import cds.gen.capex.MediaStore;
import customer.capex.enums.StatusEnum;
import customer.capex.repository.CqnRepository;
import customer.capex.service.sap_document_management.enums.MediaDirectory;
import customer.capex.service.sap_document_management.models.Media;
import customer.capex.service.sap_workflow_management.WorkflowManagementAPI;
import customer.capex.service.sap_workflow_management.contexts.ApprovalContext;
import customer.capex.service.sap_workflow_management.enums.WorkflowDefinition;
import customer.capex.service.sap_workflow_management.models.WorkflowRequest;
import customer.capex.service.sap_workflow_management.models.WorkflowInstance;
import customer.capex.utils.Utility;

@Service
public class CERService {
    
    @Autowired
    MediaStoreService mediaStoreService;

    @Autowired
    CqnRepository cqnRepository;

    @Autowired
    WorkflowManagementAPI workflowAPI;

    public void afterCreateApprovalQuery(ApprovalQuery view, CdsCreateEventContext context) {
        Media media = new Media();
        media.setName(view.getAttachmentName());
        media.setContentType(view.getAttachmentType());
        media.setBytes(view.getAttachment());
        MediaStore mediaStore = mediaStoreService.upload(MediaDirectory.APPROVAL_QUERY, view.getId(), media);
        view.setMediaStoreId(mediaStore.getId());
        cqnRepository.saveApprovalQueryMediaStoreId(view.getId(), mediaStore.getId());
    }

    public void afterReadApprovalQuery(List<ApprovalQuery> list, CdsReadEventContext context) {
        if(list.size() == 1) {
            ApprovalQuery view = list.get(0);
            Media media = new Media();
            media.setName(view.getAttachmentName());
            media.setContentType(view.getAttachmentType());
            media.setBytes(view.getAttachment());
            MediaStore mediaStore = mediaStoreService.download(MediaDirectory.APPROVAL_QUERY, view.getId());
            view.setAttachment(mediaStore.getContent());
            view.setAttachmentName(mediaStore.getMediaName());
            view.setAttachmentType(mediaStore.getContentType());
        }
    }

    public void afterCreateCER(Cer view, CdsCreateEventContext context) {
        MasterTAT masterTAT = cqnRepository.findMasterTATByCERTypeID(view.getCERTypeId());
        if(masterTAT != null) {
            List<MasterTATLevel> tatLevels = masterTAT.getTATLevels();
            List<CERApproval> approvals = new ArrayList<>();
            if(tatLevels.size() > 0) {
                tatLevels.stream().forEach(tatLevel -> {
                    CERApproval cerApproval = CERApproval.create();
                    cerApproval.setId(UUID.randomUUID().toString());
                    cerApproval.setCerId(view.getId());
                    cerApproval.setLevel(tatLevel.getLevel());
                    cerApproval.setStatus(StatusEnum.NONE.status());
                    cerApproval.setTATDurationMinutes(tatLevel.getTATDurationMinutes());
                    cerApproval.setTatId(tatLevel.getTatId());
                    cerApproval.setTATUserEmail(tatLevel.getTATUserEmail());
                    approvals.add(cerApproval);
                });
                // initializing 1st step
                CERApproval currentApproval = approvals.get(0);
                currentApproval.setStatus(StatusEnum.PENDING.status()); 
                view.setCurrentTATLevel(currentApproval.getLevel());
                view.setTATUserEmail(currentApproval.getTATUserEmail());
            }
            view.setCERCode(Utility.generateToken(8, false));
            view.setCERApprovals(approvals);
            view.setTotalTATLevels(tatLevels.size());
            view.setStatusId(StatusEnum.PENDING.code());
            String workflowRequestId = triggerApprovalWorkflow(view);
            view.setWorkflowRequestId(workflowRequestId);
            double totalBudgetaryCost = 0d;
            for(CERLineItem item: view.getCERLineItems()) {
                totalBudgetaryCost += item.getGrossCost();
            }
            view.setBudgetaryTotalCost(totalBudgetaryCost);
            cds.gen.capex.Cer cer = Struct.access(view).as(cds.gen.capex.Cer.class);
            cqnRepository.updateCER(cer);
        }
    }

    public void afterReadCER(List<Cer> list, CdsReadEventContext context) {
        list.stream().forEach(view -> {
            AgainstBudgetaryStatistics stats = cqnRepository.getAgainstBudgetaryStatistics(view.getId());
            view.setAgainstBudgetaryCount(stats.getAgainstBudgetaryCount() != null ? stats.getAgainstBudgetaryCount() : 0);
            view.setAgainstBudgetaryTotalCost(stats.getAgainstBudgetaryTotalCost() != null ? stats.getAgainstBudgetaryTotalCost() : 0);
        });
    }

    public void afterReadCERApproval(List<CERApproval> list, CdsReadEventContext context) {
        list.stream().forEach(view -> {
            ApprovalQueryStatistics stats = cqnRepository.getApprovalQueryStatistics(view.getId());
            view.setTotalQueries(stats.getTotalQueries() != null ? stats.getTotalQueries() : 0);
            view.setTotalAttachments(stats.getTotalAttachments() != null ? stats.getTotalAttachments() : 0);
        });
    }

    public CERApproval onUpdateApprovalStatus(String cerApprovalId, String status) {
        cds.gen.capex.CERApproval cerApproval = cqnRepository.findCERApproval(cerApprovalId);
        cerApproval.setStatus(status);
        cqnRepository.updateCERApproval(cerApproval);
        String currentTATUserEmail = cerApproval.getTATUserEmail();
        Integer currentTATLevel = cerApproval.getLevel();
        if(StatusEnum.APPROVED.status().equals(status)) {
            cds.gen.capex.CERApproval nextApproval = cqnRepository.findCERApprovalByCerIdandLevel(cerApproval.getCerId(), currentTATLevel + 1);
            if(nextApproval != null) {
                currentTATLevel = nextApproval.getLevel();
                currentTATUserEmail = nextApproval.getTATUserEmail();
                status = StatusEnum.PENDING.status();
                nextApproval.setStatus(status);
                cqnRepository.updateCERApproval(nextApproval);
            }
        }
        cqnRepository.updateCERApprovalDetails(cerApproval.getCerId(), StatusEnum.getEnum(status).code(), currentTATLevel, currentTATUserEmail);
        return Struct.access(cerApproval).as(CERApproval.class);
    }

    public String triggerApprovalWorkflow(Cer cer) {
        WorkflowRequest<ApprovalContext> workflowRequest = new WorkflowRequest<>();
        ApprovalContext context = new ApprovalContext();
        context.setCerId(cer.getId());
        workflowRequest.setDefinitionId(WorkflowDefinition.approval.definitionId());
        workflowRequest.setContext(context);

        WorkflowInstance workflowInstance = null;
        // trigger workflow request
        ResponseEntity<WorkflowInstance> response = workflowAPI.initiate(workflowRequest);
        if(response.getStatusCode().is2xxSuccessful()) {
            workflowInstance = response.getBody();
        }
        if(workflowInstance != null) {
            return workflowInstance.getId();
        }
        return null;
    }

}
