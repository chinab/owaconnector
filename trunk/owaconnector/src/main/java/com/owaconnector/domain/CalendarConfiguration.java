package com.owaconnector.domain;

import java.net.URI;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;

@Entity
@RooJavaBean
@RooToString
@RooEntity(finders = { "findCalendarConfigurationsByTokenEquals" })
// @Secured("ROLE_USER")
public class CalendarConfiguration {

	@NotNull
	private String username;

	@NotNull
	private String domainName;

	@NotNull
	private String passwordEncrypted;

	@NotNull
	private URI URL;

	@NotNull
	@DecimalMin("0")
	@DecimalMax("50")
	private Integer maxDaysInPast;

	private String token;

	@ManyToOne(targetEntity = CalendarUser.class)
	@JoinColumn
	private CalendarUser owner;

	@SuppressWarnings("unchecked")
	// @PostFilter("hasPermission(filterObject,'read')")
	@PostFilter("filterObject.owner.username == authentication.name")
	public static List<CalendarConfiguration> findAllCalendarConfigurations() {
		return entityManager().createQuery(
				"select o from CalendarConfiguration o").getResultList();
	}

	// @PostAuthorize("hasPermission(returnObject,'read')")
	@PostAuthorize("returnObject.owner.username == authentication.name")
	public static CalendarConfiguration findCalendarConfiguration(Long id) {
		if (id == null)
			throw new IllegalArgumentException(
					"An identifier is required to retrieve an instance of CalendarConfiguration");
		return entityManager().find(CalendarConfiguration.class, id);
	}

	@SuppressWarnings("unchecked")
	// @PostFilter("hasPermission(filterObject,'read')")
	@PostFilter("filterObject.owner.username == authentication.name")
	public static List<CalendarConfiguration> findCalendarConfigurationEntries(
			int firstResult, int maxResults) {
		return entityManager().createQuery(
				"select o from CalendarConfiguration o").setFirstResult(
				firstResult).setMaxResults(maxResults).getResultList();
	}
}
