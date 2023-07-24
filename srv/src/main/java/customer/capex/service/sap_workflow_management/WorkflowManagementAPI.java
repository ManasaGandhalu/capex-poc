package customer.capex.service.sap_workflow_management;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sap.cloud.security.xsuaa.client.OAuth2TokenResponse;
import com.sap.cloud.security.xsuaa.tokenflows.TokenFlowException;
import com.sap.cloud.security.xsuaa.tokenflows.XsuaaTokenFlows;

import customer.capex.service.sap_workflow_management.enums.WorkflowStatus;
import customer.capex.service.sap_workflow_management.models.CancelWorkflowInstance;
import customer.capex.service.sap_workflow_management.models.DeleteWorkflowInstance;
import customer.capex.service.sap_workflow_management.models.WorkflowInstance;
import customer.capex.service.sap_workflow_management.models.WorkflowRequest;
import customer.capex.service.sap_workflow_management.models.WorkflowUserTask;
import customer.capex.service.sap_xsuaa.XSUAATokenService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WorkflowManagementAPI {

    @Value("${sap_workflow_management.auth_url:}")
	private String authUrl;

	@Value("${sap_workflow_management.api_url:}")
	private String apiUrl;

	@Value("${sap_workflow_management.usertask_url:}")
	private String usertaskUrl;

	@Value("${sap_workflow_management.client_id:}")
	private String clientId;

	@Value("${sap_workflow_management.client_secret:}")
	private String clientSecret;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	XSUAATokenService xsuaaTokenService;

	public ResponseEntity<WorkflowInstance> initiate(WorkflowRequest<?> workflowRequest) {
		if (workflowRequest == null) {
			return null;
		}
		OAuth2TokenResponse xsuaaToken = getAccessToken();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(xsuaaToken.getAccessToken());
		HttpEntity<?> entity = new HttpEntity<>(workflowRequest, headers);
		ResponseEntity<WorkflowInstance> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, WorkflowInstance.class);
		return response;
	}

    public ResponseEntity<String> deleteWorkflowInstancesById(List<String> wfInstanceIds) {
		if (wfInstanceIds == null || wfInstanceIds.isEmpty()) {
			return null;
		}
		List<DeleteWorkflowInstance> instances = new ArrayList<>();
		wfInstanceIds.forEach(wfInstanceId -> {
			DeleteWorkflowInstance wfInstance = new DeleteWorkflowInstance(wfInstanceId);
			instances.add(wfInstance);
		});
		
		OAuth2TokenResponse xsuaaToken = getAccessToken();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(xsuaaToken.getAccessToken());
		HttpEntity<?> entity = new HttpEntity<>(instances, headers);
		ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.PATCH, entity, String.class);
        log.info("DeleteWorkflowInstance: {}, Status: {}", wfInstanceIds, response.getStatusCode());
		return response;
	}

	public void deleteWorkflowInstancesByStatus(List<String> statuses) {
		while(true) {
			try {
				List<WorkflowInstance> instances = getWorkflowInstances(statuses, 100, 0);
				if(instances.isEmpty()) {
					// terminate the loop
					log.info("DeleteWorkflowInstance Job Completed");
					break;
				} else {
					List<String> ids = instances.stream().map(i -> i.getId()).collect(Collectors.toList());
					deleteWorkflowInstancesById(ids);
				}
				Thread.sleep(60000); //1 minute wait
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				log.error("ERROR", e);
			}
		}
	}
    
    public List<WorkflowInstance> getWorkflowInstances(List<String> statuses, Integer top, Integer skip) {
    	List<WorkflowInstance> instances = new ArrayList<>();
    	if (statuses == null || statuses.isEmpty()) {
			return instances;
		}
        OAuth2TokenResponse xsuaaToken = getAccessToken();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(xsuaaToken.getAccessToken());
		HttpEntity<?> entity = new HttpEntity<>(null, headers);
		String statusFilter = "";
		for(int i = 0; i < statuses.size(); i++) {
			statusFilter += "&status=" + statuses.get(i);
		}
		String url = apiUrl + "?parentInstanceId=null&$orderby=startedAt asc" + "&$top=" + top + "&$skip=" + skip;
		if(statusFilter.length() > 0) {
			url += statusFilter;
		}
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        log.info("GetWorkflowInstances: {}, Status: {}", url, response.getStatusCode());
		if(response.hasBody()) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				JavaTimeModule javaTimeModule = new JavaTimeModule();
				mapper.registerModule(javaTimeModule);
				mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				mapper.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
				instances = mapper.readValue(response.getBody(), new TypeReference<List<WorkflowInstance>>(){});
			} catch (JsonProcessingException e) {
				log.error("ERROR", e);
			}
		}
        return instances;
	}

	private OAuth2TokenResponse getAccessToken() {
		OAuth2TokenResponse tokenResponse = null;
		XsuaaTokenFlows tokenFlows = xsuaaTokenService.tokenFlows(authUrl, clientId, clientSecret).get();
		if (tokenFlows != null) {
			try {
				tokenResponse = tokenFlows.clientCredentialsTokenFlow().execute();
			} catch (IllegalArgumentException | TokenFlowException e) {
				e.printStackTrace();
			}
		}
		return tokenResponse;
	}

	public WorkflowInstance getActiveUserTask(String workflowDefinitionId, String workflowInstanceId) {
		String url = usertaskUrl + "?" + "workflowDefinitionId=" + workflowDefinitionId + "&"
		+ "workflowInstanceIdworkflowDefinitionId=" + workflowInstanceId + "&" + "status=READY";
		
		OAuth2TokenResponse xsuaaToken = getAccessToken();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(xsuaaToken.getAccessToken());
		HttpEntity<?> entity = new HttpEntity<>(headers);

		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
		log.info("getActiveUserTask: {}, Status: {}", url, response.getStatusCode());
		if(response.hasBody()) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				JavaTimeModule javaTimeModule = new JavaTimeModule();
				mapper.registerModule(javaTimeModule);
				mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				mapper.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
				List<WorkflowInstance> instances = mapper.readValue(response.getBody(), new TypeReference<List<WorkflowInstance>>(){});
				if(instances != null && !instances.isEmpty()) {
					return instances.get(0);
				}
			} catch (JsonProcessingException e) {
				log.error("ERROR", e);
			}
		}

		return null;
	}

	public ResponseEntity<String> cancelWorkflowInstance(String workflowInstanceId) {
		String url = apiUrl+"/"+workflowInstanceId;
		
		OAuth2TokenResponse xsuaaToken = getAccessToken();
		CancelWorkflowInstance instance = new CancelWorkflowInstance(true);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(xsuaaToken.getAccessToken());
		HttpEntity<?> entity = new HttpEntity<>(instance, headers);

		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PATCH, entity, String.class);
		return response;
	}

	public ResponseEntity<String> patchUserTask(String userTaskId, WorkflowUserTask instance) {
		String url = usertaskUrl + "/" + userTaskId;
		
		OAuth2TokenResponse xsuaaToken = getAccessToken();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(xsuaaToken.getAccessToken());
		HttpEntity<?> entity = new HttpEntity<>(instance, headers);

		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PATCH, entity, String.class);
		return response;
	}


}