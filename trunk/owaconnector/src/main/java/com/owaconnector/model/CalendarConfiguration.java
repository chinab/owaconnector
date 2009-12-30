package com.owaconnector.model;

import java.io.Serializable;
import java.net.URI;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.owaconnector.util.ConfigurationUtil;

@Entity
public class CalendarConfiguration implements Serializable {

	// needed for persistence
	@GeneratedValue
	Long id;
	private static final long serialVersionUID = 5188699604904296844L;

	public CalendarConfiguration() {
	}

	/**
	 * Actual properties
	 */
	private String username;
	private byte[] password;
	private URI url;
	private int maxDaysInPast;
	private String token;
	private User user;

	/**
	 * @param username
	 * @param password
	 * @param url
	 * @param maxDaysInPast
	 */
	public CalendarConfiguration(String username, byte[] password, URI url,
			int maxDaysInPast, User user) {
		setUsername(username);
		setPassword(password);
		setUrl(url);
		setMaxDaysInPast(maxDaysInPast);
		setToken(ConfigurationUtil.generateToken());
		setUser(user);

	}

	public String getUsername() {
		return username;
	}

	public byte[] getPassword() {
		return password;
	}

	public URI getUrl() {
		return url;
	}

	public int getMaxDaysInPast() {
		return maxDaysInPast;
	}

	/**
	 * @param username
	 *            the username to set
	 */
	private void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	private void setPassword(byte[] password) {
		this.password = password;
	}

	/**
	 * @param url
	 *            the url to set
	 */
	private void setUrl(URI url) {
		this.url = url;
	}

	/**
	 * @param maxDaysInPast
	 *            the maxDaysInPast to set
	 */
	public void setMaxDaysInPast(int maxDaysInPast) {
		this.maxDaysInPast = maxDaysInPast;
	}

	/**
	 * @param token
	 *            the token to set
	 */
	private void setToken(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	/**
	 * needed for persistence
	 */
	@Id
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "USER_ID")
	public User getUser() {
		return user;
	}

}
