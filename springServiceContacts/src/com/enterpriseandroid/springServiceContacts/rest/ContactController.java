package com.enterpriseandroid.springServiceContacts.rest;

import com.enterpriseandroid.springServiceContacts.dataModel.Contact;
import com.enterpriseandroid.springServiceContacts.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/Contacts")
public class ContactController {
	@Autowired
	@Qualifier(value = "contactService")
	private ContactService service;

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	@ResponseBody
	public Contact getContact(@PathVariable Long id, HttpServletResponse resp)
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
	public Map<String, String> updateObject(@PathVariable Long id,
			@RequestBody Contact contact, HttpServletRequest req)
			throws IOException
    {
		contact.setId(id);
		service.storeOrUpdateContact(contact);
		Map<String, String> map = new HashMap<String, String>(1);
		map.put("location", req.getRequestURL().toString());
		return map;
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public String delete(@PathVariable Long id) throws IOException {
		service.deleteContact(id);
		return HttpStatus.OK.toString();
	}

	@ExceptionHandler(DuplicateKeyException.class)
	@ResponseBody
	public String handleDuplicationKeyException(Throwable exception,
			HttpServletResponse response) throws IOException {
		response.setStatus(HttpStatus.BAD_REQUEST.value());
		return HttpStatus.BAD_REQUEST.toString();
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
