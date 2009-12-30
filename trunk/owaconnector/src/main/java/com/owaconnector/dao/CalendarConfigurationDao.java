package com.owaconnector.dao;

import java.util.List;

import com.owaconnector.model.CalendarConfiguration;
import com.owaconnector.model.User;

public interface CalendarConfigurationDao {

	public abstract void deleteConfiguration(CalendarConfiguration config);

	public abstract void saveConfiguration(CalendarConfiguration config);

	public abstract List<CalendarConfiguration> getCalendarConfigurationsForUser(
			User user);

	public abstract CalendarConfiguration getCalendarConfigurationForToken(
			String token) throws Exception;

}
