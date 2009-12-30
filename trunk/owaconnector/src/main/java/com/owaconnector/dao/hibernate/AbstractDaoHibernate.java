package com.owaconnector.dao.hibernate;

import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;

public abstract class AbstractDaoHibernate {
	/**
	 * Constructor for AbstractDaoHibernate. Called by subclasses using super(factory). Obtain Session through getSession().
	 * 
	 * @param factory
	 *            SessionFactory for dao.
	 */
	public AbstractDaoHibernate(SessionFactory factory) {
		this.factory = factory;
	}

	/**
	 * private field to store SessionFactory for all implementations of
	 * AbstractDao. Session can be obtained through getSession(), no need for
	 * the factory.
	 */
	private SessionFactory factory;

	/**
	 * Get current session from SessionFactory.
	 */
	protected Session getSession() {
		return factory.getCurrentSession();
	}

}
