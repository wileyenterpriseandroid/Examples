/**
 * @author Zane Pan
 */
package com.enterpriseandroid.springServiceContacts.service;

import java.io.IOException;
import java.util.List;


import com.enterpriseandroid.springServiceContacts.dataModel.Contact;
import com.enterpriseandroid.springServiceContacts.dataModel.SyncRequest;
import com.enterpriseandroid.springServiceContacts.dataModel.SyncResult;

public interface ContactService {
	Contact storeOrUpdateContact(Contact c) throws IOException ;
	List<Contact> findContactByFirstName(String firstName, int start, int numOfmatches) throws IOException;
	List<Contact> getAll( int start, int numOfmatches) throws IOException;
	Contact getContact(String id) throws IOException ;	
	void deleteContact(String id) throws IOException;
	List<Contact> findChanged(long timestamp, int start, int numOfmatches);
	SyncResult sync(SyncRequest syncR) throws IOException;
}
