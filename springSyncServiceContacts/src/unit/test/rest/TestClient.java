package unit.test.rest;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.enterpriseandroid.springServiceContacts.dataModel.Contact;
import com.enterpriseandroid.springServiceContacts.dataModel.SyncRequest;
import com.enterpriseandroid.springServiceContacts.dataModel.SyncResult;


public class TestClient {
	private final static org.apache.log4j.Logger log = Logger.getLogger(TestClient.class);
	//static private final String serverUrl = "http://contactapp123.appspot.com/Contacts/";
	//static private final String serverUrl = "http://localhost:8080/awsServiceContacts/Contacts/";
	//static private final String serverUrl = "http://localhost:8888/Contacts/";
	static private final String serverUrl = "http://localhost:8080/springSyncServiceContacts/Contacts/";
	
	private String user = "exampleUser";
	private String password = "examplePassword";

	private RestTemplate restTemplate;
		
	public TestClient(RestTemplate restTemplate) {
		super();
		this.restTemplate = restTemplate;
	}
	
	
	public String restCreateOrUpdate(String id, String firstName) {
		Contact contact = createContact(id, firstName);
		return restCreateOrUpdate(contact);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String restCreateOrUpdate(Contact contact) {
		try {
			HttpHeaders header = createHttpHeaders();
			header.add("content-type", "application/json");
			String postUrl = serverUrl;		
			HttpEntity<Contact> requestEntity = new  HttpEntity<Contact>(contact, header);
			ResponseEntity<Map> response = restTemplate.exchange(postUrl,
					HttpMethod.POST, requestEntity, Map.class);
			Map<String, String> map = response.getBody();
			return (map.get("location"));
		} catch (RestClientException e) {
			e.printStackTrace();
			throw e;
		}	
		
	}
	
	public void restDeleteById(String id) {	
		restDelete(serverUrl + id);
	}
	public void restDelete(String deleteUrl) {	
		HttpHeaders header = new HttpHeaders();
		header.add("content-type", "application/json");	
		HttpEntity<Contact> requestEntity = new  HttpEntity<Contact>(header);
		ResponseEntity<String> response = restTemplate.exchange(deleteUrl,
				  HttpMethod.DELETE, requestEntity, String.class);
		log.info(response.getStatusCode());	
		
	}
	
	public Contact restGet(String url) {	
		System.out.println(" url: " + url);
		HttpHeaders header = createHttpHeaders();
		HttpEntity<Contact> requestEntity = new  HttpEntity<Contact>(header);
		ResponseEntity<Contact> response = restTemplate.exchange(url,
				  HttpMethod.GET, requestEntity, Contact.class);
		return response.getBody();		
	}

	public Contact restGetById(String id) {
		return restGet(serverUrl + id);
	}
	
	public Contact createContact(String id, String firstName) {
		Contact c = new Contact();
		c.setId(id);
		c.setFirstName(firstName);
		c.setLastName("Smith");
		c.setEmail("jsmith@yahoo.com");
		c.setPhone("123-456-7890");
		return c;
	}
	
	public Contact[] findByFirstName(String firstName, int start, int numOfmatchers) {
		try {
			String findByFirstNameUrl = serverUrl + "?firstName={firstName}&start={start}&num={num}";
			HttpHeaders header = createHttpHeaders();
			HttpEntity<Contact> requestEntity = new  HttpEntity<Contact>(header);
			ResponseEntity<Contact[]> response = restTemplate.exchange(findByFirstNameUrl,
					  HttpMethod.GET, requestEntity, Contact[].class, firstName, start, numOfmatchers);
			return response.getBody();
		} catch (HttpClientErrorException e) {
			log.info(e.getResponseBodyAsString());
			throw e;
		}				
	}
	
	public SyncResult sync(List<Contact> contactList, long syncTime) {
		String syncUrl = serverUrl + "sync";
		HttpHeaders header = createHttpHeaders();		
		SyncRequest resquest = new SyncRequest(contactList, syncTime);
		HttpEntity<SyncRequest> requestEntity = new  HttpEntity<SyncRequest>(resquest, header);	
		ResponseEntity<SyncResult> response = restTemplate.exchange(syncUrl,
				  HttpMethod.POST, requestEntity, SyncResult.class);
		return response.getBody();
	}

	public Contact[] getAll() {
		try {
			HttpHeaders header = createHttpHeaders();
			HttpEntity<Contact> requestEntity = new  HttpEntity<Contact>(header);
			ResponseEntity<Contact[]> response = restTemplate.exchange(serverUrl,
					  HttpMethod.GET, requestEntity, Contact[].class);
			return response.getBody();
		} catch (HttpClientErrorException e) {
			log.info(e.getResponseBodyAsString());
			throw e;
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
}
