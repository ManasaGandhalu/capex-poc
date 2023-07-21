package customer.capex.repository.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
import org.springframework.stereotype.Repository;

import com.sap.cds.Result;
import com.sap.cds.ql.Delete;
import com.sap.cds.ql.Select;
import com.sap.cds.ql.Update;
import com.sap.cds.ql.Upsert;
import com.sap.cds.ql.cqn.CqnDelete;
import com.sap.cds.ql.cqn.CqnSelect;
import com.sap.cds.ql.cqn.CqnUpdate;
import com.sap.cds.ql.cqn.CqnUpsert;
import com.sap.cds.services.persistence.PersistenceService;

import cds.gen.capex.MediaStore;
import cds.gen.capex.MediaStore_;
import customer.capex.repository.MediaStoreRepository;
import customer.capex.service.document_management.enums.MediaDirectory;
import customer.capex.service.document_management.enums.MediaStatus;

@Repository
public class MediaStoreRepositoryImpl implements MediaStoreRepository {

	@Autowired
    private PersistenceService db;

    public MediaStore save(MediaStore mediaStore) {
		CqnUpsert upsert = Upsert.into(MediaStore_.class).entry(mediaStore);
        Result result = db.run(upsert);
		return result.first(MediaStore.class).orElse(null);
	}

	public MediaStore findByDirectoryNameAndDirectoryId(MediaDirectory directoryName, String directoryId) {
		CqnSelect select = Select.from(MediaStore_.class).where(m -> 
			m.get(MediaStore.DIRECTORY_NAME).eq(directoryName.name())
			.and(m.get(MediaStore.DIRECTORY_ID).eq(directoryId))
		);
		Result result = db.run(select);
		return result.first(MediaStore.class).orElse(null);
	}

	public List<MediaStore> findAllSyncableRecords(List<MediaStatus> statusList, int skip, int top) {
		CqnSelect select = Select.from(MediaStore_.class).where(m -> 
			m.get(MediaStore.CONTENT).isNotNull()
			.and(m.get(MediaStore.URL).isNull())
			.and(m.get(MediaStore.STATUS).in(statusList))
		).limit(top, skip);
		Result result = db.run(select);
		return result.listOf(MediaStore.class);
	}

	public void updateMediaStatus(String id, MediaStatus status) {
		CqnUpdate update = Update.entity(MediaStore_.class).data(MediaStore.STATUS, status.name()).byId(id);
        Result result = db.run(update);
	}

	public void deleteByDirectoryNameAndDirectoryId(Integer directoryId, MediaDirectory directoryName) {
		CqnDelete delete = Delete.from(MediaStore_.class).where(m -> 
			m.get(MediaStore.DIRECTORY_NAME).eq(directoryName.name())
			.and(m.get(MediaStore.DIRECTORY_ID).eq(directoryId))
		);
        Result result = db.run(delete);
	}
	
}
