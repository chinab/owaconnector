package com.owaconnector.domain;

import com.owaconnector.domain.CalendarUser;
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

privileged aspect CalendarUser_Roo_Entity {
    
    @PersistenceContext    
    transient EntityManager CalendarUser.entityManager;    
    
    @Id    
    @GeneratedValue(strategy = GenerationType.AUTO)    
    @Column(name = "id")    
    private Long CalendarUser.id;    
    
    @Version    
    @Column(name = "version")    
    private Integer CalendarUser.version;    
    
    public Long CalendarUser.getId() {    
        return this.id;        
    }    
    
    public void CalendarUser.setId(Long id) {    
        this.id = id;        
    }    
    
    public Integer CalendarUser.getVersion() {    
        return this.version;        
    }    
    
    public void CalendarUser.setVersion(Integer version) {    
        this.version = version;        
    }    
    
    @Transactional    
    public void CalendarUser.persist() {    
        if (this.entityManager == null) this.entityManager = entityManager();        
        this.entityManager.persist(this);        
    }    
    
    @Transactional    
    public void CalendarUser.remove() {    
        if (this.entityManager == null) this.entityManager = entityManager();        
        if (this.entityManager.contains(this)) {        
            this.entityManager.remove(this);            
        } else {        
            CalendarUser attached = this.entityManager.find(CalendarUser.class, this.id);            
            this.entityManager.remove(attached);            
        }        
    }    
    
    @Transactional    
    public void CalendarUser.flush() {    
        if (this.entityManager == null) this.entityManager = entityManager();        
        this.entityManager.flush();        
    }    
    
    @Transactional    
    public void CalendarUser.merge() {    
        if (this.entityManager == null) this.entityManager = entityManager();        
        CalendarUser merged = this.entityManager.merge(this);        
        this.entityManager.flush();        
        this.id = merged.getId();        
    }    
    
    public static final EntityManager CalendarUser.entityManager() {    
        EntityManager em = new CalendarUser().entityManager;        
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");        
        return em;        
    }    
    
    public static long CalendarUser.countCalendarUsers() {    
        return (Long) entityManager().createQuery("select count(o) from CalendarUser o").getSingleResult();        
    }    
    
    public static List<CalendarUser> CalendarUser.findAllCalendarUsers() {    
        return entityManager().createQuery("select o from CalendarUser o").getResultList();        
    }    
    
    public static CalendarUser CalendarUser.findCalendarUser(Long id) {    
        if (id == null) throw new IllegalArgumentException("An identifier is required to retrieve an instance of CalendarUser");        
        return entityManager().find(CalendarUser.class, id);        
    }    
    
    public static List<CalendarUser> CalendarUser.findCalendarUserEntries(int firstResult, int maxResults) {    
        return entityManager().createQuery("select o from CalendarUser o").setFirstResult(firstResult).setMaxResults(maxResults).getResultList();        
    }    
    
}
