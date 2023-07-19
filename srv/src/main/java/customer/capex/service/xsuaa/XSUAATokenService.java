package customer.capex.service.xsuaa;

import java.util.Optional;

import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.stereotype.Service;

import com.sap.cloud.security.client.HttpClientException;
import com.sap.cloud.security.client.HttpClientFactory;
import com.sap.cloud.security.config.ClientIdentity;
import com.sap.cloud.security.config.OAuth2ServiceConfiguration;
import com.sap.cloud.security.config.OAuth2ServiceConfigurationBuilder;
import com.sap.cloud.security.xsuaa.client.DefaultOAuth2TokenService;
import com.sap.cloud.security.xsuaa.client.OAuth2TokenService;
import com.sap.cloud.security.xsuaa.client.XsuaaDefaultEndpoints;
import com.sap.cloud.security.xsuaa.tokenflows.XsuaaTokenFlows;

@Service
public class XSUAATokenService {

    public Optional<XsuaaTokenFlows> tokenFlows(String authUrl, String clientId, String clientSecret) {
        XsuaaTokenFlows tokenFlows = null;
        try {
			OAuth2ServiceConfigurationBuilder builder = OAuth2ServiceConfigurationBuilder.forService(com.sap.cloud.security.config.Service.XSUAA);
			OAuth2ServiceConfiguration config = builder.withClientId(clientId)
                                           .withClientSecret(clientSecret)
                                           .withUrl(authUrl).build();

			ClientIdentity clientIdentity = config.getClientIdentity();
			CloseableHttpClient client = HttpClientFactory.create(clientIdentity);
			OAuth2TokenService tokenService = new DefaultOAuth2TokenService(client);
			XsuaaDefaultEndpoints endpointsProvider = new XsuaaDefaultEndpoints(config);
			tokenFlows = new XsuaaTokenFlows(tokenService, endpointsProvider, clientIdentity);
		} catch (HttpClientException e) {
            
        }
        return Optional.ofNullable(tokenFlows);
    }
    
}
