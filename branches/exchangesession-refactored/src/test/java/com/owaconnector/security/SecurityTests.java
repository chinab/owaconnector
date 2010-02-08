package com.owaconnector.security;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.owaconnector.domain.CalendarConfiguration;
import com.owaconnector.domain.CalendarUser;
import com.owaconnector.util.AuthenticationUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-context.xml" })
public class SecurityTests {

	// logger
	@com.owaconnector.logger.Logger
	private org.apache.log4j.Logger log;

	private ArrayList<CalendarUser> users = new ArrayList<CalendarUser>();
	private ArrayList<CalendarConfiguration> configurations = new ArrayList<CalendarConfiguration>();

	@Before
	public void setUp() {
		AuthenticationUtil.createAuthentication("user1", "xxx", "ROLE_USER");
		CalendarUser cu1 = new CalendarUser();
		cu1.setId(new Long('1'));
		cu1.setUsername("user1");
		cu1.persist();
		users.add(cu1);

		CalendarUser cu2 = new CalendarUser();
		cu2.setId(new Long('2'));
		cu2.setUsername("user2");
		cu2.persist();
		users.add(cu2);

		CalendarConfiguration cc1 = new CalendarConfiguration();
		cc1.setId(new Long('1'));
		cc1.setDomainName("domain");
		cc1.setMaxDaysInPast(10);
		cc1.setPasswordEncrypted("password");
		cc1.setToken("token");
		cc1.setOwner(cu1);
		cc1.persist();
		configurations.add(cc1);

		CalendarConfiguration cc2 = new CalendarConfiguration();
		cc2.setId(new Long('2'));
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
		for (CalendarConfiguration config : configurations) {
			configurations.remove(config);
			config.remove();

		}

		for (CalendarUser user : users) {

			users.remove(user);
			user.remove();
		}

	}

	@Test
	public void testSecuredMethodPostFilterRightUser() throws Exception {
		String username = "user1";
		AuthenticationUtil.createAuthentication(username, "xxx", "ROLE_USER");
		List<CalendarConfiguration> findAllCalendarConfigurations = CalendarConfiguration
				.findAllCalendarConfigurations();
		validateOwnerOfConfigurations(username, findAllCalendarConfigurations);
		Assert.assertEquals(1, findAllCalendarConfigurations.size());

	}

	@Test
	public void testSecuredMethodPostFilterRightUserEntries() throws Exception {

		String username = "user1";
		AuthenticationUtil.createAuthentication(username, "xxx", "ROLE_USER");
		List<CalendarConfiguration> findAllCalendarConfigurations = CalendarConfiguration
				.findCalendarConfigurationEntries(0, 100);
		validateOwnerOfConfigurations(username, findAllCalendarConfigurations);
		Assert.assertEquals(1, findAllCalendarConfigurations.size());
	}

	@Test(expected = AccessDeniedException.class)
	public void testSecuredMethodPostAuthorizeAccessDenied() throws Exception {

		String username = "user1";
		AuthenticationUtil.createAuthentication(username, "xxx", "ROLE_USER");
		for (CalendarConfiguration configuration : configurations) {
			log.debug("findCalendarConfiguration for: "
					+ configuration.toString());
			log.debug("Owner: " + configuration.getOwner().getUsername());
			CalendarConfiguration.findCalendarConfiguration(configuration
					.getId());

		}

	}

	@Test
	public void testSecuredMethodPostAuthorizeOwnObject() throws Exception {

		String username = "user1";
		AuthenticationUtil.createAuthentication(username, "xxx", "ROLE_USER");
		List<CalendarConfiguration> findAllCalendarConfigurations = CalendarConfiguration
				.findAllCalendarConfigurations();
		validateOwnerOfConfigurations(username, findAllCalendarConfigurations);
		for (CalendarConfiguration calendarConfiguration : findAllCalendarConfigurations) {
			CalendarConfiguration
					.findCalendarConfiguration(calendarConfiguration.getId());
		}
		Assert.assertEquals(1, findAllCalendarConfigurations.size());

	}

	private void validateOwnerOfConfigurations(String username,
			List<CalendarConfiguration> findAllCalendarConfigurations) {
		for (CalendarConfiguration calendarConfiguration : findAllCalendarConfigurations) {
			CalendarUser owner = calendarConfiguration.getOwner();
			Assert.assertEquals(owner.getUsername().toString(), username);
		}
	}

}
