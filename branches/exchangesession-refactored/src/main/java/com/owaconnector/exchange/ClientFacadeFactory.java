package com.owaconnector.exchange;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class ClientFacadeFactory {

	@Bean
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public ClientFacade createHttpClientFacade() {
		return new HttpClientFacade();
	}
}
