package com.owaconnector.dao.hibernate;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.owaconnector.dao.CalendarConfigurationDao;
import com.owaconnector.model.CalendarConfiguration;
import com.owaconnector.model.User;

@Repository
public class CalendarConfigurationDaoHibernate extends AbstractDaoHibernate
		implements CalendarConfigurationDao {

	/**
	 * Calls constructor of superclass to set SessionFactory. Obtain through
	 * getSession(). factory injected by Spring.
	 * 
	 * @param factory
	 */
	@Autowired
	public CalendarConfigurationDaoHibernate(SessionFactory factory) {
		super(factory);
	}

	public void deleteConfiguration(CalendarConfiguration config) {
		getSession().delete(config);

	}

	public CalendarConfiguration getCalendarConfigurationForToken(String token)
			throws Exception {
		return (CalendarConfiguration) getSession().createQuery(
				"from CalendarConfiguration c where c.token = :token")
				.setParameter("token", token).uniqueResult();
	}

	@SuppressWarnings("unchecked")
	public List<CalendarConfiguration> getCalendarConfigurationsForUser(
			User user) {
		return (List<CalendarConfiguration>) getSession().createQuery(
				"from CalendarConfiguration c where c.user = :user")
				.setParameter("user", user.getUsername()).list();

	}

	public void saveConfiguration(CalendarConfiguration config) {
		getSession().saveOrUpdate(config);

	}

}
