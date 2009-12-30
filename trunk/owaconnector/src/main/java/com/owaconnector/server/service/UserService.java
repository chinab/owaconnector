package com.owaconnector.server.service;

import com.owaconnector.exception.DuplicateUserException;
import com.owaconnector.exception.NoSuchUserException;
import com.owaconnector.model.User;

public interface UserService {

	public abstract void createUser(User user) throws DuplicateUserException;

	public abstract void deleteUser(User user) throws NoSuchUserException;

	public abstract void saveUser(User user) throws NoSuchUserException;

	public abstract User getUser(String username) throws NoSuchUserException;
}