package customer.capex.service.sap_document_management;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNameConstraintViolationException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import customer.capex.service.sap_document_management.config.CMISSessionHelper;
import customer.capex.service.sap_document_management.enums.MediaDirectory;
import customer.capex.service.sap_document_management.models.Media;
import customer.capex.service.sap_document_management.models.MediaResponse;

@Service
public class SAPDocumentService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SAPDocumentService.class);
	private static final String CMIS_ROOT = "knpl-pragati";
	private static final String CMIS_FOLDER = "cmis:folder";
	private static final String CMIS_DOCUMENT = "cmis:document";
	private static final String ARCHIVED_FOLDER = "archived";
	private static final String FORWARD_SLASH = "/";
	
	@Autowired
	CMISSessionHelper helper;

	/**
	 * Check if SAP Document Service is enabled
	 */
	public boolean enabled() {
		return helper.enabled;
	}

	/**
	 * Upload to SAP Document Service
	 * @param directory
	 * @param directoryId
	 * @param media
	 * @return
	 */
	public MediaResponse uploadDocument(MediaDirectory directory, String directoryId, Media media) {
		MediaResponse mediaResponse = new MediaResponse();
		boolean uploaded = false;
		try {
			Session session = helper.getSession();
			Document document = null;
			archiveDocument(directory, directoryId, media.getId());
			InputStream inputStream = media.getInputStream();
			ContentStream contentStream = session.getObjectFactory().createContentStream(media.getName(), media.getContentLength(), media.getContentType(), inputStream);
			
			Folder root = session.getRootFolder();
			Folder subFolder1 = createSubFolder(root, CMIS_ROOT, session);
			Folder subFolder2 = createSubFolder(subFolder1, directory.name(), session);
			Folder subFolder3 = createSubFolder(subFolder2, directoryId, session);
			
			// create the document
			Map<String, Object> fileProperties = new HashMap<>();
			fileProperties.put(PropertyIds.OBJECT_TYPE_ID, CMIS_DOCUMENT);
			fileProperties.put(PropertyIds.NAME, media.getName());
			
			try {
				document = subFolder3.createDocument(fileProperties, contentStream, null);
			} catch(CmisNameConstraintViolationException e) {
				for(CmisObject object: subFolder3.getChildren()) {
					// empty the directory
					object.delete(true);
				}
				document = subFolder3.createDocument(fileProperties, contentStream, null);
			}
			
			if(document != null) {
				media.setId(document.getId());
				media.setUrl(document.getContentUrl());
				uploaded = true;
			}
			inputStream.close();
		} catch (Exception e) {
			LOGGER.error("Cmis Object Upload Exception Occured: " + media.getId());
			e.printStackTrace();
		}
		
		if(uploaded) {
			mediaResponse.setData(media);
			mediaResponse.setStatus(HttpStatus.CREATED.value());
		} else {
			mediaResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		
		return mediaResponse;
	}
	
	/**
	 * Download from SAP Document Service
	 * @param directory 
	 * @param directoryId
	 * @param objectId 
	 * @return
	 */
	public MediaResponse downloadDocument(MediaDirectory directory, String directoryId, String objectId) {
		MediaResponse mediaResponse = new MediaResponse();
		Media media = null;
		try {
			Document document = getDocumentById(objectId);
			if(document != null) {
				ContentStream cs = document.getContentStream();
				if(cs != null) {
					InputStream is = cs.getStream();
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				    byte[] read_buf = new byte[1024];
				    int read_len = 0;
				    while ((read_len = is.read(read_buf)) > 0) {
				    	outputStream.write(read_buf, 0, read_len);
				    }
				    byte[] bytes = outputStream.toByteArray();
				    is.close();
				    outputStream.close();
				    
				    media = new Media();
					media.setId(document.getId());
					media.setName(document.getName());
					media.setBytes(bytes);
					media.setContentType(document.getContentStreamMimeType());
					media.setUrl(document.getContentUrl());
				}
			}
			
		} catch(CmisObjectNotFoundException e) {
			LOGGER.info("Cmis Object Not Found: " + objectId);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		if(media != null) {
			mediaResponse.setData(media);
			mediaResponse.setStatus(HttpStatus.OK.value());
		} else {
			mediaResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return mediaResponse;
	}
	
	/**
	 * Delete from SAP Document Service
	 * @param objectId
	 * @return
	 */
	public MediaResponse deleteDocument(MediaDirectory directory, String directoryId, String objectId) {
		MediaResponse mediaResponse = new MediaResponse();
		boolean deleted = false;
		try {
			Session session = helper.getSession();
			CmisObject object = session.getObject(objectId);
			if (object.getBaseTypeId().value().equalsIgnoreCase(CMIS_FOLDER)) {
				Folder document = (Folder) object;
				document.delete(true);
			} else {
				Document document = (Document) object;
				document.delete(true);
			}

			deleted = true;
		} catch(CmisObjectNotFoundException e) {
			LOGGER.info("Cmis Object Not Found: " + objectId);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		if(deleted) {
			mediaResponse.setStatus(HttpStatus.ACCEPTED.value());
		} else {
			mediaResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		
		return mediaResponse;
	}
	
	/**
	 * Archive in SAP Document Service
	 * @param directory 
	 * @param directoryId
	 * @param objectId
	 * @return
	 */
	public MediaResponse archiveDocument(MediaDirectory directory, String directoryId, String objectId) {
		MediaResponse mediaResponse = new MediaResponse();
		Media media = new Media();
		boolean archived = false;
		try {
			Session session = helper.getSession();
			Folder root = session.getRootFolder();
			Folder sourceFolder = (Folder) session.getObjectByPath(root.getPath() + FORWARD_SLASH + CMIS_ROOT + FORWARD_SLASH + directory.name() + FORWARD_SLASH + directoryId);
			
			if(sourceFolder != null) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
				
				Folder archivedFolder = createSubFolder(root, ARCHIVED_FOLDER, session);
				Folder targetFolder1 = createSubFolder(archivedFolder, directory.name(), session);
				Folder targetFolder2 = createSubFolder(targetFolder1, directoryId, session);
				Date date = new Date();
				String targetDirectory = sdf.format(date);
				Folder targetFolder3 = createSubFolder(targetFolder2, targetDirectory, session);
				
				try {
					Document document = (Document) session.getObject(objectId);
					document.move(sourceFolder, targetFolder3);
					document.refresh();
					media.setId(document.getId());
					media.setUrl(document.getContentUrl());
					archived = true;
				} catch(Exception e) {
					e.printStackTrace();
				}
				
			}
		} catch(CmisObjectNotFoundException e) {
			LOGGER.info("Cmis Object Not Found: " + objectId);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		if(archived) {
			mediaResponse.setData(media);
			mediaResponse.setStatus(HttpStatus.ACCEPTED.value());
		} else {
			mediaResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		
		return mediaResponse;
	}
	
	private Folder createSubFolder(Folder folder, String name, Session cmisSession) {
		Folder subFolder = null;
		
		Map<String, Object> folderProperties = new HashMap<>();
		folderProperties.put(PropertyIds.OBJECT_TYPE_ID, CMIS_FOLDER);
		folderProperties.put(PropertyIds.NAME, name);
		
		try {
			subFolder = (Folder) cmisSession.getObjectByPath(folder.getPath() + FORWARD_SLASH + name);
		} catch (CmisObjectNotFoundException onfe) {
			subFolder = folder.createFolder(folderProperties);
		}
		return subFolder;
	}
	
	private Document getDocumentById(String objectId) {
		try {
			Session session = helper.getSession();
			CmisObject object = session.getObject(objectId);
			if (object.getBaseTypeId().value().equalsIgnoreCase(CMIS_DOCUMENT)) {
				return (Document) object;
			}
		} catch(CmisObjectNotFoundException e) {
			LOGGER.info("Cmis Object Not Found: " + objectId);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
