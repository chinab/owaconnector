package com.owaconnector.test.integration;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import com.owaconnector.server.controller.CalendarController;
import com.owaconnector.test.ImplTestCase;

public class TestIntegrationCalendarControllerImpl extends ImplTestCase {

	@Autowired
	private CalendarController OwaConnectorController;


	@Test
	public void testCalendarControllerInalidRequestAndResponse() throws Exception  {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter(OwaConnectorController.TOKEN, "1234");
		MockHttpServletResponse response = new MockHttpServletResponse();

		ModelAndView modelAndView = OwaConnectorController.handleRequest(request,
				response);
		Assert.assertNotNull(modelAndView);
		Assert.assertEquals("calendar", modelAndView.getViewName());
		Map<?, ?> model = modelAndView.getModel();
		Assert.assertNotNull(model);
		Object calendar = model.get("calendar");
		Assert.assertNotNull(calendar);
	}
}
