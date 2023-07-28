package customer.capex.service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import cds.gen.capex.MediaStore;
import customer.capex.repository.MediaStoreRepository;
import customer.capex.service.sap_document_management.SAPDocumentService;
import customer.capex.service.sap_document_management.enums.MediaDirectory;
import customer.capex.service.sap_document_management.enums.MediaStatus;
import customer.capex.service.sap_document_management.models.Media;
import customer.capex.service.sap_document_management.models.MediaResponse;

@Service
public class MediaStoreService {

	@Autowired(required = false)
	SAPDocumentService documentService;

	@Autowired
	MediaStoreRepository mediaStoreRepository;
	
	private ExecutorService executorService = Executors.newFixedThreadPool(5);

	public MediaStore upload(MediaDirectory directoryName, String directoryId, Media media) {
		if (media.getContentLength() > 0) {

			MediaStore mediaStore = mediaStoreRepository.findByDirectoryNameAndDirectoryId(directoryName, directoryId);
			if (mediaStore != null) {
				// copy saved media id and update fields
				media.setId(mediaStore.getMediaId());
				mediaStore.setMediaName(media.getName());
				mediaStore.setContentType(media.getContentType());
				mediaStore.setStatus(MediaStatus.PENDING.name());
				mediaStore.setIsArchived(false);
				mediaStore.setContent(media.getBytes());
				mediaStore.setUrl(media.getUrl());
				mediaStore.setMediaSize(media.getContentLength());
			} else {
				mediaStore = createMediaStore(directoryName, directoryId, media);
			}

			// upload
			mediaStore = mediaStoreRepository.save(mediaStore);
			if (documentService.enabled()) {
				uploadAsync(mediaStore, directoryName, directoryId, media);
			} else {
				mediaStore.setStatus(MediaStatus.UPLOADED.name());
			}
			return mediaStoreRepository.save(mediaStore);
		} else {
			return delete(directoryName, directoryId);
		}
	}

	private MediaStore createMediaStore(MediaDirectory directoryName, String directoryId, Media media) {
		MediaStore mediaStore = MediaStore.create();
		mediaStore.setId(UUID.randomUUID().toString());
		mediaStore.setDirectoryName(directoryName.name());
		mediaStore.setDirectoryId(directoryId);
		mediaStore.setMediaId(media.getId());
		mediaStore.setMediaName(media.getName());
		mediaStore.setContent(media.getBytes());
		mediaStore.setContentType(media.getContentType());
		mediaStore.setUrl(media.getUrl());
		mediaStore.setMediaSize(media.getContentLength());
		return mediaStore;
	}

	public Media mediaStoreToMedia(MediaStore mediaStore) {
		Media media = new Media();
		media.setId(mediaStore.getMediaId());
		media.setName(mediaStore.getMediaName());
		media.setContentType(mediaStore.getContentType());
		media.setBytes(mediaStore.getContent());
		media.setUrl(mediaStore.getUrl());
		return media;
	}

	private void uploadAsync(MediaStore mediaStore, MediaDirectory directoryName, String directoryId, Media media) {
		executorService.submit(() -> {
			MediaResponse mediaResponse = documentService.uploadDocument(directoryName, directoryId, media);
			if (mediaResponse.getStatus() == HttpStatus.CREATED.value()) {
				// update media id
				mediaStore.setMediaId(mediaResponse.getData().getId());
				mediaStore.setStatus(MediaStatus.UPLOADED.name());
				// update the url
				mediaStore.setUrl(mediaResponse.getData().getUrl());
				// clear the content as binary content will be stored on object store
				mediaStore.setContent(null);
			} else {
				mediaStore.setStatus(MediaStatus.UPLOADED.name());
			}
			mediaStoreRepository.save(mediaStore);
		});
	}



	public MediaStore download(MediaDirectory directoryName, String directoryId) {
		MediaStore mediaStore = mediaStoreRepository.findByDirectoryNameAndDirectoryId(directoryName, directoryId);
		if (mediaStore != null && !mediaStore.getIsArchived() && (MediaStatus.UPLOADED.name().equals(mediaStore.getStatus())
				|| MediaStatus.SYNCING.name().equals(mediaStore.getStatus()))) {
			if (documentService.enabled()) {
				MediaResponse mediaResponse = documentService.downloadDocument(directoryName, directoryId,
						mediaStore.getMediaId());
				if (mediaResponse.getStatus() == HttpStatus.OK.value()) {
					Media media = mediaResponse.getData();
					// update the content and url
					mediaStore.setContent(media.getBytes());
					mediaStore.setUrl(media.getUrl());
				}
			}
			return mediaStore;
		} else if(mediaStore != null && !mediaStore.getIsArchived() && MediaStatus.PENDING.name().equals(mediaStore.getStatus())) {
			return mediaStore;
		}

		return null;
	}
	
