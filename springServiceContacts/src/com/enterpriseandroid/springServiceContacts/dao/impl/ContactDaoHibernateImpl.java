package com.enterpriseandroid.springServiceContacts.dao.impl;

import com.enterpriseandroid.springServiceContacts.dao.ContactDao;
import com.enterpriseandroid.springServiceContacts.dao.VersionNotMatchException;
import com.enterpriseandroid.springServiceContacts.dataModel.Contact;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;


public class ContactDaoHibernateImpl extends HibernateDaoSupport implements ContactDao {
	private final static org.apache.log4j.Logger log = Logger.getLogger(ContactDaoHibernateImpl.class);
	
	@Override
	public Contact getContact(Long id) throws IOException {
		return getHibernateTemplate().get(Contact.class, id);
	}

	@Override
	public Long storeOrUpdateContact(Contact contact) throws IOException {
		try {
			log.info(" storeOrUpdate: " + contact.getEmail());
			contact.setUpdateTime(System.currentTimeMillis());
			if ( contact.getVersion() == null ) {
				contact.setVersion(1L);
			}
			getHibernateTemplate().saveOrUpdate(contact);
		} catch (HibernateOptimisticLockingFailureException e ) {
			throw new VersionNotMatchException("version does not match", e);
		}
		return contact.getId();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Contact> findContactFirstName(String firstName, int start,
			int numOfmatches) {
		String hql="from Contact where firstName = :firstName";
		String[] paramNames = new String[] {"firstName"};
		Object[] values = new Object[] {firstName};
		
		HibernateCallbackImpl action = new HibernateCallbackImpl(hql, paramNames, values, start, numOfmatches);
		return getHibernateTemplate().executeFind(action);

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Contact> findChanged(long timestamp, int start, int numOfmatches) {
		   String hql="from Contact where updateTime > :timestamp";
			String[] paramNames = new String[] {"timestamp"};
			Object[] values = new Object[] {new Long(timestamp)};
			HibernateCallbackImpl action = new HibernateCallbackImpl(hql, paramNames, values, start, numOfmatches);
			return getHibernateTemplate().executeFind(action);
	}

	@Override
	public void delete(Long id) throws IOException {
		 getHibernateTemplate().delete(getContact(id));

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Contact> getAll(int start, int numOfmatches)
			throws IOException {
		   String hql="from Contact where deleted = false";
		   return getHibernateTemplate().find(hql);

	}

	static class HibernateCallbackImpl implements HibernateCallback<List<Contact>> {
		private String queryString;
		int firstResult;
		int maxResults;
		String[] paramNames;
		Object[] values;
		public HibernateCallbackImpl(
	            String queryString, 
	            String[] paramNames, 
	            Object[] values,
	            int firstResult,
	            int maxResults) {
	        this.queryString = queryString;
	        this.paramNames = paramNames;
	        this.values = values;
	
	        this.firstResult = firstResult;
	        this.maxResults = maxResults;
	    }

	    @Override
	    public List<Contact> doInHibernate(Session session) throws HibernateException,
	            SQLException {
	        Query query = session.createQuery(queryString);
	        query.setFirstResult(firstResult);
	        query.setMaxResults(maxResults);
	
	        for (int c=0; c<paramNames.length; c++) {
	            query.setParameter(paramNames[c], values[c]);
	        }
	
	        @SuppressWarnings("unchecked")
	        List<Contact> result = query.list();
	
	        return result;
	    }

	}
  
}
