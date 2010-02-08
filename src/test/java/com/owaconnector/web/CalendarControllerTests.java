package com.owaconnector.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.ui.ModelMap;

import com.owaconnector.exception.NoCalendarFoundException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-context.xml" })
public class CalendarControllerTests {

	@Autowired
	private CalendarController controller;

	@Test(expected = NoCalendarFoundException.class)
	public void testCalendarNoCalendarFound() throws Exception {

		HttpServletRequest request = new MockHttpServletRequest();
		HttpServletResponse response = new MockHttpServletResponse();

		ModelMap modelMap = new ModelMap();
		String encryptionKey = "1234";
		String token = "asdfasdf";
		controller.getCalendar(token, encryptionKey, modelMap, request,
				response);
	}

}
