package unit.test.rest;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.enterpriseandroid.springServiceContacts.dataModel.Contact;
import com.enterpriseandroid.springServiceContacts.dataModel.SyncResult;




@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( { "/spring/applicationContext*.xml" })

public class SpringSyncServiceContactsTest {
	private final static org.apache.log4j.Logger log = Logger.getLogger(SpringSyncServiceContactsTest.class);
	
	@Autowired
	@Qualifier(value = "testClient")
	private TestClient testClient;

	
	@Before
	public void setup() {
		log.info(" calling setup");
		Contact[] contacts = testClient.getAll();
		for ( Contact c : contacts ) {
			System.out.println(" delete id: "+ c.getId());
			testClient.restDeleteById(c.getId());
		}
	}
	
	@Test
	public void testUpdate() {
		String firstName = "john";
		String updatedFirstName = "updated " + firstName;
		String id = "testUpdate-id-100";
		String url = testClient.restCreateOrUpdate(id,firstName);
		Contact contact = testClient.restGet(url);
		assertEquals(firstName, contact.getFirstName());
		assertEquals(1, contact.getVersion());
		contact.setFirstName(updatedFirstName);
		url = testClient.restCreateOrUpdate(contact);
		contact = testClient.restGet(url);
		assertEquals(updatedFirstName, contact.getFirstName());
		assertEquals(2, contact.getVersion());
		testClient.restDeleteById(id);
	}
	
	@Test 
	public void testFind() {
		int num = 5;
		String idPrefix = "testFind-id-";
		String firstName = "john getAll Test";
		for ( int i = 0; i< num; i++) {
			testClient.restCreateOrUpdate (idPrefix + i, firstName);
		}
		
		int start = 0;
		int numOfmatchers = 2;
		Contact[] contacts = testClient.findByFirstName(firstName, start, numOfmatchers);
		assertEquals(numOfmatchers, contacts.length);
		
		for( int i=0; i<numOfmatchers; i++) {
			assertEquals(firstName, contacts[i].getFirstName());
		}
		
		contacts = testClient.findByFirstName(firstName, start, num);
		assertEquals(num, contacts.length);
		for ( Contact c : contacts ) {
			assertEquals(firstName, c.getFirstName());
		}
		
		for ( int i = 0; i<num; i++) {
			testClient.restDeleteById (idPrefix + i);
		}
		
	}
	@Test
	public void testConflict() throws InterruptedException {
		long now = System.currentTimeMillis();
		Thread.sleep(10);
		String id1 = "testConflict-id-1";
		String id2 = "testConflict-id-2";
		String id3 = "testConflict-id-3";
		
		String url1 = testClient.restCreateOrUpdate(id1, "john1");
		String url2 = testClient.restCreateOrUpdate(id2, "john");
		Contact conflictContact = testClient.restGet(url1);
		conflictContact.setFirstName("conflict Contact");
		testClient.restCreateOrUpdate(conflictContact);
		conflictContact.setFirstName("*conflict Contact");

		Contact contact = testClient.createContact(id3, "syncName");

		List<Contact> contactList = new ArrayList<Contact>();
		contactList.add(contact);
		contactList.add(conflictContact);
	
		SyncResult result = testClient.sync(contactList, now);
		assertEquals(1, result.getConflicts().size());
		testClient.restDeleteById(id1);
		testClient.restDeleteById (id2);
		testClient.restDeleteById (id3);
		
	}

	@Test
	public void testSync() throws InterruptedException {
		long now = System.currentTimeMillis();
			String id1 = "testSync-id-1";
		Thread.sleep(10);
		String id2 = "testSync-id-2";
		String id3 = "testSync-id-3";
		testClient.restCreateOrUpdate(id1, "john1");
		testClient.restCreateOrUpdate(id2, "john2");
		Contact contact = testClient.createContact(id3, "syncName");

		List<Contact> contactList = new ArrayList<Contact>();
		contactList.add(contact);

		SyncResult result = testClient.sync(contactList, now);
		System.out.println(" size : " + result.getModified().size());

		assertEquals(2, result.getModified().size());
		testClient.restDeleteById(id1);
		testClient.restDeleteById (id2);
		testClient.restDeleteById (id3);

	}
	
	@Test
	public void testGetAll() throws InterruptedException {
		int num = 5;
		String idPrefix = "testGetAll-id-";
		String firstName = "john getAll Test";
		for ( int i = 0; i< num; i++) {
			testClient.restCreateOrUpdate (idPrefix + i, firstName);
		}
		Contact[] contacts = testClient.getAll();
		assertEquals(num, contacts.length);
		for ( Contact c : contacts ) {
			assertEquals(firstName, c.getFirstName());
		}
		
		for ( int i = 0; i<num; i++) {
			testClient.restDeleteById (idPrefix + i);
		}
		
	}
	
}
