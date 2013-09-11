package com.enterpriseandroid.googleappengineContacts.dao.impl;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;


import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.enterpriseandroid.googleappengineContacts.dao.ContactDao;
import com.enterpriseandroid.googleappengineContacts.dao.VersionNotMatchException;
import com.enterpriseandroid.googleappengineContacts.dataModel.Contact;




@Repository
@Qualifier("contactDao")
@Transactional
public class ContactDaoGoogleAppEngineImpl implements ContactDao  {
    private static final Logger log = Logger
            .getLogger(ContactDaoGoogleAppEngineImpl.class.getName());
    
  
    
    @PersistenceContext
    private EntityManager entityManager;

    public ContactDaoGoogleAppEngineImpl () {

    }
    
    @Override
    public Contact getContact(String id) throws IOException {
    	try {
    		Contact contact = entityManager.find(Contact.class, id);
    		return contact;
    	} catch ( javax.persistence.EntityNotFoundException e) {
    		return null;
    	}
    }

    @Override
    public Contact storeOrUpdateContact(Contact contact) throws IOException {
    	contact.setUpdateTime(System.currentTimeMillis());
        String id = contact.getId();
    	if ( id == null ) {
    		createContact(contact);
    		return contact;
    	}
      	Contact oldContact = getContact(id) ;
  		 
      	if ( oldContact == null) {
      		/***
      		 * Client should only UUID for the id, so there is not going to be a case such that
      		 * two clients create contact with the same id. 
      		 */
      		createContact(contact);
      		return contact;
      	} 
      	
      	if ( contact.getVersion() !=(oldContact.getVersion())) {
      		throw new VersionNotMatchException("Verson mismatch, contact id : " + contact.getId());
      	}
      	copyContact(contact, oldContact);
      	return oldContact;
	}

    private void copyContact(Contact src, Contact dest) {
    	dest.setFirstName(src.getFirstName());
    	dest.setLastName(src.getLastName());
      	dest.setEmail(src.getEmail());
      	dest.setDeleted(src.isDeleted());
      	dest.setUpdateTime(src.getUpdateTime());
      	//dest.setVersion(src.getVersion());
    }
    
    private void createContact(final Contact contact) {
		if ( contact.getId() == null) {
			contact.setId(UUID.randomUUID().toString());
		}
		try {
			entityManager.persist(contact);
		} catch (Exception e ) {
			e.printStackTrace();
		}
    }   
 
    
    @Override
    public List<Contact> findContactFirstName(String firstName,
                                              int start, int numOfmatches)
    {
        TypedQuery<Contact> query = entityManager.createQuery(
                "SELECT c FROM Contact c where c.firstName = ?1 and  c.deleted = false",
                Contact.class);
        query.setParameter(1, firstName);
        query.setFirstResult(start);
        query.setMaxResults(numOfmatches);
		List<Contact> list = query.getResultList();
		return list;
    }

    @Override
    public List<Contact> findChanged(long timestamp, int start,
                                     int numOfmatches) {
        TypedQuery<Contact> query = entityManager.createQuery(
                "SELECT c FROM Contact c where c.updateTime >= ?1",
                Contact.class);
        query.setParameter(1, timestamp);
        query.setFirstResult(start);
        query.setMaxResults(numOfmatches);
        List<Contact> list = query.getResultList();
        return list;
    }

    @Override
    public void delete(String id) throws IOException {
        log.info( "delete: "+ id);
        try {
            Contact c = getContact(id);
            if ( c != null) {
            	entityManager.remove(c);
            }
        } catch (Exception e ) {
            e.printStackTrace();
        }
        entityManager.getEntityManagerFactory().getCache().evictAll();
    }

    @Override
    public List<Contact> getAll(int start, int numOfmatches)
            throws IOException
    {
        TypedQuery<Contact> query =
                entityManager.createQuery("SELECT c FROM Contact c",
                        Contact.class);
        return query.getResultList();
  
    }

}
