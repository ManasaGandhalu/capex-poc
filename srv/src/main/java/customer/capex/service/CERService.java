package customer.capex.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sap.cds.services.cds.CdsCreateEventContext;
import com.sap.cds.services.cds.CdsReadEventContext;

import cds.gen.adminservice.ApprovalQuery;
import cds.gen.capex.MediaStore;
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

    public void afterReadApprovalQuery(ApprovalQuery view, CdsReadEventContext context) {
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
