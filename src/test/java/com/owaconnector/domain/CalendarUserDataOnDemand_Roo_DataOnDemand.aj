package com.owaconnector.domain;

import com.owaconnector.domain.CalendarUser;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

privileged aspect CalendarUserDataOnDemand_Roo_DataOnDemand {
    
    declare @type: CalendarUserDataOnDemand: @Component;    
    
    private Random CalendarUserDataOnDemand.rnd = new SecureRandom();    
    
    private List<CalendarUser> CalendarUserDataOnDemand.data;    
    
    public CalendarUser CalendarUserDataOnDemand.getNewTransientCalendarUser(int index) {    
        com.owaconnector.domain.CalendarUser obj = new com.owaconnector.domain.CalendarUser();        
        obj.setAccountNonExpired(true);        
        obj.setAccountNonLocked(true);        
        obj.setCredentialsNonExpired(true);        
        obj.setEnabled(true);        
        obj.setIdentifier("identifier_" + index);        
        obj.setOauthToken("oauthToken_" + index);        
        obj.setPassword("password_" + index);        
        obj.setUsername("username_" + index);        
        return obj;        
    }    
    
    public CalendarUser CalendarUserDataOnDemand.getRandomCalendarUser() {    
        init();        
        CalendarUser obj = data.get(rnd.nextInt(data.size()));        
        return CalendarUser.findCalendarUser(obj.getId());        
    }    
    
    public boolean CalendarUserDataOnDemand.modifyCalendarUser(CalendarUser obj) {    
        return false;        
    }    
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)    
    public void CalendarUserDataOnDemand.init() {    
        if (data != null) {        
            return;            
        }        
                
        data = com.owaconnector.domain.CalendarUser.findCalendarUserEntries(0, 10);        
        if (data == null) throw new IllegalStateException("Find entries implementation for 'CalendarUser' illegally returned null");        
        if (data.size() > 0) {        
            return;            
        }        
                
        data = new java.util.ArrayList<com.owaconnector.domain.CalendarUser>();        
        for (int i = 0; i < 10; i++) {        
            com.owaconnector.domain.CalendarUser obj = getNewTransientCalendarUser(i);            
            obj.persist();            
            data.add(obj);            
        }        
    }    
    
}
