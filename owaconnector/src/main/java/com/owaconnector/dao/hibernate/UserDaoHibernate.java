package com.owaconnector.dao.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.owaconnector.dao.UserDao;
import com.owaconnector.exception.DuplicateUserException;
import com.owaconnector.exception.NoSuchUserException;
import com.owaconnector.model.User;

@Repository
public class UserDaoHibernate extends AbstractDaoHibernate implements UserDao {
	/**
	 * Calls constructor of superclass to set SessionFactory. Obtain through
	 * getSession(). factory injected by Spring.
	 * 
	 * @param factory
	 */
	@Autowired
	public UserDaoHibernate(SessionFactory factory) {
		super(factory);
	}

	public void saveUser(User user) throws NoSuchUserException {
		try {
			getSession().saveOrUpdate(user);
		} catch (HibernateException e) {
			throw new NoSuchUserException("User with username "
					+ user.getUsername() + " does not exist");
		}
	}

	public void addUser(User user) throws DuplicateUserException {
		getSession().save(user);
	}

	public void deleteUser(User user) throws NoSuchUserException {
		try {
			getSession().delete(user);
		} catch (HibernateException e) {
			throw new NoSuchUserException("User with username "
					+ user.getUsername() + " does not exist");
		}
	}

	public User getUser(String username) throws NoSuchUserException {

		return (User) getSession().createQuery(
				"from User u where u.username = :username").setParameter(
				"username", username).uniqueResult();
	}

}