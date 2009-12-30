package com.owaconnector.test.integration;

import java.net.URI;
import java.security.KeyPair;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import com.owaconnector.model.CalendarConfiguration;
import com.owaconnector.model.User;
import com.owaconnector.server.controller.CalendarController;
import com.owaconnector.server.service.ConfigurationService;
import com.owaconnector.server.service.PasswordService;
import com.owaconnector.server.service.UserService;
import com.owaconnector.test.StubTestCase;


public class TestIntegrationCalendarControllerStubs extends StubTestCase {
	@Autowired
	private CalendarController owaConnectorController;

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private UserService userService;
	
	@Autowired
	private PasswordService passwordService;

	@Test
	public void testCalendarControllerRightToken() throws Exception{
		User user = new User("test","test", "test","test");
		userService.createUser(user);
		KeyPair keyPair = passwordService.generateKeys();
		
		String password = "password";
		byte[] encryptedPassword = passwordService.encrypt(password .getBytes(), keyPair.getPublic());
		String privateKeyAsString = passwordService.getStringRepresentation(keyPair.getPrivate());
		
		CalendarConfiguration config = new CalendarConfiguration("username",encryptedPassword,new URI("http://owa.everest.nl/exchange"), new Integer(30), user);
		
		configurationService.saveCalendarConfiguration(config);
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("token", 	config.getToken());
		request.addParameter("key", privateKeyAsString);
		MockHttpServletResponse response = new MockHttpServletResponse();

		ModelAndView modelAndView = owaConnectorController.handleRequest(request, response);
		expectModel(modelAndView);
	}

	private void expectModel(ModelAndView modelAndView) {
		Assert.assertNotNull(modelAndView);
		Assert.assertEquals("calendar",modelAndView.getViewName());
		Map<?,?> model = modelAndView.getModel();
		Assert.assertNotNull(model);
		Object calendar = model.get("calendar");
		Assert.assertNotNull(calendar);
	}
	@Test
	public void testCalendarControllerWrongToken() throws Exception{
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("token", "asdf");
		MockHttpServletResponse response = new MockHttpServletResponse();

		ModelAndView modelAndView = owaConnectorController.handleRequest(request, response);
		expectNullModel(modelAndView);
		
	}

	private void expectNullModel(ModelAndView modelAndView) {
		Assert.assertNotNull(modelAndView);
		Assert.assertEquals(modelAndView.getViewName(),"welcome");
		Map<?,?> model = modelAndView.getModel();
		Assert.assertTrue(model.isEmpty());
		Assert.assertFalse(model.containsKey("calendar"));
	}
	@Test
	public void testCalendarControllerWrongGetParam() throws Exception{
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("key", "asdf");
		MockHttpServletResponse response = new MockHttpServletResponse();

		ModelAndView modelAndView = owaConnectorController.handleRequest(request, response);
		expectNullModel(modelAndView);
	}
	public void testCalendarControllerNoGetRequest() throws Exception{
		MockHttpServletResponse response = new MockHttpServletResponse();

		ModelAndView modelAndView = owaConnectorController.handleRequest(null, response);
		expectNullModel(modelAndView);
	}
	public void testCalendarControllerNoResponseObj() throws Exception{
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("key", "asdf");
		ModelAndView modelAndView = owaConnectorController.handleRequest(request,null);
		expectNullModel(modelAndView);
	}
	

}
