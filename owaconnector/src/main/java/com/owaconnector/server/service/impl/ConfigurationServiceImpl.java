package com.owaconnector.server.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.owaconnector.dao.CalendarConfigurationDao;
import com.owaconnector.model.CalendarConfiguration;
import com.owaconnector.model.User;
import com.owaconnector.server.service.ConfigurationService;

@Service
public class ConfigurationServiceImpl implements ConfigurationService {

	@Autowired
	private CalendarConfigurationDao calendarConfigurationDao;

	/**
	 * @return the calendarConfigurationDao
	 */
	public CalendarConfigurationDao getCalendarConfigurationDao() {
		return calendarConfigurationDao;
	}

	/**
	 * @param calendarConfigurationDao the calendarConfigurationDao to set
	 */
	public void setCalendarConfigurationDao(
			CalendarConfigurationDao calendarConfigurationDao) {
		this.calendarConfigurationDao = calendarConfigurationDao;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.owaconnector.server.service.ConfigurationService#getConfigurationForToken
	 * (java.lang.String)
	 */
	public CalendarConfiguration getConfigurationForToken(String token) throws Exception {
		Assert.notNull(token, "Token is a required parameter");
		return calendarConfigurationDao.getCalendarConfigurationForToken(token);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.owaconnector.server.service.ConfigurationService#
	 * deleteCalendarConfiguration(com.owaconnector.model.User,
	 * com.owaconnector.model.CalendarConfiguration)
	 */
	public void deleteCalendarConfiguration(CalendarConfiguration config) {
		Assert.notNull(config, "Config is a required parameter");
		calendarConfigurationDao.deleteConfiguration(config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.owaconnector.server.service.ConfigurationService#
	 * saveCalendarConfiguration(com.owaconnector.model.User,
	 * com.owaconnector.model.CalendarConfiguration)
	 */
	public void saveCalendarConfiguration(CalendarConfiguration config) {
		Assert.notNull(config, "Config is a required parameter");
		calendarConfigurationDao.saveConfiguration(config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.owaconnector.server.service.ConfigurationService#getConfigurationsForUser
	 * (com.owaconnector.model.User)
	 */
	public List<CalendarConfiguration> getConfigurationsForUser(User user) {
		Assert.notNull(user, "User is a required parameter");
		return calendarConfigurationDao.getCalendarConfigurationsForUser(user);
	}

}
