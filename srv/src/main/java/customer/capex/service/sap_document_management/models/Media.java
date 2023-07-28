package customer.capex.service.sap_document_management.models;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Ankit.Pundhir
 *
 */
public class Media {
	
	@JsonProperty("Id")
	private String id = UUID.randomUUID().toString();
	
	@JsonIgnore
	private byte[] bytes;
	
	@JsonProperty("Name")
	private String name;
	
	@JsonProperty("ContentType")
	private String contentType;
	
	@JsonProperty("Url")
	private String url;
	
	@JsonProperty("ContentLength")
	private long contentLength;
	
	
	public Media() {
	}
	
	public Media(String fileName, String contentType, byte[] bytes) {
		this.setName(fileName);
		this.setContentType(contentType);
		this.setBytes(bytes);
	}

	public Media(MultipartFile file) throws IOException {
		this.setName(file.getOriginalFilename());
		this.setContentType(file.getContentType());
		this.setBytes(file.getBytes());
	}

	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
		this.contentLength = bytes != null ? bytes.length : 0;
	}

	public String getName() {
		if(StringUtils.isEmpty(name)) {
			name = id;
		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@JsonIgnore
	public InputStream getInputStream() {
		InputStream inputStream = new ByteArrayInputStream(this.getBytes());
		return inputStream;
	}

	 
	public long getContentLength() {
		return contentLength;
	}

	public void setContentLength(long contentLength) {
		this.contentLength = contentLength;
	}
	
	
}

