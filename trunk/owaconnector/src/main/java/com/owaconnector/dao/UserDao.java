package com.owaconnector.dao;

import com.owaconnector.exception.DuplicateUserException;
import com.owaconnector.exception.NoSuchUserException;
import com.owaconnector.model.User;

public interface UserDao {

	public abstract void saveUser(User user) throws NoSuchUserException;

	public abstract void addUser(User user) throws DuplicateUserException;

	public abstract void deleteUser(User user) throws NoSuchUserException;

	public abstract User getUser(String username) throws NoSuchUserException;
}
