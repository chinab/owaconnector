package com.owaconnector.domain;

import com.owaconnector.domain.CalendarUser;
import java.lang.Integer;
import java.lang.String;
import java.net.URI;

privileged aspect CalendarConfiguration_Roo_JavaBean {
    
    public String CalendarConfiguration.getUsername() {    
        return this.username;        
    }    
    
    public void CalendarConfiguration.setUsername(String username) {    
        this.username = username;        
    }    
    
    public String CalendarConfiguration.getDomainName() {    
        return this.domainName;        
    }    
    
    public void CalendarConfiguration.setDomainName(String domainName) {    
        this.domainName = domainName;        
    }    
    
    public String CalendarConfiguration.getPasswordEncrypted() {    
        return this.passwordEncrypted;        
    }    
    
    public void CalendarConfiguration.setPasswordEncrypted(String passwordEncrypted) {    
        this.passwordEncrypted = passwordEncrypted;        
    }    
    
    public URI CalendarConfiguration.getURL() {    
        return this.URL;        
    }    
    
    public void CalendarConfiguration.setURL(URI URL) {    
        this.URL = URL;        
    }    
    
    public Integer CalendarConfiguration.getMaxDaysInPast() {    
        return this.maxDaysInPast;        
    }    
    
    public void CalendarConfiguration.setMaxDaysInPast(Integer maxDaysInPast) {    
        this.maxDaysInPast = maxDaysInPast;        
    }    
    
    public String CalendarConfiguration.getToken() {    
        return this.token;        
    }    
    
    public void CalendarConfiguration.setToken(String token) {    
        this.token = token;        
    }    
    
    public CalendarUser CalendarConfiguration.getOwner() {    
        return this.owner;        
    }    
    
    public void CalendarConfiguration.setOwner(CalendarUser owner) {    
        this.owner = owner;        
    }    
    
}
