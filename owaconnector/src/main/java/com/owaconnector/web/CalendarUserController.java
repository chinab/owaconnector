package com.owaconnector.web;

import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import com.owaconnector.domain.CalendarUser;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;

@RooWebScaffold(path = "calendaruser", automaticallyMaintainView = true, formBackingObject = CalendarUser.class,delete=false, 
        update=false)
@RequestMapping("/calendaruser/**")
@Controller
public class CalendarUserController {
}
