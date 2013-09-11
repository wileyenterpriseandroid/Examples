/**
 * @author Zane Pan
 */
package com.enterpriseandroid.springServiceContacts.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.enterpriseandroid.springServiceContacts.dao.ContactDao;
import com.enterpriseandroid.springServiceContacts.dao.VersionNotMatchException;
import com.enterpriseandroid.springServiceContacts.dataModel.Contact;
import com.enterpriseandroid.springServiceContacts.dataModel.SyncRequest;
import com.enterpriseandroid.springServiceContacts.dataModel.SyncResult;
import com.enterpriseandroid.springServiceContacts.service.ContactService;

public class ContactServiceImpl implements ContactService {
	private static final int MAX_NUM_CONTACT_TO_SYNCH = 1000;
	
	private ContactDao dao;
	
	public void setContactDao(ContactDao dao) {
		this.dao = dao;
	}
	
	@Override
	public Contact storeOrUpdateContact(Contact c) throws IOException {
		return dao.storeOrUpdateContact(c);
	}
	@Override
	public List<Contact> findContactByFirstName(String firstName, int start, int numOfmatches) throws IOException {
		return dao.findContactFirstName(firstName, start, numOfmatches);
	}
	@Override
	public Contact getContact(String id) throws IOException {
		return dao.getContact(id);
	}
	
	@Override
	public void deleteContact(String id) throws IOException {
		dao.delete(id);
	}

	@Override
	public List<Contact> getAll(int start, int numOfmatches)
			throws IOException {	
		return dao.getAll(start, numOfmatches);
	}
	
	@Override
	public List<Contact> findChanged(long timestamp, int start, int numOfmatches) {
		return dao.findChanged(timestamp, start, numOfmatches);
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
			/***
			 * For now, we only support the number records changes on the server side to max 1000
			 */
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
