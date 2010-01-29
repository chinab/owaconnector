package com.owaconnector.web;

import com.owaconnector.domain.CalendarConfiguration;
import com.owaconnector.domain.CalendarUser;
import java.lang.Long;
import java.lang.String;
import javax.validation.Valid;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

privileged aspect CalendarUserController_Roo_Controller {
    
    @RequestMapping(value = "/calendaruser", method = RequestMethod.POST)    
    public String CalendarUserController.create(@Valid CalendarUser calendarUser, BindingResult result, ModelMap modelMap) {    
        if (calendarUser == null) throw new IllegalArgumentException("A calendarUser is required");        
        if (result.hasErrors()) {        
            modelMap.addAttribute("calendarUser", calendarUser);            
            modelMap.addAttribute("calendarconfigurations", CalendarConfiguration.findAllCalendarConfigurations());            
            return "calendaruser/create";            
        }        
        calendarUser.persist();        
        return "redirect:/calendaruser/" + calendarUser.getId();        
    }    
    
    @RequestMapping(value = "/calendaruser/form", method = RequestMethod.GET)    
    public String CalendarUserController.createForm(ModelMap modelMap) {    
        modelMap.addAttribute("calendarUser", new CalendarUser());        
        modelMap.addAttribute("calendarconfigurations", CalendarConfiguration.findAllCalendarConfigurations());        
        return "calendaruser/create";        
    }    
    
    @RequestMapping(value = "/calendaruser/{id}", method = RequestMethod.GET)    
    public String CalendarUserController.show(@PathVariable("id") Long id, ModelMap modelMap) {    
        if (id == null) throw new IllegalArgumentException("An Identifier is required");        
        modelMap.addAttribute("calendarUser", CalendarUser.findCalendarUser(id));        
        return "calendaruser/show";        
    }    
    
    @RequestMapping(value = "/calendaruser", method = RequestMethod.GET)    
    public String CalendarUserController.list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, ModelMap modelMap) {    
        if (page != null || size != null) {        
            int sizeNo = size == null ? 10 : size.intValue();            
            modelMap.addAttribute("calendarusers", CalendarUser.findCalendarUserEntries(page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo));            
            float nrOfPages = (float) CalendarUser.countCalendarUsers() / sizeNo;            
            modelMap.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));            
        } else {        
            modelMap.addAttribute("calendarusers", CalendarUser.findAllCalendarUsers());            
        }        
        return "calendaruser/list";        
    }    
    
    @RequestMapping(value = "find/ByOauthTokenEquals/form", method = RequestMethod.GET)    
    public String CalendarUserController.findCalendarUsersByOauthTokenEqualsForm(ModelMap modelMap) {    
        return "calendaruser/findCalendarUsersByOauthTokenEquals";        
    }    
    
    @RequestMapping(value = "find/ByOauthTokenEquals", method = RequestMethod.GET)    
    public String CalendarUserController.findCalendarUsersByOauthTokenEquals(@RequestParam("oauthtoken") String oauthToken, ModelMap modelMap) {    
        if (oauthToken == null || oauthToken.length() == 0) throw new IllegalArgumentException("A OauthToken is required.");        
        modelMap.addAttribute("calendarusers", CalendarUser.findCalendarUsersByOauthTokenEquals(oauthToken).getResultList());        
        return "calendaruser/list";        
    }    
    
    @RequestMapping(value = "find/ByUsernameEquals/form", method = RequestMethod.GET)    
    public String CalendarUserController.findCalendarUsersByUsernameEqualsForm(ModelMap modelMap) {    
        return "calendaruser/findCalendarUsersByUsernameEquals";        
    }    
    
    @RequestMapping(value = "find/ByUsernameEquals", method = RequestMethod.GET)    
    public String CalendarUserController.findCalendarUsersByUsernameEquals(@RequestParam("username") String username, ModelMap modelMap) {    
        if (username == null || username.length() == 0) throw new IllegalArgumentException("A Username is required.");        
        modelMap.addAttribute("calendarusers", CalendarUser.findCalendarUsersByUsernameEquals(username).getResultList());        
        return "calendaruser/list";        
    }    
    
}
