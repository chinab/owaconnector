package com.owaconnector.web;

import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import com.owaconnector.domain.CalendarConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;

@RooWebScaffold(path = "calendarconfiguration", automaticallyMaintainView = true, formBackingObject = CalendarConfiguration.class,exposeFinders=false)
@RequestMapping("/calendarconfiguration/**")
@Controller
public class CalendarConfigurationController {
}
