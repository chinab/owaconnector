package com.owaconnector.domain;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@RooJavaBean
@RooToString
@RooEntity(finders = { "findCalendarUsersByUsernameEquals" })
public class CalendarUser implements UserDetails {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7142628250213871853L;

	@NotNull
	private String identifier;

	@NotNull
	private String username;

	@NotNull
	@OneToMany(cascade = CascadeType.ALL)
	private Set<CalendarConfiguration> configurations = new HashSet<CalendarConfiguration>();

	/**
	 * Returns the authorities granted to the user. Cannot return
	 * <code>null</code>.
	 */
	@Transient
	private Collection<GrantedAuthority> authorities;

	/**
	 * Returns the password used to authenticate the user. Cannot return
	 * <code>null</code>.
	 */
	private String password;

	/**
	 * Indicates whether the user's account has expired. An expired account
	 * cannot be authenticated.
	 * 
	 */
	private boolean accountNonExpired;

	/**
	 * Indicates whether the user is locked or unlocked. A locked user cannot be
	 * authenticated.
	 */
	private boolean accountNonLocked;

	/**
	 * Indicates whether the user's credentials (password) has expired. Expired
	 * credentials prevent authentication.
	 */
	private boolean credentialsNonExpired;

	/**
	 * Indicates whether the user is enabled or disabled. A disabled user cannot
	 * be authenticated.
	 */
	private boolean enabled;

	private String oauthToken;
}
