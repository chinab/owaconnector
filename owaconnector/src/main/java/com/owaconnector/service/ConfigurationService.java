package com.owaconnector.service;

import com.owaconnector.domain.CalendarConfiguration;

public interface ConfigurationService {

	/**
	 * Get calendarconfiguration for user supplied token
	 * @param token user supplied token
	 * @return CalendarConfiguration configuration to obtain calendar information
	 * @throws Exception 
	 */
	public abstract CalendarConfiguration getConfigurationForToken(String token) throws Exception;

}