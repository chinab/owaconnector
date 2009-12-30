package com.owaconnector.server.service;

import com.owaconnector.model.CalendarConfiguration;

public interface CalendarService {

	/**
	 * Get Calendar through ExchangeSessionFactory for a specific configuration 
	 * @param config Exchange configuration
	 * @param decodedPassword plain text password
	 * @return StringBuilder containing the calendar events as iCal 
	 */
	public abstract StringBuilder getCalendar(CalendarConfiguration config, String decodedPassword);

}