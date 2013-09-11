/**
 * @author Zane Pan
 */
package com.enterpriseandroid.springServiceContacts.service.impl;

import com.enterpriseandroid.springServiceContacts.dao.ContactDao;
import com.enterpriseandroid.springServiceContacts.dataModel.Contact;
import com.enterpriseandroid.springServiceContacts.service.ContactService;

import java.io.IOException;
import java.util.List;

public class ContactServiceImpl implements ContactService {
	private static final int MAX_NUM_CONTACT_TO_SYNCH = 1000;
	
	private ContactDao dao;
	
	public void setContactDao(ContactDao dao) {
		this.dao = dao;
	}
	
	@Override
	public void storeOrUpdateContact(Contact c) throws IOException {
		dao.storeOrUpdateContact(c);
	}
	@Override
	public List<Contact> findContactByFirstName(String firstName, int start, int numOfmatches) throws IOException {
		return dao.findContactFirstName(firstName, start, numOfmatches);
	}
	@Override
	public Contact getContact(Long id) throws IOException {
		return dao.getContact(id);
	}
	
	@Override
	public void deleteContact(Long id) throws IOException {
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
