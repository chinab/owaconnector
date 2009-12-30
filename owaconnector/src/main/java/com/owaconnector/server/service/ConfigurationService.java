package com.owaconnector.server.service;

import java.util.List;

import com.owaconnector.model.CalendarConfiguration;
import com.owaconnector.model.User;

public interface ConfigurationService {

	/**
	 * Get calendarconfiguration for user supplied token
	 * @param token user supplied token
	 * @return CalendarConfiguration configuration to obtain calendar information
	 * @throws Exception 
	 */
	public abstract CalendarConfiguration getConfigurationForToken(String token) throws Exception;

	/**
	 * Save calendarconfiguration for user
	 * @param user user who owns the calendarconfiguration
	 * @param config the configuration that should be saved 
	 */
	public abstract void saveCalendarConfiguration( CalendarConfiguration config);
	
	/**
	 * Delete the calendarconfiguration for user
	 * @param user user who owns the calendarconfiguration
	 * @param config the calendarconfiguration that should be deleted
	 */
	public abstract void deleteCalendarConfiguration( CalendarConfiguration config);
	/**
	 * Get calendarconfigurations for a specific user
	 * @param user user owning the calendarconfigurations
	 * @return list of calendarconfigurations for the specified user
	 */
	public abstract List<CalendarConfiguration> getConfigurationsForUser(User user);
	
}