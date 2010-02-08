package com.owaconnector.domain;

import java.lang.String;
import javax.persistence.EntityManager;
import javax.persistence.Query;

privileged aspect CalendarConfiguration_Roo_Finder {
    
    public static Query CalendarConfiguration.findCalendarConfigurationsByTokenEquals(String token) {    
        if (token == null || token.length() == 0) throw new IllegalArgumentException("The token argument is required");        
        EntityManager em = CalendarConfiguration.entityManager();        
        Query q = em.createQuery("SELECT CalendarConfiguration FROM CalendarConfiguration AS calendarconfiguration WHERE calendarconfiguration.token = :token");        
        q.setParameter("token", token);        
        return q;        
    }    
    
}
