package customer.capex.repository;

import java.util.List;

import cds.gen.capex.MediaStore;
import customer.capex.service.sap_document_management.enums.MediaDirectory;
import customer.capex.service.sap_document_management.enums.MediaStatus;

public interface MediaStoreRepository {

    public MediaStore save(MediaStore mediaStore);
	public MediaStore findByDirectoryNameAndDirectoryId(MediaDirectory directoryName, String directoryId);
	public List<MediaStore> findAllSyncableRecords(List<MediaStatus> statusList, int skip, int top);
	public void updateMediaStatus(String id, MediaStatus status);
	public void deleteByDirectoryNameAndDirectoryId(Integer directoryId, MediaDirectory directoryName);

}
