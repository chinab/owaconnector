package com.owaconnector.service.impl;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.owaconnector.domain.CalendarUser;

public class UserServiceImpl implements UserDetailsService {

	@com.owaconnector.logger.Logger
	private org.apache.log4j.Logger log;

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.springframework.security.core.userdetails.UserDetailsService#
	 * loadUserByUsername(java.lang.String)
	 */
	public UserDetails loadUserByUsername(final String username) {
		// Allow anyone who's authenticated to log in as a "user"
		// check if this is a returning user
		CalendarUser currentUser;
		try {
			currentUser = (CalendarUser) CalendarUser
					.findCalendarUsersByUsernameEquals(username)
					.getSingleResult();
			currentUser.setAuthorities(AuthorityUtils
					.createAuthorityList("ROLE_USER"));
		} catch (EmptyResultDataAccessException e) {
			// if no user is found, create new instance
			currentUser = createCalendarUser(username, "ROLE_USER");
		}

		return currentUser;

	}

	/**
	 * Create new CalendarUser based on username.
	 * 
	 * @param username
	 *            username for the new CalendarUser
	 * @return CalendarUser new calendaruser
	 */
	private CalendarUser createCalendarUser(final String username, String role) {
		CalendarUser calendarUser = new CalendarUser();
		calendarUser.setUsername(username);
		calendarUser.setEnabled(true);
		calendarUser.setAccountNonExpired(true);
		calendarUser.setCredentialsNonExpired(true);
		calendarUser.setAccountNonLocked(true);
		calendarUser.setAuthorities(AuthorityUtils.createAuthorityList(role));
		log.debug("Created calendarUser: " + calendarUser.toString());
		calendarUser.persist();
		return calendarUser;
	}
}
