package unit.test.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import com.enterpriseandroid.springServiceContacts.dao.ContactDao;
import com.enterpriseandroid.springServiceContacts.dataModel.Contact;

public class DaoTestClient {

	private ContactDao dao;
	public DaoTestClient(ContactDao dao) {
		this.dao = dao;
	}
	

	public void testUpdate() throws IOException {
		String firstName = "john";
		String updatedFirstName = "updated " + firstName;
		String id = "testUpdate-id-100";
		createOrUpdate(id,firstName);
		Contact contact = dao.getContact(id);
		assertEquals(firstName, contact.getFirstName());
		assertEquals(1, contact.getVersion());
		contact.setFirstName(updatedFirstName);
		dao.storeOrUpdateContact(contact);
		contact = dao.getContact(id);
		assertEquals(updatedFirstName, contact.getFirstName());
		assertEquals(2, contact.getVersion());
		dao.delete(id);
	}
	
	public void testFind() throws IOException {
		String idPrefix = "testFind-id-";
		String firstName = "find john Test";
		
	

		for ( int i = 0; i< 10; i++) {
			if ( i < 5 ) {
				createOrUpdate(idPrefix + i, "xyz");
			} else {
				createOrUpdate(idPrefix + i, firstName);
			}
			
		}
		
		int start = 0;
		int numOfmatchers = 2;
		List<Contact> contacts = dao.findContactFirstName(firstName, start, numOfmatchers);
		assertEquals(numOfmatchers, contacts.size());
		
		for( int i=0; i<numOfmatchers; i++) {
			assertEquals(firstName, contacts.get(i).getFirstName());
		}
		
		contacts = dao.findContactFirstName(firstName, start, 10);
		assertEquals(5, contacts.size());
		for ( Contact c : contacts ) {
			assertEquals(firstName, c.getFirstName());
		}
		
		for ( int i = 0; i<10; i++) {
			dao.delete (idPrefix + i);
		}
		
	}
	
	public void testFindChanged() throws IOException {
		String idPrefix = "testFindChanged-";
		for ( int i=0;i<5; i++) {
			createOrUpdate( idPrefix + i, "john_" + i);
		}
		long t = System.currentTimeMillis();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {			
			e.printStackTrace();
		}

		for ( int i=5;i<10; i++) {
			createOrUpdate(idPrefix + i, "john123" );
		}
		List<Contact> list = dao.findChanged(t, 0, 10);		
		assertTrue(list.size() == 5);
		for(Contact cc : list) {
			assertEquals("john123", cc.getFirstName());			
			dao.delete(cc.getId());
		}		
	}	
	
	
	public void testgetAll() throws IOException {
		String idPrefix = "testAll-";
		for ( int i=0;i<5; i++) {
			createOrUpdate(idPrefix + i, "john123");
		}
		List<Contact> list = dao.getAll(0, 10);
		assertEquals(list.size(),5);
	
		for(Contact cc : list) {
			assertEquals("john123", cc.getFirstName());
			dao.delete(cc.getId());
		}
			
	}
	
	public void removeALl() throws IOException {
		for ( Contact c : dao.getAll(0, 1000)) {
			dao.delete(c.getId());
		}
	}
	
	public void testFindChangedPaging() throws IOException {
		String idPrefix = "testFindChanged-";
		long t = System.currentTimeMillis();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {			
			e.printStackTrace();
		}

		for ( int i=0;i<5; i++) {
			createOrUpdate(idPrefix + i, "john123" );
		}
		int start = 1;
		int numOfMatchers = 2;
		List<Contact> list = dao.findChanged(t, start, numOfMatchers);
		for ( int i=0; i<list.size(); i++) {
			assertEquals(idPrefix + (i+start), list.get(i).getFirstName());
		}	
	}
	
	private void createOrUpdate(String id, String firstName) throws IOException {
		Contact contact = createContact(id,firstName);
		dao.storeOrUpdateContact(contact);

	}
	private Contact createContact(String id, String firstName) {
		Contact c = new Contact();
		c.setId(id);
		c.setFirstName(firstName);
		c.setLastName("Smith");
		c.setEmail("jsmith@yahoo.com");
		c.setPhone("123-456-7890");
		return c;
	}

}
