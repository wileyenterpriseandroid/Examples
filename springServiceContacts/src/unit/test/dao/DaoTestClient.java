package unit.test.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;


import com.enterpriseandroid.springServiceContacts.dao.ContactDao;
import com.enterpriseandroid.springServiceContacts.dataModel.Contact;

public class DaoTestClient {
	protected ContactDao dao;
	
	public DaoTestClient(ContactDao dao) {
		super();
		this.dao = dao;
	}


	public void testFind() throws IOException {
		for ( int i=0;i<5; i++) {
			Contact c = createContact("john123");
			dao.storeOrUpdateContact(c);
		}
		List<Contact> list = dao.findContactFirstName("john123", 0, 10);
		assertTrue(list.size() == 5);
		for(Contact cc : list) {
			assertEquals("john123", cc.getFirstName());
			dao.delete(cc.getId());
		}
		
	}
	
	
	public void testStore() throws IOException {
		Contact c = createContact("john456");
		dao.storeOrUpdateContact(c);
		Contact c2 = dao.getContact(c.getId());
		assertEquals(c.getFirstName(), c2.getFirstName());
		assertEquals(c.getId(), c2.getId());

		System.out.println(c2.getId());
		dao.delete(c2.getId());
	}
	
	

	public void testUpdate() throws IOException {
		Contact c = createContact("john456");
		dao.storeOrUpdateContact(c);
		//c = dao.getContact(c.getId());
		c.setFirstName("modified john");
		dao.storeOrUpdateContact(c);
		Contact c2 = dao.getContact(c.getId());
		assertEquals("modified john", c2.getFirstName());
		dao.delete(c2.getId());
	}
	

	public void testgetAll() throws IOException {
		for ( int i=0;i<5; i++) {
			Contact c = createContact("john123");
			dao.storeOrUpdateContact(c);
		}
		List<Contact> list = dao.getAll(0, 10);
		assertEquals(list.size(),5);
	
		for(Contact cc : list) {
			assertEquals("john123", cc.getFirstName());
			dao.delete(cc.getId());
		}
			
	}
	

	public void testFindChanged() throws IOException {
		for ( int i=0;i<5; i++) {
			Contact c = createContact("john_" + i);
			dao.storeOrUpdateContact(c);
		}
		long t = System.currentTimeMillis();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {			
			e.printStackTrace();
		}

		for ( int i=0;i<5; i++) {
			Contact c = createContact("john123" );
			dao.storeOrUpdateContact(c);
		}
		List<Contact> list = dao.findChanged(t, 0, 10);		
		assertTrue(list.size() == 5);
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
	
 	public Contact createContact(String firstName) {
		Contact c = new Contact();
		c.setFirstName(firstName);
		c.setLastName("Smith");
		c.setEmail("jsmith@yahoo.com");
		c.setPhone("123-456-7890");
		return c;
	}

}
