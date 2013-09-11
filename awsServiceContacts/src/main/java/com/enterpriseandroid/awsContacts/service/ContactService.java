package com.enterpriseandroid.awsContacts.service;

import java.io.IOException;
import java.util.List;


import com.enterpriseandroid.awsContacts.dataModel.Contact;
import com.enterpriseandroid.awsContacts.dataModel.SyncRequest;
import com.enterpriseandroid.awsContacts.dataModel.SyncResult;


public interface ContactService {
	String storeOrUpdateContact(String userId, Contact c) throws IOException ;
	List<Contact> findContactByFirestName(String userId, String firstName, int start, int numOfmatches) throws IOException;
	List<Contact> getAll(String userId, int start, int numOfmatches) throws IOException;
	Contact getContact(String userId, String contactId) throws IOException ;	
	void deleteContact(String userId, String contactId) throws IOException;
	List<Contact> findChanged(String userId, long timestamp, int start, int numOfmatches);
	SyncResult sync(String userId, SyncRequest syncR) throws IOException;
}
