package com.owaconnector.service;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.owaconnector.domain.CalendarUser;
import com.owaconnector.util.AuthenticationUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-context.xml" })
public class UserDetailsServiceTest {

	@Autowired(required = true)
	private UserDetailsService userServiceImpl;

	private CalendarUser user;

	@Before
	public void setUp() {
		user = new CalendarUser();
		user.setUsername("user1");
		user.persist();
	}

	@After
	public void tearDown() {
		user.remove();
	}

	@Test
	public void testUserDetailsNotFound() {
		AuthenticationUtil.createAuthentication("user1", "xxx", "ROLE_USER");
		long before = CalendarUser.countCalendarUsers();
		// should create extra user:
		userServiceImpl.loadUserByUsername("asdf");
		long after = CalendarUser.countCalendarUsers();
		Assert.assertEquals(1, after - before);

	}

	@Test
	public void testUserDetailsFound() {
		AuthenticationUtil.createAuthentication("asdf", "xxx", "ROLE_USER");
		long before = CalendarUser.countCalendarUsers();
		// should not create extra user:
		userServiceImpl.loadUserByUsername("user1");
		long after = CalendarUser.countCalendarUsers();
		Assert.assertEquals(0, after - before);
	}
}
