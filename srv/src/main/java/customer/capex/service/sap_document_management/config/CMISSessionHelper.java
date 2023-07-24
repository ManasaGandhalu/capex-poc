package customer.capex.service.sap_document_management.config;

import java.net.URISyntaxException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.bindings.CmisBindingFactory;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.sap.cloud.security.xsuaa.client.OAuth2TokenResponse;
import com.sap.cloud.security.xsuaa.tokenflows.TokenFlowException;
import com.sap.cloud.security.xsuaa.tokenflows.XsuaaTokenFlows;

import customer.capex.service.sap_xsuaa.XSUAATokenService;

@Configuration
public class CMISSessionHelper {

	@Value("${sap_document_service.enabled:false}")
	public boolean enabled;
	
	@Value("${sap_document_service.api_url:}")
	public String apiUrl;

	@Value("${sap_document_service.repository_id:}")
	public String repositoryId;

	@Value("${sap_document_service.auth_url:}")
	public String authUrl;

	@Value("${sap_document_service.client_id:}")
	public String clientId;

	@Value("${sap_document_service.client_secret:}")
	public String clientSecret;

	@Autowired
	XSUAATokenService xsuaaTokenService;

	@Autowired
	SessionFactory sessionFactory;

	private Session session;

	private Instant sessionExpiry = Instant.now();

	private Map<String, String> parameters = new HashMap<>();

	Logger logger = LoggerFactory.getLogger(CMISSessionHelper.class);

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public Session getSession() throws JSONException, URISyntaxException {
		if (Instant.now().isAfter(sessionExpiry)) {
			try {
				// session is not expired
				session.getRepositoryInfo();
				return session;
			} catch (CmisUnauthorizedException e) {
				logger.error("exception in CMISSessionHelper on session.getRepositoryInfo() = " + e);
			}
		}

		parameters.put(SessionParameter.BINDING_SPI_CLASS, CmisBindingFactory.BINDING_SPI_BROWSER);
		parameters.put(SessionParameter.BROWSER_URL, apiUrl + "browser");
		parameters.put(SessionParameter.BINDING_TYPE, BindingType.BROWSER.value());

		// Need to get Repository ID using API
		parameters.put(SessionParameter.REPOSITORY_ID, repositoryId);

		parameters.put(SessionParameter.AUTH_HTTP_BASIC, "false");
		OAuth2TokenResponse token = getAccessToken();
		parameters.put(SessionParameter.HEADER + ".0", "Authorization: Bearer " + token.getAccessToken());

		sessionExpiry = token.getExpiredAt();
		session = sessionFactory.createSession(parameters);
		return session;
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

}
