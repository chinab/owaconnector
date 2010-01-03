package com.owaconnector.domain;

import junit.framework.Assert;

import org.springframework.roo.addon.test.RooIntegrationTest;
import com.owaconnector.domain.CalendarConfiguration;
import org.junit.Test;

@RooIntegrationTest(entity = CalendarConfiguration.class)
public class CalendarConfigurationIntegrationTest {

	@Test
	public void testMarkerMethod() {
		CalendarConfiguration config = new CalendarConfiguration();
		String token = "abcd";
		config.setToken(token);
		config.persist();
		CalendarConfiguration result = (CalendarConfiguration)CalendarConfiguration.findCalendarConfigurationsByTokenEquals(token)
				.getSingleResult();
		Assert.assertNotNull("Result may not be null",result);
		Assert.assertEquals("Token from result does not equal the token from persisted entity",result.getToken(), token);
	}
}
