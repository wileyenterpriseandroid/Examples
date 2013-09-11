package com.enterpriseandroid.awsContacts.dao;

import java.io.IOException;
import java.util.List;

import com.enterpriseandroid.awsContacts.dataModel.Contact;

public interface ContactDao {
	Contact getContact(String userId, String id) throws IOException ;
	String storeOrUpdateContact(String userId, Contact contact) throws IOException ;
	List<Contact> findContactFirstName(String userId, String firstName, int start, int numOfmatches);
	List<Contact> findChanged(String userId, long timestamp, int start, int numOfmatches);
	void delete(String userId, String id) throws IOException ;
	List<Contact> getAll(String userId, int start, int numOfmatches) throws IOException ;;
}
