/**
 * @author Zane Pan
 */
package com.enterpriseandroid.googleappengineContacts.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.jpa.JpaOptimisticLockingFailureException;

import com.enterpriseandroid.googleappengineContacts.dao.ContactDao;
import com.enterpriseandroid.googleappengineContacts.dao.VersionNotMatchException;
import com.enterpriseandroid.googleappengineContacts.dataModel.Contact;
import com.enterpriseandroid.googleappengineContacts.dataModel.SyncRequest;
import com.enterpriseandroid.googleappengineContacts.dataModel.SyncResult;
import com.enterpriseandroid.googleappengineContacts.service.ContactService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;


public class ContactServiceImpl implements ContactService {
	private static final int MAX_NUM_CONTACT_TO_SYNCH = 1000;
	@Autowired
	@Qualifier(value = "contactDao")
	private ContactDao dao;

	/***
	 *  Although we set the JPA to use strong consistency, but jpa query results are eventual consistency.
	 *  Since our sync logic requires strong consistency for query results of the findChanged query, we have
	 *  to make our query results strong consistency. 
	 *  We have two choice here:
	 *  1. use a cache to provide strong consistency 
	 *  2. Use ancestor query filter.
	 *  
	 *  We have decided to use a cache since it provides the better performance. 
	 *  In the real production, you should use memcached instead of the local cache
	 *  so that the cached data is shared across multiple servers. 
	 *  
	 *  We use a local cache here just to make it easier for demonstrating the concept of using
	 *  a shared cache to achieve the strong consistency level.  
	 */
	private Cache<String, Contact> cache;
	public ContactServiceImpl (){
		cache = CacheBuilder.newBuilder()
				.expireAfterAccess(1000 * 20, TimeUnit.MILLISECONDS)
				.maximumSize(10*1000*1000)
				.build();
	}
	
	@Override
	public void storeOrUpdateContact(Contact c)
			throws IOException {
		try {
			c = dao.storeOrUpdateContact(c);
			cache.put(c.getId(), c);
		} catch (JpaOptimisticLockingFailureException e) {
			throw new VersionNotMatchException("Verson mismatch, contact id : " + c.getId(), e);
		}
	}

	@Override
	public List<Contact> findContactByFirstName(
			String firstName, int start, int numOfmatches) throws IOException {
		
		List<Contact> list =  dao.findContactFirstName(firstName, start, numOfmatches);
		return cacheFindContactByFirstName(list, firstName, numOfmatches);
	}

	
	private List<Contact> cacheFindContactByFirstName(List<Contact> list, String firstName, int numOfmatches) {
		List<Contact> result = new ArrayList<Contact>(list);
		Map<String, Contact > map = cache.asMap();
		for ( Map.Entry<String, Contact> e : map.entrySet()) {
			if ( numOfmatches == result.size() ) {
				break;
			}
			if ( e.getValue().getFirstName().equals(firstName) && !result.contains(e.getValue())) {
				result.add(e.getValue());
			}
		}
		return result;		
	}
	
	@Override
	public List<Contact> getAll(int start, int numOfmatches)
			throws IOException {	
		List<Contact> list = dao.getAll(start, numOfmatches);
		return cacheGetAll(list, numOfmatches-list.size());

	}

	private List<Contact> cacheGetAll(List<Contact> list, int numOfmatches) {
		List<Contact> result = new ArrayList<Contact>(list);
		Map<String, Contact > map = cache.asMap();
		for ( Map.Entry<String, Contact> e : map.entrySet()) {
			if ( numOfmatches == result.size() ) {
				break;
			}
			if ( !result.contains(e.getValue())) {
				result.add(e.getValue());
			}
		}
		return result;
	}
	@Override
	public Contact getContact(String contactId)
			throws IOException {
		
		return dao.getContact(contactId);
	}

