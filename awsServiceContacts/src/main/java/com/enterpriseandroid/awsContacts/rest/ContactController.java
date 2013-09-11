package com.enterpriseandroid.awsContacts.rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.enterpriseandroid.awsContacts.dataModel.Contact;
import com.enterpriseandroid.awsContacts.dataModel.SyncRequest;
import com.enterpriseandroid.awsContacts.dataModel.SyncResult;
import com.enterpriseandroid.awsContacts.service.ContactService;


@Controller
@RequestMapping("/Contacts")
public class ContactController {
	
	/***
	 * The userId should be passed in as part of the authentication
	 * For an example, If you use oath or http basic authentication,
	 * the userId is passed in the Authorization header.
	 * 
	 * Since we are not using any authentication in this example, we hard code it
	 * for now. 
	 */
	private String userId ="exampleUser";
	@Autowired
	@Qualifier(value = "contactService")
	private ContactService service;

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	@ResponseBody
	public Contact getContact(@PathVariable String id, HttpServletResponse resp)
			throws IOException
    {
		Contact c = service.getContact(userId, id);
		if (c == null) {
			resp.setStatus(HttpStatus.NOT_FOUND.value());
		}
		return c;
	}

	@RequestMapping(value = "", method = RequestMethod.GET)
	@ResponseBody
	public List<Contact> findContacts(
			@RequestParam(value = "firstName", required = false) String firstName,
			@RequestParam(value = "start", required = false) String startStr,
			@RequestParam(value = "num", required = false) String numStr)
			throws IOException
    {
		int start = 0;
		int num = 10;
		if (startStr != null) {
			start = Integer.parseInt(startStr);
		}

		if (numStr != null) {
			num = Integer.parseInt(numStr);
		}

		if (firstName != null) {
			return service.findContactByFirestName(userId, firstName, start, num);
		} else {
			return service.getAll(userId, start, num);
		}
	}

	@RequestMapping(value = "", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, String> createObject(@RequestBody Contact contact,
			HttpServletRequest req) throws IOException
    {
		return createOrUpdate(contact, req);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public Map<String, String> updateObject(@PathVariable String id,
			@RequestBody Contact contact, HttpServletRequest req)
			throws IOException
    {
		service.storeOrUpdateContact(userId, contact);
		Map<String, String> map = new HashMap<String, String>(1);
		map.put("location", req.getRequestURL().toString());
		return map;
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public String delete(@PathVariable String id) throws IOException {
		service.deleteContact(userId, id);
		return HttpStatus.OK.toString();
	}



	@RequestMapping(value = "/sync", method = RequestMethod.POST)
	@ResponseBody
	public SyncResult sync(@RequestBody SyncRequest syncR) throws IOException {
		try {
			SyncResult result = service.sync(userId, syncR);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException();
		}
	}

	private Map<String, String> createOrUpdate(Contact contact,
			HttpServletRequest req) throws IOException
    {
		service.storeOrUpdateContact(userId, contact);
		Map<String, String> map = new HashMap<String, String>(1);
		map.put("location",
				req.getRequestURL().toString() + "/" + contact.getId());
		return map;
	}
}
