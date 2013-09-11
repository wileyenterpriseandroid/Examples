package unit.test.rest;

import com.enterpriseandroid.springServiceContacts.dataModel.Contact;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( { "/spring/applicationContext*.xml" })
public class ContactRestTest {
	private final static org.apache.log4j.Logger log = Logger.getLogger(ContactRestTest.class);
	
	@Autowired
	@Qualifier(value = "restTemplate")
	protected RestTemplate restTemplate;
	
	private String user = "exampleUser";
	private String password = "examplePassword";
	@Test
	public void testCreate() {
		String url = restCreate("john");
		delete(url);
		
	}

	 	
	@Test
	public void testUpdate() {
		String updatedFirst="updated FirstName";
		String url = restCreate("john");
		Contact contact = restGet(url);
		contact.setFirstName(updatedFirst);
		url = restUpdate(url, contact);
		contact = restGet(url);
		assertEquals(updatedFirst, contact.getFirstName());
	}
	
	//@Test
	public void testGetAll() {
		for ( int i = 0; i< 5; i++) {
			restCreate ("john");
		}
		Contact[] contacts = getAll();
		assertTrue(contacts.length == 5);
		for ( Contact c : contacts ) {
			assertEquals("john", c.getFirstName());
		}
		
	}
	
	private void delete(String deleteUrl) {		
		HttpHeaders header = createHttpHeaders();
		HttpEntity<Contact> requestEntity = new  HttpEntity<Contact>(header);
		ResponseEntity<String> response = restTemplate.exchange(deleteUrl + "?delete=true",
				  HttpMethod.DELETE, requestEntity, String.class);
		log.info(response.getStatusCode());	
		
	}
	
	private Contact[] getAll() {
		try {
			String getAllUrl = "http://localhost:8080/springServiceContacts/Contacts";		
			HttpHeaders header = createHttpHeaders();
			HttpEntity<Contact> requestEntity = new  HttpEntity<Contact>(header);
			ResponseEntity<Contact[]> response = restTemplate.exchange(getAllUrl,
					  HttpMethod.GET, requestEntity, Contact[].class);
			return response.getBody();
		} catch (HttpClientErrorException e) {
			log.info(e.getResponseBodyAsString());
			throw e;
		}				
	}
	
	private Contact restGet(String url) {
		HttpHeaders header = createHttpHeaders();
		HttpEntity<Contact> requestEntity = new  HttpEntity<Contact>(header);
		ResponseEntity<Contact> response = restTemplate.exchange(url,
				  HttpMethod.GET, requestEntity, Contact.class);
		return response.getBody();		
	}

	
	private String restUpdate(String url, Contact contact) {
		try {
			HttpHeaders header = createHttpHeaders();
			header.add("content-type", "application/json");
			HttpEntity<Contact> requestEntity = new  HttpEntity<Contact>(contact, header);
			@SuppressWarnings("rawtypes")
			ResponseEntity<Map> response = restTemplate.exchange(url,
					  HttpMethod.PUT, requestEntity, Map.class);
			@SuppressWarnings("unchecked")
			Map<String, String> map = response.getBody();
			return (map.get("location"));
		} catch (HttpClientErrorException e) {
			log.error(e.getResponseBodyAsString());
			throw e;
		}	
	}

	@Before
	public void deleteAll() {	
		for (Contact c : getAll()) {
			delete("http://localhost:8080/springServiceContacts/Contacts/" + c.getId());
		}		
	}
	
	private HttpHeaders createHttpHeaders() {
		HttpHeaders header = new HttpHeaders();
		header.add("content-type", "application/json");
		String authStr = user + ":" + password;
		String authEncoded = new String( Base64.encode(authStr.getBytes()));
		header.add("Authorization", "Basic " + authEncoded);
		return header;
	}
	
	private String restCreate(String firstName) {
		try {
			String postUrl = "http://localhost:8080/springServiceContacts/Contacts";		
			HttpHeaders header = createHttpHeaders();
			header.add("content-type", "application/json");
			Contact contact = createContact(firstName);
			HttpEntity<Contact> requestEntity = new  HttpEntity<Contact>(contact, header);
			@SuppressWarnings("rawtypes")
			ResponseEntity<Map> response = restTemplate.exchange(postUrl,
					  HttpMethod.POST, requestEntity, Map.class);
			@SuppressWarnings("unchecked")
			Map<String, String> map = response.getBody();
			return (map.get("location"));
		} catch (HttpClientErrorException e) {
			log.error(e.getResponseBodyAsString());
			throw e;
		}				
		
	}
	
	
	private Contact createContact(String firstName) {
		Contact c = new Contact();
		c.setFirstName(firstName);
		c.setLastName("Smith");
		c.setEmail("jsmith@yahoo.com");
		c.setPhone("123-456-7890");
		return c;
	}
}
