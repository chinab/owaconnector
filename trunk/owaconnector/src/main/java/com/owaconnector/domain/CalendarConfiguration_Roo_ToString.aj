package com.owaconnector.domain;

import java.lang.String;

privileged aspect CalendarConfiguration_Roo_ToString {
    
    public String CalendarConfiguration.toString() {    
        StringBuilder sb = new StringBuilder();        
        sb.append("Id: ").append(getId()).append(", ");        
        sb.append("Version: ").append(getVersion()).append(", ");        
        sb.append("Username: ").append(getUsername()).append(", ");        
        sb.append("DomainName: ").append(getDomainName()).append(", ");        
        sb.append("PasswordEncrypted: ").append(getPasswordEncrypted()).append(", ");        
        sb.append("URL: ").append(getURL()).append(", ");        
        sb.append("MaxDaysInPast: ").append(getMaxDaysInPast()).append(", ");        
        sb.append("Token: ").append(getToken()).append(", ");        
        sb.append("Owner: ").append(getOwner());        
        return sb.toString();        
    }    
    
}
