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

privileged aspect CalendarConfigurationController_Roo_Controller {
    
    @RequestMapping(value = "/calendarconfiguration", method = RequestMethod.POST)    
    public String CalendarConfigurationController.create(@Valid CalendarConfiguration calendarConfiguration, BindingResult result, ModelMap modelMap) {    
        if (calendarConfiguration == null) throw new IllegalArgumentException("A calendarConfiguration is required");        
        if (result.hasErrors()) {        
            modelMap.addAttribute("calendarConfiguration", calendarConfiguration);            
            modelMap.addAttribute("calendarusers", CalendarUser.findAllCalendarUsers());            
            return "calendarconfiguration/create";            
        }        
        calendarConfiguration.persist();        
        return "redirect:/calendarconfiguration/" + calendarConfiguration.getId();        
    }    
    
    @RequestMapping(value = "/calendarconfiguration/form", method = RequestMethod.GET)    
    public String CalendarConfigurationController.createForm(ModelMap modelMap) {    
        modelMap.addAttribute("calendarConfiguration", new CalendarConfiguration());        
        modelMap.addAttribute("calendarusers", CalendarUser.findAllCalendarUsers());        
        return "calendarconfiguration/create";        
    }    
    
    @RequestMapping(value = "/calendarconfiguration/{id}", method = RequestMethod.GET)    
    public String CalendarConfigurationController.show(@PathVariable("id") Long id, ModelMap modelMap) {    
        if (id == null) throw new IllegalArgumentException("An Identifier is required");        
        modelMap.addAttribute("calendarConfiguration", CalendarConfiguration.findCalendarConfiguration(id));        
        return "calendarconfiguration/show";        
    }    
    
    @RequestMapping(value = "/calendarconfiguration", method = RequestMethod.GET)    
    public String CalendarConfigurationController.list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, ModelMap modelMap) {    
        if (page != null || size != null) {        
            int sizeNo = size == null ? 10 : size.intValue();            
            modelMap.addAttribute("calendarconfigurations", CalendarConfiguration.findCalendarConfigurationEntries(page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo));            
            float nrOfPages = (float) CalendarConfiguration.countCalendarConfigurations() / sizeNo;            
            modelMap.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));            
        } else {        
            modelMap.addAttribute("calendarconfigurations", CalendarConfiguration.findAllCalendarConfigurations());            
        }        
        return "calendarconfiguration/list";        
    }    
    
    @RequestMapping(method = RequestMethod.PUT)    
    public String CalendarConfigurationController.update(@Valid CalendarConfiguration calendarConfiguration, BindingResult result, ModelMap modelMap) {    
        if (calendarConfiguration == null) throw new IllegalArgumentException("A calendarConfiguration is required");        
        if (result.hasErrors()) {        
            modelMap.addAttribute("calendarConfiguration", calendarConfiguration);            
            modelMap.addAttribute("calendarusers", CalendarUser.findAllCalendarUsers());            
            return "calendarconfiguration/update";            
        }        
        calendarConfiguration.merge();        
        return "redirect:/calendarconfiguration/" + calendarConfiguration.getId();        
    }    
    
    @RequestMapping(value = "/calendarconfiguration/{id}/form", method = RequestMethod.GET)    
    public String CalendarConfigurationController.updateForm(@PathVariable("id") Long id, ModelMap modelMap) {    
        if (id == null) throw new IllegalArgumentException("An Identifier is required");        
        modelMap.addAttribute("calendarConfiguration", CalendarConfiguration.findCalendarConfiguration(id));        
        modelMap.addAttribute("calendarusers", CalendarUser.findAllCalendarUsers());        
        return "calendarconfiguration/update";        
    }    
    
    @RequestMapping(value = "/calendarconfiguration/{id}", method = RequestMethod.DELETE)    
    public String CalendarConfigurationController.delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size) {    
        if (id == null) throw new IllegalArgumentException("An Identifier is required");        
        CalendarConfiguration.findCalendarConfiguration(id).remove();        
        return "redirect:/calendarconfiguration?page=" + ((page == null) ? "1" : page.toString()) + "&size=" + ((size == null) ? "10" : size.toString());        
    }    
    
    @RequestMapping(value = "find/ByTokenEquals/form", method = RequestMethod.GET)    
    public String CalendarConfigurationController.findCalendarConfigurationsByTokenEqualsForm(ModelMap modelMap) {    
        return "calendarconfiguration/findCalendarConfigurationsByTokenEquals";        
    }    
    
    @RequestMapping(value = "find/ByTokenEquals", method = RequestMethod.GET)    
    public String CalendarConfigurationController.findCalendarConfigurationsByTokenEquals(@RequestParam("token") String token, ModelMap modelMap) {    
        if (token == null || token.length() == 0) throw new IllegalArgumentException("A Token is required.");        
        modelMap.addAttribute("calendarconfigurations", CalendarConfiguration.findCalendarConfigurationsByTokenEquals(token).getResultList());        
        return "calendarconfiguration/list";        
    }    
    
}
