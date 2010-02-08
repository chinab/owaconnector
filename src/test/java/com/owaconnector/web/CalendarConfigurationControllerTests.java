package com.owaconnector.web;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DirectFieldBindingResult;

import com.owaconnector.domain.CalendarConfiguration;
import com.owaconnector.domain.CalendarUser;
import com.owaconnector.util.AuthenticationUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-context.xml", "/test-datasource.xml" })
public class CalendarConfigurationControllerTests {

	@Autowired
	private CalendarConfigurationController controller;

	private CalendarConfiguration cc1 = null;
	private CalendarConfiguration cc2 = null;
	private CalendarUser cu1 = null;

	@Before
	public void setUp() {
		AuthenticationUtil.createAuthentication("user1", "xxx", "ROLE_USER");
		cu1 = new CalendarUser();
		cu1.setId(new Long('1'));
		cu1.setUsername("user1");
		cu1.persist();

		cc1 = new CalendarConfiguration();
		cc1.setId(new Long('1'));
		cc1.setDomainName("domain");
		cc1.setMaxDaysInPast(10);
		cc1.setPasswordEncrypted("password");
		cc1.setToken("token");
		cc1.setOwner(cu1);
		cc1.persist();
		SecurityContextHolder.clearContext();
	}

	@After
	public void tearDown() {
		cc1.remove();
		if (cc2 != null) {
			cc2.remove();
		}
		cu1.remove();
		SecurityContextHolder.clearContext();
	}

	@Test
	public void testCalendarConfigurationControllerCreate() {
		AuthenticationUtil.createAuthentication("user1", "xxx", "ROLE_USER");
		createTransientCalendarConfiguration();

		performCreate(this.cc2);
	}

	// @Test(expected = AuthenticationCredentialsNotFoundException.class)
	// public void testCalendarConfigurationControllerCreateUnauthorized() {
	// CalendarConfiguration cc2 = createTransientCalendarConfiguration();
	//
	// performCreate(cc2);
	// }

	@Test
	public void testCalendarConfigurationControllerShow() {
		AuthenticationUtil.createAuthentication("user1", "xxx", "ROLE_USER");
		performShow();

	}

	@Test(expected = AuthenticationCredentialsNotFoundException.class)
	public void testCalendarConfigurationControllerShowUnauthorized() {
		performShow();
	}

	@Test
	public void testCalendarConfigurationControllerDelete() {
		AuthenticationUtil.createAuthentication("user1", "xxx", "ROLE_USER");
		createTransientCalendarConfiguration();
		this.cc2.persist();

		performDelete(this.cc2);
		this.cc2 = null;

	}

	// @Test(expected = AuthenticationCredentialsNotFoundException.class)
	// public void testCalendarConfigurationControllerDeleteUnauthorized() {
	// CalendarConfiguration cc2 = createTransientCalendarConfiguration();
	// cc2.persist();
	//
	// performDelete(cc2);
	//
	// }

	@Test
	public void testCalendarConfigurationControllerList() {
		AuthenticationUtil.createAuthentication("user1", "xxx", "ROLE_USER");

		performList();

	}

	@Test(expected = AuthenticationCredentialsNotFoundException.class)
	public void testCalendarConfigurationControllerListUnauthorized() {

		performList();

	}

	private void performCreate(CalendarConfiguration cc2) {
		BindingResult result = new DirectFieldBindingResult(cc2, cc2.getClass()
				.getName());
		ModelMap modelMap = new ModelMap();
		String view = controller.create(cc2, result, modelMap);
		Assert.assertTrue(StringUtils.hasLength(view));
	}

	private void performShow() {
		ModelMap modelMap = new ModelMap();
		String view = controller.show(cc1.getId(), modelMap);
		Assert.assertTrue(StringUtils.hasLength(view));
	}

	private void performList() {
		ModelMap modelMap = new ModelMap();
		String view = controller.list(1, 10, modelMap);
		Assert.assertTrue(StringUtils.hasLength(view));
	}

	private void performDelete(CalendarConfiguration cc2) {
		String view = controller.delete(cc2.getId(), 1, 1);
		Assert.assertTrue(StringUtils.hasLength(view));
	}

	private void createTransientCalendarConfiguration() {
		this.cc2 = new CalendarConfiguration();
		cc2.setDomainName("domain");
		cc2.setMaxDaysInPast(10);
		cc2.setPasswordEncrypted("password");
		cc2.setToken("token");
		cc2.setOwner(cu1);
	}
}
