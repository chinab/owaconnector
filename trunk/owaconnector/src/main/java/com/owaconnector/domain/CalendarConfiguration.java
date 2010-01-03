package com.owaconnector.domain;

import java.net.URI;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

@Entity
@RooJavaBean
@RooToString
@RooEntity(finders = { "findCalendarConfigurationsByTokenEquals" })
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
}
