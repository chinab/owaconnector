package com.owaconnector.domain;

import java.lang.String;

privileged aspect CalendarUser_Roo_ToString {
    
    public String CalendarUser.toString() {    
        StringBuilder sb = new StringBuilder();        
        sb.append("Id: ").append(getId()).append(", ");        
        sb.append("Version: ").append(getVersion()).append(", ");        
        sb.append("Identifier: ").append(getIdentifier()).append(", ");        
        sb.append("Username: ").append(getUsername()).append(", ");        
        sb.append("Configurations: ").append(getConfigurations() == null ? "null" : getConfigurations().size()).append(", ");        
        sb.append("Authorities: ").append(getAuthorities() == null ? "null" : getAuthorities().size()).append(", ");        
        sb.append("Password: ").append(getPassword()).append(", ");        
        sb.append("AccountNonExpired: ").append(isAccountNonExpired()).append(", ");        
        sb.append("AccountNonLocked: ").append(isAccountNonLocked()).append(", ");        
        sb.append("CredentialsNonExpired: ").append(isCredentialsNonExpired()).append(", ");        
        sb.append("Enabled: ").append(isEnabled()).append(", ");        
        sb.append("OauthToken: ").append(getOauthToken());        
        return sb.toString();        
    }    
    
}
