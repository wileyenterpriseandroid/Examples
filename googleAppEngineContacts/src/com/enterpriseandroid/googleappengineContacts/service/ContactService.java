/**
 * @author Zane Pan
 */
package com.enterpriseandroid.googleappengineContacts.service;

import java.io.IOException;
import java.util.List;



import com.enterpriseandroid.googleappengineContacts.dataModel.Contact;
import com.enterpriseandroid.googleappengineContacts.dataModel.SyncRequest;
import com.enterpriseandroid.googleappengineContacts.dataModel.SyncResult;

public interface ContactService {
	
	void storeOrUpdateContact( Contact c) throws IOException ;
	
	List<Contact> findContactByFirstName(String firstName, int start, int numOfmatches) throws IOException;
	List<Contact> getAll(int start, int numOfmatches) throws IOException;
	Contact getContact(String contactId) throws IOException ;	
	void deleteContact( String contactId) throws IOException;
	List<Contact> findChanged(long timestamp, int start, int numOfmatches);
	
	SyncResult sync(SyncRequest syncR) throws IOException;
}
