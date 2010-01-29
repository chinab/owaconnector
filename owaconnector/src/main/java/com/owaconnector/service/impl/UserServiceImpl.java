package com.owaconnector.service.impl;

import java.util.Arrays;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.owaconnector.domain.CalendarUser;


public class UserServiceImpl implements UserDetailsService {

	@com.owaconnector.logger.Logger 
	private org.apache.log4j.Logger log;

	public UserDetails loadUserByUsername(final String username) {
		// Allow anyone who's authenticated to log in as a "user"
		CalendarUser calendarUser = new CalendarUser();
		calendarUser.setUsername(username);
		calendarUser.setEnabled(true);
		calendarUser.setAccountNonExpired(true);
		calendarUser.setCredentialsNonExpired(true);
		calendarUser.setAccountNonLocked(true);
		calendarUser.setAuthorities(Arrays
				.asList(new GrantedAuthority[] { new GrantedAuthorityImpl(
						"ROLE_USER") }));
		log.debug("Created calendarUser: " + calendarUser.toString());
		calendarUser.persist();
		return calendarUser;

	}
}
