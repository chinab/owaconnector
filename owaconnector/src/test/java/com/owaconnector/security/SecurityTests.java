package com.owaconnector.security;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

import com.owaconnector.domain.CalendarConfiguration;
import com.owaconnector.domain.CalendarUser;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-context.xml", "/test-datasource.xml" })
public class SecurityTests {

	// logger
	@com.owaconnector.logger.Logger
	private org.apache.log4j.Logger log;

	private ArrayList<CalendarUser> users = new ArrayList<CalendarUser>();
	private ArrayList<CalendarConfiguration> configurations = new ArrayList<CalendarConfiguration>();

	@Before
	public void setUp() {
		Authentication token = new UsernamePasswordAuthenticationToken("user1",
				"xxx", AuthorityUtils.createAuthorityList("ROLE_USER"));
		SecurityContextHolder.getContext().setAuthentication(token);

		CalendarUser cu1 = new CalendarUser();
		cu1.setUsername("user1");
		cu1.persist();
		users.add(cu1);

		CalendarUser cu2 = new CalendarUser();
		cu2.setUsername("user2");
		cu2.persist();
		users.add(cu2);

		CalendarConfiguration cc1 = new CalendarConfiguration();
		cc1.setDomainName("domain");
		cc1.setMaxDaysInPast(10);
		cc1.setPasswordEncrypted("password");
		cc1.setToken("token");
		cc1.setOwner(cu1);
		cc1.persist();
		configurations.add(cc1);

		CalendarConfiguration cc2 = new CalendarConfiguration();
		cc2.setDomainName("domain");
		cc2.setMaxDaysInPast(10);
		cc2.setPasswordEncrypted("password");
		cc2.setToken("token");
		cc2.setOwner(cu2);
		cc2.persist();
		configurations.add(cc2);

		SecurityContextHolder.clearContext();

	}

	@After
	public void tearDown() {
		SecurityContextHolder.clearContext();
		Authentication token = new UsernamePasswordAuthenticationToken("user1",
				"xxx", AuthorityUtils.createAuthorityList("ROLE_USER"));
		SecurityContextHolder.getContext().setAuthentication(token);

		for (CalendarConfiguration config : configurations) {
			configurations.remove(config);
			config.remove();

		}
		for (CalendarUser user : users) {
			users.remove(user);
			user.remove();
		}
		SecurityContextHolder.clearContext();

	}

	// @Test(expected = AccessDeniedException.class)
	// public void testSecuredClassWrongRole() throws Exception {
	//
	// Authentication token = new UsernamePasswordAuthenticationToken("test",
	// "xxx", AuthorityUtils.createAuthorityList("ROLE_ADMIN"));
	// SecurityContextHolder.getContext().setAuthentication(token);
	// CalendarConfiguration.findAllCalendarConfigurations();
	//
	// }

	@Test
	public void testSecuredClassRightRole() throws Exception {

		Authentication token = new UsernamePasswordAuthenticationToken("user1",
				"xxx", AuthorityUtils.createAuthorityList("ROLE_USER"));
		SecurityContextHolder.getContext().setAuthentication(token);
		List<CalendarConfiguration> findAllCalendarConfigurations = CalendarConfiguration
				.findAllCalendarConfigurations();
		Assert.notNull(findAllCalendarConfigurations);
	}

	@Test
	public void testSecuredClassPostFilter() throws Exception {

		Authentication token = new UsernamePasswordAuthenticationToken("user1",
				"xxx", AuthorityUtils.createAuthorityList("ROLE_USER"));
		SecurityContextHolder.getContext().setAuthentication(token);
		List<CalendarConfiguration> findAllCalendarConfigurations = CalendarConfiguration
				.findAllCalendarConfigurations();
		Assert.notNull(findAllCalendarConfigurations);
		log
				.debug("[testSecuredClassPostFilter] findAllCalendarConfigurations size: "
						+ findAllCalendarConfigurations.size());
		Assert.isTrue(findAllCalendarConfigurations.size() == 1,
				"Expected 1 calendarconfiguration, actual: "
						+ findAllCalendarConfigurations.size());
	}

	@Test(expected = AccessDeniedException.class)
	public void testSecuredClassPreAuthorize() throws Exception {

		Authentication token = new UsernamePasswordAuthenticationToken("user1",
				"xxx", AuthorityUtils.createAuthorityList("ROLE_USER"));
		SecurityContextHolder.getContext().setAuthentication(token);
		for (CalendarConfiguration configuration : configurations) {
			CalendarConfiguration.findCalendarConfiguration(configuration
					.getId());

		}

	}

}
