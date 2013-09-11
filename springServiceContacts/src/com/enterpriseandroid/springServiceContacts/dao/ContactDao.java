package com.enterpriseandroid.springServiceContacts.dao;

import java.io.IOException;
import java.util.List;

import com.enterpriseandroid.springServiceContacts.dataModel.Contact;

public interface ContactDao {
	Contact getContact(Long id) throws IOException ;
	Long storeOrUpdateContact(Contact contact) throws IOException ;
	List<Contact> findContactFirstName(String firstName, int start, int numOfmatches);
	List<Contact> findChanged(long timestamp, int start, int numOfmatches);
	void delete(Long id) throws IOException ;
	List<Contact> getAll(int start, int numOfmatches) throws IOException ;;
}
