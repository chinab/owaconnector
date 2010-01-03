package com.owaconnector.domain;

import com.owaconnector.domain.CalendarConfiguration;
import java.lang.Integer;
import java.lang.Long;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Version;
import org.springframework.transaction.annotation.Transactional;

privileged aspect CalendarConfiguration_Roo_Entity {
    
    @PersistenceContext    
    transient EntityManager CalendarConfiguration.entityManager;    
    
    @Id    
    @GeneratedValue(strategy = GenerationType.AUTO)    
    @Column(name = "id")    
    private Long CalendarConfiguration.id;    
    
    @Version    
    @Column(name = "version")    
    private Integer CalendarConfiguration.version;    
    
    public Long CalendarConfiguration.getId() {    
        return this.id;        
    }    
    
    public void CalendarConfiguration.setId(Long id) {    
        this.id = id;        
    }    
    
    public Integer CalendarConfiguration.getVersion() {    
        return this.version;        
    }    
    
    public void CalendarConfiguration.setVersion(Integer version) {    
        this.version = version;        
    }    
    
    @Transactional    
    public void CalendarConfiguration.persist() {    
        if (this.entityManager == null) this.entityManager = entityManager();        
        this.entityManager.persist(this);        
    }    
    
    @Transactional    
    public void CalendarConfiguration.remove() {    
        if (this.entityManager == null) this.entityManager = entityManager();        
        if (this.entityManager.contains(this)) {        
            this.entityManager.remove(this);            
        } else {        
            CalendarConfiguration attached = this.entityManager.find(CalendarConfiguration.class, this.id);            
            this.entityManager.remove(attached);            
        }        
    }    
    
    @Transactional    
    public void CalendarConfiguration.flush() {    
        if (this.entityManager == null) this.entityManager = entityManager();        
        this.entityManager.flush();        
    }    
    
    @Transactional    
    public void CalendarConfiguration.merge() {    
        if (this.entityManager == null) this.entityManager = entityManager();        
        CalendarConfiguration merged = this.entityManager.merge(this);        
        this.entityManager.flush();        
        this.id = merged.getId();        
    }    
    
    public static final EntityManager CalendarConfiguration.entityManager() {    
        EntityManager em = new CalendarConfiguration().entityManager;        
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");        
        return em;        
    }    
    
    public static long CalendarConfiguration.countCalendarConfigurations() {    
        return (Long) entityManager().createQuery("select count(o) from CalendarConfiguration o").getSingleResult();        
    }    
    
    public static List<CalendarConfiguration> CalendarConfiguration.findAllCalendarConfigurations() {    
        return entityManager().createQuery("select o from CalendarConfiguration o").getResultList();        
    }    
    
    public static CalendarConfiguration CalendarConfiguration.findCalendarConfiguration(Long id) {    
        if (id == null) throw new IllegalArgumentException("An identifier is required to retrieve an instance of CalendarConfiguration");        
        return entityManager().find(CalendarConfiguration.class, id);        
    }    
    
    public static List<CalendarConfiguration> CalendarConfiguration.findCalendarConfigurationEntries(int firstResult, int maxResults) {    
        return entityManager().createQuery("select o from CalendarConfiguration o").setFirstResult(firstResult).setMaxResults(maxResults).getResultList();        
    }    
    
}
