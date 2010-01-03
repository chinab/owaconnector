package com.owaconnector.domain;

import java.lang.String;

privileged aspect CalendarUser_Roo_ToString {
    
    public String CalendarUser.toString() {    
        StringBuilder sb = new StringBuilder();        
        sb.append("Id: ").append(getId()).append(", ");        
        sb.append("Version: ").append(getVersion()).append(", ");        
        sb.append("Identifier: ").append(getIdentifier()).append(", ");        
        sb.append("Name: ").append(getName()).append(", ");        
        sb.append("Configurations: ").append(getConfigurations() == null ? "null" : getConfigurations().size());        
        return sb.toString();        
    }    
    
}
