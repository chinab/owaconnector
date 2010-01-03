package com.owaconnector.service;

import com.owaconnector.domain.CalendarConfiguration;

public interface CalendarService {

	/**
	 * Get Calendar through ExchangeSessionFactory for a specific configuration 
	 * @param config Exchange configuration
	 * @param decodedPassword plain text password
	 * @return StringBuilder containing the calendar events as iCal 
	 */
	public abstract StringBuilder getCalendar(CalendarConfiguration config, String decodedPassword);

}