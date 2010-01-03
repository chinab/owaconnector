package com.owaconnector.domain;

import com.owaconnector.domain.CalendarConfigurationDataOnDemand;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

privileged aspect CalendarConfigurationIntegrationTest_Roo_IntegrationTest {
    
    declare @type: CalendarConfigurationIntegrationTest: @RunWith(SpringJUnit4ClassRunner.class);    
    
    declare @type: CalendarConfigurationIntegrationTest: @ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext.xml");    
    
    @Autowired    
    private CalendarConfigurationDataOnDemand CalendarConfigurationIntegrationTest.dod;    
    
    @Test    
    public void CalendarConfigurationIntegrationTest.testCountCalendarConfigurations() {    
        org.junit.Assert.assertNotNull("Data on demand for 'CalendarConfiguration' failed to initialize correctly", dod.getRandomCalendarConfiguration());        
        long count = com.owaconnector.domain.CalendarConfiguration.countCalendarConfigurations();        
        org.junit.Assert.assertTrue("Counter for 'CalendarConfiguration' incorrectly reported there were no entries", count > 0);        
    }    
    
    @Test    
    public void CalendarConfigurationIntegrationTest.testFindCalendarConfiguration() {    
        org.junit.Assert.assertNotNull("Data on demand for 'CalendarConfiguration' failed to initialize correctly", dod.getRandomCalendarConfiguration());        
        java.lang.Long id = dod.getRandomCalendarConfiguration().getId();        
        org.junit.Assert.assertNotNull("Data on demand for 'CalendarConfiguration' failed to provide an identifier", id);        
        com.owaconnector.domain.CalendarConfiguration obj = com.owaconnector.domain.CalendarConfiguration.findCalendarConfiguration(id);        
        org.junit.Assert.assertNotNull("Find method for 'CalendarConfiguration' illegally returned null for id '" + id + "'", obj);        
        org.junit.Assert.assertEquals("Find method for 'CalendarConfiguration' returned the incorrect identifier", id, obj.getId());        
    }    
    
    @Test    
    public void CalendarConfigurationIntegrationTest.testFindAllCalendarConfigurations() {    
        org.junit.Assert.assertNotNull("Data on demand for 'CalendarConfiguration' failed to initialize correctly", dod.getRandomCalendarConfiguration());        
        long count = com.owaconnector.domain.CalendarConfiguration.countCalendarConfigurations();        
        org.junit.Assert.assertTrue("Too expensive to perform a find all test for 'CalendarConfiguration', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);        
        java.util.List<com.owaconnector.domain.CalendarConfiguration> result = com.owaconnector.domain.CalendarConfiguration.findAllCalendarConfigurations();        
        org.junit.Assert.assertNotNull("Find all method for 'CalendarConfiguration' illegally returned null", result);        
        org.junit.Assert.assertTrue("Find all method for 'CalendarConfiguration' failed to return any data", result.size() > 0);        
    }    
    
    @Test    
    public void CalendarConfigurationIntegrationTest.testFindCalendarConfigurationEntries() {    
        org.junit.Assert.assertNotNull("Data on demand for 'CalendarConfiguration' failed to initialize correctly", dod.getRandomCalendarConfiguration());        
        long count = com.owaconnector.domain.CalendarConfiguration.countCalendarConfigurations();        
        if (count > 20) count = 20;        
        java.util.List<com.owaconnector.domain.CalendarConfiguration> result = com.owaconnector.domain.CalendarConfiguration.findCalendarConfigurationEntries(0, (int)count);        
        org.junit.Assert.assertNotNull("Find entries method for 'CalendarConfiguration' illegally returned null", result);        
        org.junit.Assert.assertEquals("Find entries method for 'CalendarConfiguration' returned an incorrect number of entries", count, result.size());        
    }    
    
    @Test    
    @Transactional    
    public void CalendarConfigurationIntegrationTest.testFlush() {    
        org.junit.Assert.assertNotNull("Data on demand for 'CalendarConfiguration' failed to initialize correctly", dod.getRandomCalendarConfiguration());        
        java.lang.Long id = dod.getRandomCalendarConfiguration().getId();        
        org.junit.Assert.assertNotNull("Data on demand for 'CalendarConfiguration' failed to provide an identifier", id);        
        com.owaconnector.domain.CalendarConfiguration obj = com.owaconnector.domain.CalendarConfiguration.findCalendarConfiguration(id);        
        org.junit.Assert.assertNotNull("Find method for 'CalendarConfiguration' illegally returned null for id '" + id + "'", obj);        
        boolean modified =  dod.modifyCalendarConfiguration(obj);        
        java.lang.Integer currentVersion = obj.getVersion();        
        obj.flush();        
        org.junit.Assert.assertTrue("Version for 'CalendarConfiguration' failed to increment on flush directive", obj.getVersion() > currentVersion || !modified);        
    }    
    
    @Test    
    @Transactional    
    public void CalendarConfigurationIntegrationTest.testMerge() {    
        org.junit.Assert.assertNotNull("Data on demand for 'CalendarConfiguration' failed to initialize correctly", dod.getRandomCalendarConfiguration());        
        java.lang.Long id = dod.getRandomCalendarConfiguration().getId();        
        org.junit.Assert.assertNotNull("Data on demand for 'CalendarConfiguration' failed to provide an identifier", id);        
        com.owaconnector.domain.CalendarConfiguration obj = com.owaconnector.domain.CalendarConfiguration.findCalendarConfiguration(id);        
        org.junit.Assert.assertNotNull("Find method for 'CalendarConfiguration' illegally returned null for id '" + id + "'", obj);        
        boolean modified =  dod.modifyCalendarConfiguration(obj);        
        java.lang.Integer currentVersion = obj.getVersion();        
        obj.merge();        
        obj.flush();        
        org.junit.Assert.assertTrue("Version for 'CalendarConfiguration' failed to increment on merge and flush directive", obj.getVersion() > currentVersion || !modified);        
    }    
    
    @Test    
    @Transactional    
    public void CalendarConfigurationIntegrationTest.testPersist() {    
        org.junit.Assert.assertNotNull("Data on demand for 'CalendarConfiguration' failed to initialize correctly", dod.getRandomCalendarConfiguration());        
        com.owaconnector.domain.CalendarConfiguration obj = dod.getNewTransientCalendarConfiguration(Integer.MAX_VALUE);        
        org.junit.Assert.assertNotNull("Data on demand for 'CalendarConfiguration' failed to provide a new transient entity", obj);        
        org.junit.Assert.assertNull("Expected 'CalendarConfiguration' identifier to be null", obj.getId());        
        obj.persist();        
        obj.flush();        
        org.junit.Assert.assertNotNull("Expected 'CalendarConfiguration' identifier to no longer be null", obj.getId());        
    }    
    
    @Test    
    @Transactional    
    public void CalendarConfigurationIntegrationTest.testRemove() {    
        org.junit.Assert.assertNotNull("Data on demand for 'CalendarConfiguration' failed to initialize correctly", dod.getRandomCalendarConfiguration());        
        java.lang.Long id = dod.getRandomCalendarConfiguration().getId();        
        org.junit.Assert.assertNotNull("Data on demand for 'CalendarConfiguration' failed to provide an identifier", id);        
        com.owaconnector.domain.CalendarConfiguration obj = com.owaconnector.domain.CalendarConfiguration.findCalendarConfiguration(id);        
        org.junit.Assert.assertNotNull("Find method for 'CalendarConfiguration' illegally returned null for id '" + id + "'", obj);        
        obj.remove();        
        org.junit.Assert.assertNull("Failed to remove 'CalendarConfiguration' with identifier '" + id + "'", com.owaconnector.domain.CalendarConfiguration.findCalendarConfiguration(id));        
    }    
    
}
