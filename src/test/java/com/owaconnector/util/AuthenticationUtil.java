package com.owaconnector.util;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthenticationUtil {
	public static void createAuthentication(String username, String password,
			String role) {
		Authentication token = new UsernamePasswordAuthenticationToken(
				username, password, AuthorityUtils.createAuthorityList(role));
		SecurityContextHolder.getContext().setAuthentication(token);
	}
}
