package com.owaconnector.test.server;

import java.net.URI;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.owaconnector.model.CalendarConfiguration;
import com.owaconnector.model.User;
import com.owaconnector.server.service.CalendarService;
import com.owaconnector.test.StubTestCase;

public class TestCalendarServiceStub extends StubTestCase {
	@Autowired
	private CalendarService calendarService;

	@Test
	public void testCalendarService1() {
		String username = "asdfasdf";
		byte[] password = "asdf".getBytes();
			URI url = URI.create("https://webmail.exchange.com");
		int maxDaysInPast = 30;

		String userUsername = "TestDuplicateUser";
		User user = new User(userUsername,"TestPassword","TestEmail","TestDisplay");
		
		CalendarConfiguration config = new CalendarConfiguration(username,
				password, url, maxDaysInPast, user);
		StringBuilder calendar = calendarService.getCalendar(config, new String("asdf"));
		Assert.assertNotNull(calendar);
		// Log.debug(calendar.toString());

	}
	public void testCalendarServiceWrongUrl() {
		String username = "asdfasdf";
		byte[] password = "asdf".getBytes();
		URI url = URI.create("asdf");
		int maxDaysInPast = 30;
		String userUsername = "TestDuplicateUser";
		User user = new User(userUsername,"TestPassword","TestEmail","TestDisplay");
		
		CalendarConfiguration config = new CalendarConfiguration(username,
				password, url, maxDaysInPast, user);
		StringBuilder calendar = calendarService.getCalendar(config, new String("asdf"));
		Assert.assertNotNull(calendar);
		// Log.debug(calendar.toString());

	}
}