	@Override
	public void deleteContact(String contactId)
			throws IOException {
		try {
			dao.delete(contactId);
			cache.invalidate(contactId);
		} catch( org.springframework.orm.jpa.JpaObjectRetrievalFailureException e) {
			// do nothing
		}
		
	}

	@Override
	public List<Contact> findChanged(long timestamp, int start,
			int numOfmatches) {
		List<Contact> list =  dao.findChanged( timestamp, start, numOfmatches);
		return cacheFindChanged(list, timestamp, numOfmatches);
	}

	private List<Contact> cacheFindChanged(List<Contact> list, long timestamp, int numOfmatches) {
		List<Contact> result = new ArrayList<Contact>(list);
		Map<String, Contact > map = cache.asMap();
		for ( Map.Entry<String, Contact> e : map.entrySet()) {
			if ( numOfmatches == result.size() ) {
				break;
			}
			
			if ( e.getValue().getUpdateTime() >= timestamp && !result.contains(e.getValue())) {
				result.add(e.getValue());
			}
		}
		return result;
	}	
	
	
	private boolean checkforId(List<Contact> list, Contact contact) {
		for ( Contact c : list) {
			if (c.getId().equals(contact.getId())) {
				System.out.println( " contact equal :" + c.equals(contact) + " id: " + c.getId());
				if (!c.equals(contact) ) {
					System.out.println( " first :" + c.getFirstName() + " " + contact.getFirstName());
					System.out.println( " version :" + c.getVersion() + " " + contact.getVersion());
					System.out.println( " update :" + c.getUpdateTime() + " " + contact.getUpdateTime());

				}
				return true;
			}
		}
		return false;
	}
	@Override
	public SyncResult sync(SyncRequest syncR) throws IOException {
		try {
			List<Contact> modified = syncR.getModified();
			 /** 
			  * The now time will be used for next sync.
			  * It is possible that another client can modified the data after the "now" timestamp before the call to findChanged. 
			  * In this case the modified data is already included in the changedData set, however
			  * the next sync, the modified data will be included in the changedData set again since 
			  * its update time is great than "now"
			  *                                                   
			  */
			Long now = new Long (System.currentTimeMillis());
			List<Contact> conflict = new ArrayList<Contact>();
			
			List<Contact> serverSideChangedData = findChanged(syncR.getSyncTime(), 0, MAX_NUM_CONTACT_TO_SYNCH);
			SyncResult ret = new SyncResult(serverSideChangedData, conflict, now);
	
			for (Contact c : modified) {
				try {
					storeOrUpdateContact(c);
				} catch (VersionNotMatchException e) {
					/***
					 * The client's version does not match the server's version
					 * The server must changed its data since last sync
					 */
					Contact conflictContact = findContact(
							serverSideChangedData, c);
					if (conflictContact != null) {
						/***
						 * confirms that server did change the data since last
						 * sync.
						 */
						conflict.add(conflictContact);
					} else {
						conflictContact = getContact(c.getId());
						if (conflictContact.getVersion() > c.getVersion()) {
							conflict.add(conflictContact);
						} else {
							/***
							 * we got here because the client has a new version
							 * that the server's It must be client is sending
							 * the wrong data Wrong.
							 */
							throw new IllegalArgumentException(
									"Client is sending the wrong version of the data");
						}
					}
				}
			}
			removeContact(serverSideChangedData, modified);
			return ret;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private void removeContact(List<Contact> contactList, Contact contact) {
		for ( Contact c : contactList) {
			if (c.getId().equals(contact.getId())) {
				contactList.remove(c);
				return;
			}
		}		
	}
	
	private void removeContact(List<Contact> srcList, List<Contact> removeList) {
		for ( Contact c : removeList) {
			removeContact(srcList, c);
		}
	}
	private Contact findContact(List<Contact> list, Contact c) {
		for (Contact contact : list) {
			if ( contact.getId().equals(c.getId())) {
				return contact;
			}
		}
		return null;
	}
	

}
