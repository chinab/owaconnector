package com.owaconnector.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class User implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1603457793417836165L;
	private String username;
	private String emailAddress;
	private String displayName;
	private String password;
	@Id
	@GeneratedValue
	Long id;

	public User() {
	};

	public User(String username, String password, String emailAddress,
			String displayName) {
		setUsername(username);
		setPassword(password);
		setEmailAddress(emailAddress);
		setDisplayName(displayName);
	}

	public String getUsername() {
		return username;
	}

	private void setUsername(String username) {
		this.username = username;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	private void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getDisplayName() {
		return displayName;
	}

	private void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getPassword() {
		return this.password;
	}

	private void setPassword(String password) {
		this.password = password;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
