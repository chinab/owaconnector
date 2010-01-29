package com.owaconnector.domain;

import org.junit.Test;
import org.springframework.roo.addon.test.RooIntegrationTest;
import org.springframework.test.context.ContextConfiguration;

@RooIntegrationTest(entity = CalendarUser.class)
@ContextConfiguration(locations = { "/test-datasource.xml" })
public class CalendarUserIntegrationTest {

	@Test
	public void testMarkerMethod() {
	}
}
