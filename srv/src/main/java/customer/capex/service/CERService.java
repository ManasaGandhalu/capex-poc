package customer.capex.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sap.cds.Struct;
import com.sap.cds.services.cds.CdsCreateEventContext;
import com.sap.cds.services.cds.CdsReadEventContext;

import cds.gen.adminservice.ApprovalQuery;
import cds.gen.adminservice.CERApproval;
import cds.gen.adminservice.CERLineItem;
import cds.gen.adminservice.Cer;
import cds.gen.capex.ApprovalQueryStatistics;
import cds.gen.capex.MasterTAT;
import cds.gen.capex.MasterTATLevel;
import cds.gen.capex.MediaStore;
import customer.capex.enums.StatusEnum;
import customer.capex.repository.CqnRepository;
import customer.capex.service.document_management.enums.MediaDirectory;
import customer.capex.service.document_management.models.Media;

@Service
public class CERService {
    
    @Autowired
    MediaStoreService mediaStoreService;

    @Autowired
    CqnRepository cqnRepository;

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
            }
            view.setCERApprovals(approvals);
            view.setTotalTATLevels(tatLevels.size());
            view.setStatusId(StatusEnum.PENDING.code());
            view.setWorkflowRequestId("WF123");
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
            Double totalAgainstBudgetaryCost = cqnRepository.getAgainstBudgetaryTotalCost(view.getId());
            view.setAgainstBudgetaryTotalCost(totalAgainstBudgetaryCost);
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
        Integer currentTATLevel = cerApproval.getLevel();
        if(StatusEnum.APPROVED.status().equals(status)) {
            cds.gen.capex.CERApproval nextApproval = cqnRepository.findCERApprovalByCerIdandLevel(cerApproval.getCerId(), currentTATLevel + 1);
            if(nextApproval != null) {
                currentTATLevel = nextApproval.getLevel();
                status = StatusEnum.PENDING.status();
            }
        }
        cqnRepository.updateCERStatusIdAndTATLevel(cerApproval.getCerId(), StatusEnum.getEnum(status).code(), currentTATLevel);
        return Struct.access(cerApproval).as(CERApproval.class);
    }

}
