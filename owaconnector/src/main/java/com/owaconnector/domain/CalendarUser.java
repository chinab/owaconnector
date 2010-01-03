package com.owaconnector.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

@Entity
@RooJavaBean
@RooToString
@RooEntity
public class CalendarUser {

    @NotNull
    private String identifier;

    @NotNull
    private String name;

    @NotNull
    @OneToMany(cascade = CascadeType.ALL)
    private Set<CalendarConfiguration> configurations = new HashSet<CalendarConfiguration>();
}
