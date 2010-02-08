package com.owaconnector.domain;

import org.springframework.roo.addon.dod.RooDataOnDemand;
import org.springframework.test.context.ContextConfiguration;

@RooDataOnDemand(entity = CalendarConfiguration.class)
@ContextConfiguration(locations = { "/test-datasource.xml" })
public class CalendarConfigurationDataOnDemand {
}
