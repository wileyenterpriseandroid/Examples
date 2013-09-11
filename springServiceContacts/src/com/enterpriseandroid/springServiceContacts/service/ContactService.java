/**
 * @author Zane Pan
 */
package com.enterpriseandroid.springServiceContacts.service;

import com.enterpriseandroid.springServiceContacts.dataModel.Contact;

import java.io.IOException;
import java.util.List;

public interface ContactService {
	void storeOrUpdateContact(Contact c) throws IOException ;
	List<Contact> findContactByFirstName(String firstName, int start, int numOfmatches) throws IOException;
	List<Contact> getAll( int start, int numOfmatches) throws IOException;
	Contact getContact(Long id) throws IOException ;
	void deleteContact(Long id) throws IOException;
	List<Contact> findChanged(long timestamp, int start, int numOfmatches);
}
