package com.owaconnector.test.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.owaconnector.test.controller.TestController;
import com.owaconnector.test.integration.TestIntegrationCalendarControllerStubs;
import com.owaconnector.test.server.TestCalendarServiceStub;
import com.owaconnector.test.server.TestPasswordService;
import com.owaconnector.test.server.TestUserService;

@RunWith(Suite.class)
@SuiteClasses(value = {
 		TestCalendarServiceStub.class,
 		TestController.class,
 		TestIntegrationCalendarControllerStubs.class,
 		TestPasswordService.class,
 		TestUserService.class,
})
public class TestSuite {
} 