package com.owaconnector.security;

import java.io.Serializable;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.owaconnector.domain.CalendarConfiguration;

public class CalendarConfigurationPermissionEvaluator implements
		PermissionEvaluator {

	@Override
	public boolean hasPermission(Authentication authentication,
			Object targetDomainObject, Object permission) {
		if (targetDomainObject instanceof CalendarConfiguration) {
			CalendarConfiguration calendarConfiguration = (CalendarConfiguration) targetDomainObject;
			if (authentication instanceof UsernamePasswordAuthenticationToken) {
				UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
				return token.getName().equals(
						calendarConfiguration.getOwner().getUsername());
			} else {
				return false;
			}
		} else {
			return false;
		}

	}

	@Override
	public boolean hasPermission(Authentication authentication,
			Serializable targetId, String targetType, Object permission) {
		throw new UnsupportedOperationException("Not implemented");
	}

}
