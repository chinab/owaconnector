package com.owaconnector.service.impl;

import javax.persistence.Query;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.owaconnector.domain.CalendarConfiguration;
import com.owaconnector.service.ConfigurationService;

@Service
public class ConfigurationServiceImpl implements ConfigurationService {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.owaconnector.server.service.ConfigurationService#getConfigurationForToken
	 * (java.lang.String)
	 */
	public CalendarConfiguration getConfigurationForToken(String token){
		Assert.notNull(token, "Token is a required parameter");
		 Query query = CalendarConfiguration
			.findCalendarConfigurationsByTokenEquals(token);
		return (CalendarConfiguration)query.getSingleResult();
	}
}
