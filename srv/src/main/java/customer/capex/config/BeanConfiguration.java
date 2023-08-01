package customer.capex.config;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.servlet.DispatcherType;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;
import com.sap.hcp.cf.logging.servlet.filter.RequestLoggingFilter;

@Configuration
public class BeanConfiguration {
    
    private static final int TIMEOUT_1_MIN = 60000; //60*1000 milliseconds

	@Bean
	public RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplate();

		// Json Serialize/Deserialize
		MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter();
		jacksonConverter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
		restTemplate.getMessageConverters().add(jacksonConverter);
		
		// For HTTP request methods like PATCH
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setConnectTimeout(TIMEOUT_1_MIN);
		requestFactory.setReadTimeout(TIMEOUT_1_MIN);
		restTemplate.setRequestFactory(requestFactory);
	   
		return restTemplate;
	}

	@Bean
	public FilterRegistrationBean<RequestLoggingFilter> loggingFilter() {
		FilterRegistrationBean<RequestLoggingFilter> filterRegistrationBean = new FilterRegistrationBean<>();
		filterRegistrationBean.setFilter(new RequestLoggingFilter());
		filterRegistrationBean.setName("request-logging");
		filterRegistrationBean.addUrlPatterns("/*");
		filterRegistrationBean.setDispatcherTypes(DispatcherType.REQUEST);
		filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return filterRegistrationBean;
	}
	
// 	@Bean
// public FilterRegistrationBean loggingFilter() {
// 	FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
// 	filterRegistrationBean.setFilter(new RequestLoggingFilter());
// 	filterRegistrationBean.setName("request-logging");
// 	filterRegistrationBean.addUrlPatterns("/*");
// 	filterRegistrationBean.setDispatcherTypes(DispatcherType.REQUEST);
// 	return filterRegistrationBean;
// }


}
