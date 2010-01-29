package com.owaconnector.domain;

import com.owaconnector.domain.CalendarConfiguration;
import java.lang.String;
import java.util.Collection;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;

privileged aspect CalendarUser_Roo_JavaBean {
    
    public String CalendarUser.getIdentifier() {    
        return this.identifier;        
    }    
    
    public void CalendarUser.setIdentifier(String identifier) {    
        this.identifier = identifier;        
    }    
    
    public String CalendarUser.getUsername() {    
        return this.username;        
    }    
    
    public void CalendarUser.setUsername(String username) {    
        this.username = username;        
    }    
    
    public Set<CalendarConfiguration> CalendarUser.getConfigurations() {    
        return this.configurations;        
    }    
    
    public void CalendarUser.setConfigurations(Set<CalendarConfiguration> configurations) {    
        this.configurations = configurations;        
    }    
    
    public Collection<GrantedAuthority> CalendarUser.getAuthorities() {    
        return this.authorities;        
    }    
    
    public void CalendarUser.setAuthorities(Collection<GrantedAuthority> authorities) {    
        this.authorities = authorities;        
    }    
    
    public String CalendarUser.getPassword() {    
        return this.password;        
    }    
    
    public void CalendarUser.setPassword(String password) {    
        this.password = password;        
    }    
    
    public boolean CalendarUser.isAccountNonExpired() {    
        return this.accountNonExpired;        
    }    
    
    public void CalendarUser.setAccountNonExpired(boolean accountNonExpired) {    
        this.accountNonExpired = accountNonExpired;        
    }    
    
    public boolean CalendarUser.isAccountNonLocked() {    
        return this.accountNonLocked;        
    }    
    
    public void CalendarUser.setAccountNonLocked(boolean accountNonLocked) {    
        this.accountNonLocked = accountNonLocked;        
    }    
    
    public boolean CalendarUser.isCredentialsNonExpired() {    
        return this.credentialsNonExpired;        
    }    
    
    public void CalendarUser.setCredentialsNonExpired(boolean credentialsNonExpired) {    
        this.credentialsNonExpired = credentialsNonExpired;        
    }    
    
    public boolean CalendarUser.isEnabled() {    
        return this.enabled;        
    }    
    
    public void CalendarUser.setEnabled(boolean enabled) {    
        this.enabled = enabled;        
    }    
    
    public String CalendarUser.getOauthToken() {    
        return this.oauthToken;        
    }    
    
    public void CalendarUser.setOauthToken(String oauthToken) {    
        this.oauthToken = oauthToken;        
    }    
    
}
