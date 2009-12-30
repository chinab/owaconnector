package com.owaconnector.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.owaconnector.dao.UserDao;
import com.owaconnector.exception.DuplicateUserException;
import com.owaconnector.exception.NoSuchUserException;
import com.owaconnector.model.CalendarConfiguration;
import com.owaconnector.model.User;
import com.owaconnector.server.service.UserService;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

	@Autowired
	private UserDao userDao;

	/**
	 * @return the userDao
	 */
	public UserDao getUserDao() {
		return userDao;
	}

	/**
	 * @param userDao
	 *            the userDao to set
	 */
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.owaconnector.server.service.UserService#getUserByToken(java.lang.
	 * String)
	 */
	public User getUserForConfiguration(CalendarConfiguration config) {
		return null;
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void createUser(User user) throws DuplicateUserException {
		userDao.addUser(user);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void deleteUser(User user) throws NoSuchUserException {
		userDao.deleteUser(user);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void saveUser(User user) throws NoSuchUserException {
		userDao.saveUser(user);
	}

	public User getUser(String username) throws NoSuchUserException {
		return userDao.getUser(username);
	}

}
