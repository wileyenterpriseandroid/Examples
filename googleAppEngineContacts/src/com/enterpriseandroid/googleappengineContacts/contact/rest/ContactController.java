package com.enterpriseandroid.googleappengineContacts.contact.rest;

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
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.enterpriseandroid.googleappengineContacts.dao.VersionNotMatchException;
import com.enterpriseandroid.googleappengineContacts.dataModel.Contact;
import com.enterpriseandroid.googleappengineContacts.dataModel.SyncRequest;
import com.enterpriseandroid.googleappengineContacts.dataModel.SyncResult;
import com.enterpriseandroid.googleappengineContacts.service.ContactService;


@Controller
@RequestMapping("/Contacts")
public class ContactController {
	@Autowired
	@Qualifier(value = "contactService")
	private ContactService service;

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	@ResponseBody
	public Contact getContact(@PathVariable String id, HttpServletResponse resp)
			throws IOException
    {
		Contact c = service.getContact(id);
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
			return service.findContactByFirstName(firstName, start, num);
		} else {           
			return service.getAll(start, num);
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
		service.storeOrUpdateContact(contact);
		Map<String, String> map = new HashMap<String, String>(1);
		map.put("location", req.getRequestURL().toString());
		return map;
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public String delete(@PathVariable String id) throws IOException {
		service.deleteContact(id);
		return HttpStatus.OK.toString();
	}


	@ExceptionHandler(VersionNotMatchException.class)
	@ResponseBody
	public String handleDuplicationKeyException(Throwable exception,
			HttpServletResponse response) throws IOException {
		response.setStatus(HttpStatus.BAD_REQUEST.value());
		return HttpStatus.BAD_REQUEST.toString();
	}

	@RequestMapping(value = "/sync", method = RequestMethod.POST)
	@ResponseBody
	public SyncResult sync(@RequestBody SyncRequest syncR) throws Exception {
		try {
			SyncResult result = service.sync(syncR);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private Map<String, String> createOrUpdate(Contact contact,
			HttpServletRequest req) throws IOException
    {
		service.storeOrUpdateContact(contact);
		Map<String, String> map = new HashMap<String, String>(1);
		map.put("location",
				req.getRequestURL().toString() + "/" + contact.getId());
		return map;
	}
}
