package com.owaconnector.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.owaconnector.domain.CalendarConfigurationIntegrationTest;
import com.owaconnector.domain.CalendarUserIntegrationTest;
import com.owaconnector.security.SecurityTests;
import com.owaconnector.service.PasswordServiceTest;
import com.owaconnector.service.UserDetailsServiceTest;
import com.owaconnector.web.CalendarConfigurationControllerTests;
import com.owaconnector.web.CalendarControllerTests;

@RunWith(Suite.class)
@SuiteClasses(value = { CalendarConfigurationIntegrationTest.class,
		CalendarUserIntegrationTest.class, PasswordServiceTest.class,
		SecurityTests.class, CalendarControllerTests.class,
		CalendarConfigurationControllerTests.class,
		UserDetailsServiceTest.class })
public class TestSuite {
}