package com.owaconnector.test.integration;

import java.security.KeyPair;
import java.util.Map;

import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.owaconnector.model.User;
import com.owaconnector.server.controller.OwaConnectorController;
import com.owaconnector.server.service.PasswordService;
import com.owaconnector.server.service.UserService;
import com.owaconnector.test.ImplTestCase;

public class TestIntegrationUserConfigEncryption extends ImplTestCase {

	@Autowired
	private UserService userService;

	@Autowired
	private PasswordService passwordService;

	@Autowired
	private OwaConnectorController owaConnectorController;

	public void testCalendarControllerWithUser() throws Exception, Throwable {
		String username = "username";
		User user = new User(username, "password", "email@address.com",
				"displayName");
		userService.createUser(user);
		User user2 = userService.getUser(username);
		KeyPair keyPair = passwordService.generateKeys();
		String privateKey = passwordService.getStringRepresentation(keyPair
				.getPrivate());

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("key", privateKey);
		ModelAndView modelAndView = owaConnectorController.get(request, null);
		expectNullModel(modelAndView);

	}

	private void expectNullModel(ModelAndView modelAndView) {
		Assert.assertNotNull(modelAndView);
		Assert.assertNull(modelAndView.getViewName());
		Map<?, ?> model = modelAndView.getModel();
		Assert.assertTrue(model.isEmpty());
		Assert.assertFalse(model.containsKey("calendar"));
	}
}
