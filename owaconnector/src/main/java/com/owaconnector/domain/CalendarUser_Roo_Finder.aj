package com.owaconnector.domain;

import java.lang.String;
import javax.persistence.EntityManager;
import javax.persistence.Query;

privileged aspect CalendarUser_Roo_Finder {
    
    public static Query CalendarUser.findCalendarUsersByUsernameEquals(String username) {    
        if (username == null || username.length() == 0) throw new IllegalArgumentException("The username argument is required");        
        EntityManager em = CalendarUser.entityManager();        
        Query q = em.createQuery("SELECT CalendarUser FROM CalendarUser AS calendaruser WHERE calendaruser.username = :username");        
        q.setParameter("username", username);        
        return q;        
    }    
    
}
