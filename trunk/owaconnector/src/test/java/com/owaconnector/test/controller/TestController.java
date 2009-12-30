package com.owaconnector.test.controller;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import com.owaconnector.server.controller.OwaConnectorController;
import com.owaconnector.test.ImplTestCase;

public class TestController extends ImplTestCase{
	@Autowired
	private OwaConnectorController owaConnectorController;
	
	@Test
	public void testOwaControllerNoToken(){
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		ModelAndView modelAndView = owaConnectorController.get(request, response);
		Assert.assertNotNull(modelAndView);
		Assert.assertEquals(modelAndView.getViewName(),"welcome");
		Map<?, ?> model = modelAndView.getModel();
		Assert.assertTrue(model.isEmpty());
		Assert.assertFalse(model.containsKey("calendar"));
		
	}
	
	@Test
	public void testOwaControllerUnknownToken(){
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("token", "asdf");
		MockHttpServletResponse response = new MockHttpServletResponse();
		ModelAndView modelAndView = owaConnectorController.get(request, response);
		Assert.assertNotNull(modelAndView);
		Assert.assertEquals(modelAndView.getViewName(),"welcome");
		Map<?, ?> model = modelAndView.getModel();
		Assert.assertTrue(model.isEmpty());
		Assert.assertFalse(model.containsKey("calendar"));
		
	}
	@Test
	public void testOwaControllerUnknownGetParam(){
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("bla", "asdf");
		MockHttpServletResponse response = new MockHttpServletResponse();
		ModelAndView modelAndView = owaConnectorController.get(request, response);
		Assert.assertNotNull(modelAndView);
		Assert.assertEquals(modelAndView.getViewName(),"welcome");
		Map<?, ?> model = modelAndView.getModel();
		Assert.assertTrue(model.isEmpty());
		Assert.assertFalse(model.containsKey("calendar"));
		
	}
}
