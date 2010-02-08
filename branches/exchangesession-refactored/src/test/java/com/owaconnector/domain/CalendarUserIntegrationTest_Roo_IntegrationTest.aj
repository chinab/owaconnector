package com.owaconnector.domain;

import com.owaconnector.domain.CalendarUserDataOnDemand;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

privileged aspect CalendarUserIntegrationTest_Roo_IntegrationTest {
    
    declare @type: CalendarUserIntegrationTest: @RunWith(SpringJUnit4ClassRunner.class);    
    
    @Autowired    
    private CalendarUserDataOnDemand CalendarUserIntegrationTest.dod;    
    
    @Test    
    public void CalendarUserIntegrationTest.testCountCalendarUsers() {    
        org.junit.Assert.assertNotNull("Data on demand for 'CalendarUser' failed to initialize correctly", dod.getRandomCalendarUser());        
        long count = com.owaconnector.domain.CalendarUser.countCalendarUsers();        
        org.junit.Assert.assertTrue("Counter for 'CalendarUser' incorrectly reported there were no entries", count > 0);        
    }    
    
    @Test    
    public void CalendarUserIntegrationTest.testFindCalendarUser() {    
        org.junit.Assert.assertNotNull("Data on demand for 'CalendarUser' failed to initialize correctly", dod.getRandomCalendarUser());        
        java.lang.Long id = dod.getRandomCalendarUser().getId();        
        org.junit.Assert.assertNotNull("Data on demand for 'CalendarUser' failed to provide an identifier", id);        
        com.owaconnector.domain.CalendarUser obj = com.owaconnector.domain.CalendarUser.findCalendarUser(id);        
        org.junit.Assert.assertNotNull("Find method for 'CalendarUser' illegally returned null for id '" + id + "'", obj);        
        org.junit.Assert.assertEquals("Find method for 'CalendarUser' returned the incorrect identifier", id, obj.getId());        
    }    
    
    @Test    
    public void CalendarUserIntegrationTest.testFindAllCalendarUsers() {    
        org.junit.Assert.assertNotNull("Data on demand for 'CalendarUser' failed to initialize correctly", dod.getRandomCalendarUser());        
        long count = com.owaconnector.domain.CalendarUser.countCalendarUsers();        
        org.junit.Assert.assertTrue("Too expensive to perform a find all test for 'CalendarUser', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);        
        java.util.List<com.owaconnector.domain.CalendarUser> result = com.owaconnector.domain.CalendarUser.findAllCalendarUsers();        
        org.junit.Assert.assertNotNull("Find all method for 'CalendarUser' illegally returned null", result);        
        org.junit.Assert.assertTrue("Find all method for 'CalendarUser' failed to return any data", result.size() > 0);        
    }    
    
    @Test    
    public void CalendarUserIntegrationTest.testFindCalendarUserEntries() {    
        org.junit.Assert.assertNotNull("Data on demand for 'CalendarUser' failed to initialize correctly", dod.getRandomCalendarUser());        
        long count = com.owaconnector.domain.CalendarUser.countCalendarUsers();        
        if (count > 20) count = 20;        
        java.util.List<com.owaconnector.domain.CalendarUser> result = com.owaconnector.domain.CalendarUser.findCalendarUserEntries(0, (int)count);        
        org.junit.Assert.assertNotNull("Find entries method for 'CalendarUser' illegally returned null", result);        
        org.junit.Assert.assertEquals("Find entries method for 'CalendarUser' returned an incorrect number of entries", count, result.size());        
    }    
    
    @Test    
    @Transactional    
    public void CalendarUserIntegrationTest.testFlush() {    
        org.junit.Assert.assertNotNull("Data on demand for 'CalendarUser' failed to initialize correctly", dod.getRandomCalendarUser());        
        java.lang.Long id = dod.getRandomCalendarUser().getId();        
        org.junit.Assert.assertNotNull("Data on demand for 'CalendarUser' failed to provide an identifier", id);        
        com.owaconnector.domain.CalendarUser obj = com.owaconnector.domain.CalendarUser.findCalendarUser(id);        
        org.junit.Assert.assertNotNull("Find method for 'CalendarUser' illegally returned null for id '" + id + "'", obj);        
        boolean modified =  dod.modifyCalendarUser(obj);        
        java.lang.Integer currentVersion = obj.getVersion();        
        obj.flush();        
        org.junit.Assert.assertTrue("Version for 'CalendarUser' failed to increment on flush directive", obj.getVersion() > currentVersion || !modified);        
    }    
    
    @Test    
    @Transactional    
    public void CalendarUserIntegrationTest.testMerge() {    
        org.junit.Assert.assertNotNull("Data on demand for 'CalendarUser' failed to initialize correctly", dod.getRandomCalendarUser());        
        java.lang.Long id = dod.getRandomCalendarUser().getId();        
        org.junit.Assert.assertNotNull("Data on demand for 'CalendarUser' failed to provide an identifier", id);        
        com.owaconnector.domain.CalendarUser obj = com.owaconnector.domain.CalendarUser.findCalendarUser(id);        
        org.junit.Assert.assertNotNull("Find method for 'CalendarUser' illegally returned null for id '" + id + "'", obj);        
        boolean modified =  dod.modifyCalendarUser(obj);        
        java.lang.Integer currentVersion = obj.getVersion();        
        obj.merge();        
        obj.flush();        
        org.junit.Assert.assertTrue("Version for 'CalendarUser' failed to increment on merge and flush directive", obj.getVersion() > currentVersion || !modified);        
    }    
    
    @Test    
    @Transactional    
    public void CalendarUserIntegrationTest.testPersist() {    
        org.junit.Assert.assertNotNull("Data on demand for 'CalendarUser' failed to initialize correctly", dod.getRandomCalendarUser());        
        com.owaconnector.domain.CalendarUser obj = dod.getNewTransientCalendarUser(Integer.MAX_VALUE);        
        org.junit.Assert.assertNotNull("Data on demand for 'CalendarUser' failed to provide a new transient entity", obj);        
        org.junit.Assert.assertNull("Expected 'CalendarUser' identifier to be null", obj.getId());        
        obj.persist();        
        obj.flush();        
        org.junit.Assert.assertNotNull("Expected 'CalendarUser' identifier to no longer be null", obj.getId());        
    }    
    
    @Test    
    @Transactional    
    public void CalendarUserIntegrationTest.testRemove() {    
        org.junit.Assert.assertNotNull("Data on demand for 'CalendarUser' failed to initialize correctly", dod.getRandomCalendarUser());        
        java.lang.Long id = dod.getRandomCalendarUser().getId();        
        org.junit.Assert.assertNotNull("Data on demand for 'CalendarUser' failed to provide an identifier", id);        
        com.owaconnector.domain.CalendarUser obj = com.owaconnector.domain.CalendarUser.findCalendarUser(id);        
        org.junit.Assert.assertNotNull("Find method for 'CalendarUser' illegally returned null for id '" + id + "'", obj);        
        obj.remove();        
        org.junit.Assert.assertNull("Failed to remove 'CalendarUser' with identifier '" + id + "'", com.owaconnector.domain.CalendarUser.findCalendarUser(id));        
    }    
    
}
