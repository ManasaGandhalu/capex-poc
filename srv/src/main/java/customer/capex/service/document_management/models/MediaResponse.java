package customer.capex.service.document_management.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MediaResponse {

	@JsonProperty("Status")
	private int status;
	
	@JsonProperty("Data")
	private Media data;
	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Media getData() {
		return data;
	}

	public void setData(Media data) {
		this.data = data;
	}
	
}

