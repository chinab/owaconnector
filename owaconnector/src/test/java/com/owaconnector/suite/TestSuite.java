package com.owaconnector.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.owaconnector.domain.CalendarConfigurationDataOnDemand;
import com.owaconnector.domain.CalendarConfigurationIntegrationTest;
import com.owaconnector.domain.CalendarUserIntegrationTest;
import com.owaconnector.service.PasswordServiceTest;

@RunWith(Suite.class)
@SuiteClasses(value = {
 		CalendarConfigurationDataOnDemand.class,
 		CalendarConfigurationIntegrationTest.class,
 		CalendarUserIntegrationTest.class,
 		PasswordServiceTest.class,
})
public class TestSuite {
} 