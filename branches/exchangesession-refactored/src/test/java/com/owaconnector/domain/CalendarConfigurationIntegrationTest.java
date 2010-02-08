package com.owaconnector.domain;

import org.junit.Test;
import org.springframework.roo.addon.test.RooIntegrationTest;
import org.springframework.test.context.ContextConfiguration;

@RooIntegrationTest(entity = CalendarConfiguration.class)
@ContextConfiguration(locations = {
		"classpath:/META-INF/spring/applicationContext.xml",
		"/test-datasource.xml" })
public class CalendarConfigurationIntegrationTest {

	@Test
	public void testMarkerMethod() {

	}
}
