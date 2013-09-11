package com.enterpriseandroid.googleappengineContacts.dao;

import java.io.IOException;
import java.util.List;

import com.enterpriseandroid.googleappengineContacts.dataModel.Contact;

public interface ContactDao {
	Contact getContact(String id) throws IOException ;
	Contact storeOrUpdateContact(Contact contact) throws IOException ;
	List<Contact> findContactFirstName(String firstName, int start, int numOfmatches);
	List<Contact> findChanged( long timestamp, int start, int numOfmatches);
	void delete( String id) throws IOException ;
	List<Contact> getAll( int start, int numOfmatches) throws IOException ;
}
