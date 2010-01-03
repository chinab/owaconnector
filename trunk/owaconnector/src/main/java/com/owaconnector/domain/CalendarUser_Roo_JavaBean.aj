package com.owaconnector.domain;

import com.owaconnector.domain.CalendarConfiguration;
import java.lang.String;
import java.util.Set;

privileged aspect CalendarUser_Roo_JavaBean {
    
    public String CalendarUser.getIdentifier() {    
        return this.identifier;        
    }    
    
    public void CalendarUser.setIdentifier(String identifier) {    
        this.identifier = identifier;        
    }    
    
    public String CalendarUser.getName() {    
        return this.name;        
    }    
    
    public void CalendarUser.setName(String name) {    
        this.name = name;        
    }    
    
    public Set<CalendarConfiguration> CalendarUser.getConfigurations() {    
        return this.configurations;        
    }    
    
    public void CalendarUser.setConfigurations(Set<CalendarConfiguration> configurations) {    
        this.configurations = configurations;        
    }    
    
}
