package com.enterpriseandroidbook.contactscontractexample;

import java.util.HashMap;
import java.util.Map;

import android.net.Uri;
import android.provider.ContactsContract;

public class ListColumnMap {

	private static Map<String, String> map = null;

	private static void initMap() {
		if (null == map) {
			map = new HashMap<String, String>();
			map.put(ContactsContract.Data.CONTENT_URI.toString(),
					ContactsContract.Data.DISPLAY_NAME);
			map.put(ContactsContract.RawContacts.CONTENT_URI.toString(),
					ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY);
			map.put(ContactsContract.Contacts.CONTENT_URI.toString(),
					ContactsContract.Contacts.DISPLAY_NAME_PRIMARY);
			map.put(ContactsContract.Groups.CONTENT_URI.toString(),
					ContactsContract.Groups.ACCOUNT_NAME);
			map.put(ContactsContract.StatusUpdates.CONTENT_URI.toString(),
					ContactsContract.StatusUpdates.IM_HANDLE);
			map.put(ContactsContract.AggregationExceptions.CONTENT_URI
					.toString(), ContactsContract.AggregationExceptions._ID);
			map.put(ContactsContract.Settings.CONTENT_URI.toString(),
					ContactsContract.Settings.ACCOUNT_NAME);
			map.put(ContactsContract.SyncState.CONTENT_URI.toString(),
					ContactsContract.SyncState.ACCOUNT_NAME);
			map.put(ContactsContract.PhoneLookup.CONTENT_FILTER_URI.toString(),
					ContactsContract.PhoneLookup.DISPLAY_NAME);
		}
	}

	public static String get(Uri uri) {
		initMap();
		String key = uri.toString();
		return map.get(key);
	}

}
