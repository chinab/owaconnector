package com.owaconnector.domain;

import com.owaconnector.domain.CalendarConfiguration;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

privileged aspect CalendarConfigurationDataOnDemand_Roo_DataOnDemand {
    
    declare @type: CalendarConfigurationDataOnDemand: @Component;    
    
    private Random CalendarConfigurationDataOnDemand.rnd = new SecureRandom();    
    
    private List<CalendarConfiguration> CalendarConfigurationDataOnDemand.data;    
    
    public CalendarConfiguration CalendarConfigurationDataOnDemand.getNewTransientCalendarConfiguration(int index) {    
        com.owaconnector.domain.CalendarConfiguration obj = new com.owaconnector.domain.CalendarConfiguration();        
        obj.setDomainName("domainName_" + index);        
        obj.setMaxDaysInPast(new Integer(index));        
        obj.setPasswordEncrypted("passwordEncrypted_" + index);        
        obj.setToken("token_" + index);        
        obj.setURL(null);        
        obj.setUsername("username_" + index);        
        return obj;        
    }    
    
    public CalendarConfiguration CalendarConfigurationDataOnDemand.getRandomCalendarConfiguration() {    
        init();        
        CalendarConfiguration obj = data.get(rnd.nextInt(data.size()));        
        return CalendarConfiguration.findCalendarConfiguration(obj.getId());        
    }    
    
    public boolean CalendarConfigurationDataOnDemand.modifyCalendarConfiguration(CalendarConfiguration obj) {    
        return false;        
    }    
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)    
    public void CalendarConfigurationDataOnDemand.init() {    
        if (data != null) {        
            return;            
        }        
                
        data = com.owaconnector.domain.CalendarConfiguration.findCalendarConfigurationEntries(0, 10);        
        if (data == null) throw new IllegalStateException("Find entries implementation for 'CalendarConfiguration' illegally returned null");        
        if (data.size() > 0) {        
            return;            
        }        
                
        data = new java.util.ArrayList<com.owaconnector.domain.CalendarConfiguration>();        
        for (int i = 0; i < 10; i++) {        
            com.owaconnector.domain.CalendarConfiguration obj = getNewTransientCalendarConfiguration(i);            
            obj.persist();            
            data.add(obj);            
        }        
    }    
    
}