	public MediaStore downloadMetadata(MediaDirectory directoryName, String directoryId) {
		MediaStore mediaStore = mediaStoreRepository.findByDirectoryNameAndDirectoryId(directoryName, directoryId);
		if (mediaStore != null && !mediaStore.getIsArchived() && (MediaStatus.UPLOADED.name().equals(mediaStore.getStatus())
				|| MediaStatus.SYNCING.name().equals(mediaStore.getStatus()))) {
			mediaStore.setContent(null); // content is not part of metadata
			return mediaStore;
		} else if(mediaStore != null && !mediaStore.getIsArchived() && MediaStatus.PENDING.name().equals(mediaStore.getStatus())) {
			mediaStore.setContent(null); // content is not part of metadata
			return mediaStore;
		}

		return null;
	}

	public MediaStore delete(MediaDirectory directoryName, String directoryId) {
		MediaStore mediaStore = mediaStoreRepository.findByDirectoryNameAndDirectoryId(directoryName, directoryId);
		if (mediaStore != null && !mediaStore.getIsArchived() && MediaStatus.UPLOADED.name().equals(mediaStore.getStatus())) {
			if (documentService.enabled()) {
				MediaResponse mediaResponse = documentService.deleteDocument(directoryName, directoryId,
						mediaStore.getMediaId());
				if (mediaResponse.getStatus() == HttpStatus.ACCEPTED.value()) {
					mediaStore.setStatus(MediaStatus.DELETED.name());
					mediaStore.setIsArchived(true);
					if (mediaResponse.getData() != null) {
						// update media id
						mediaStore.setMediaId(mediaResponse.getData().getId());
						// update the url
						mediaStore.setUrl(mediaResponse.getData().getUrl());
					}
					return mediaStoreRepository.save(mediaStore);
				}
			} else {
				mediaStore.setStatus(MediaStatus.DELETED.name());
				mediaStore.setIsArchived(true);
				return mediaStoreRepository.save(mediaStore);
			}
			return mediaStore;
		}

		return null;
	}

	public void cloudSync(int cloudSyncMaxRecords) {
		if (documentService.enabled()) {
			List<MediaStatus> statusList = Arrays.asList(MediaStatus.UPLOADED, MediaStatus.DELETED);

			List<MediaStore> mediaStores = mediaStoreRepository.findAllSyncableRecords(statusList, 0, cloudSyncMaxRecords);

			if (mediaStores != null && !mediaStores.isEmpty()) {

				// set status to SYNCING to prevent resync
				for (MediaStore mediaStore : mediaStores) {
					mediaStoreRepository.updateMediaStatus(mediaStore.getId(), MediaStatus.SYNCING);
				}

				// sync with cloud
				for (MediaStore mediaStore : mediaStores) {
					Media media = mediaStoreToMedia(mediaStore);
					MediaStatus status = MediaStatus.valueOf(mediaStore.getStatus());
					MediaDirectory mediaDirectory = MediaDirectory.valueOf(mediaStore.getDirectoryName());
					MediaResponse mediaResponse = documentService.uploadDocument(mediaDirectory,
							mediaStore.getDirectoryId(), media);
					if (mediaStore.getIsArchived() || MediaStatus.DELETED.equals(status)) {
						mediaResponse = documentService.deleteDocument(mediaDirectory,
								mediaStore.getDirectoryId(), media.getId());
					}

					if (mediaResponse.getStatus() == HttpStatus.CREATED.value()) {
						// update media id
						mediaStore.setMediaId(mediaResponse.getData().getId());
						// update the url
						mediaStore.setUrl(mediaResponse.getData().getUrl());
						// clear the content as binary content will be stored on object store
						mediaStore.setContent(null);
						mediaStore.setStatus(status.name());
						mediaStoreRepository.save(mediaStore);
					} else if (mediaResponse.getStatus() == HttpStatus.ACCEPTED.value()) {
						// clear the content as binary content will be stored on object store
						mediaStore.setContent(null);
						if (mediaResponse.getData() != null) {
							// update media id
							mediaStore.setMediaId(mediaResponse.getData().getId());
							// update the url
							mediaStore.setUrl(mediaResponse.getData().getUrl());
						}
						mediaStore.setStatus(status.name());
						mediaStoreRepository.save(mediaStore);
					} else {
						// revert with previous status
						mediaStoreRepository.updateMediaStatus(mediaStore.getId(), status);
					}
				}
			}
		}
	}

}
