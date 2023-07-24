package customer.capex.service.sap_document_management.config;

import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class CMISBeanConfig {
    
	@Bean
	@Primary
	public SessionFactory sessionFactory() {
		return SessionFactoryImpl.newInstance();
	}

}
