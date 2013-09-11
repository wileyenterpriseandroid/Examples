package com.enterpriseandroid.googleappengineContacts.contact.util;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class EntityManagerFactoryHelper {
	private static final EntityManagerFactory emfInstance = Persistence
			.createEntityManagerFactory("transactions-optional");

	private EntityManagerFactoryHelper() {
	}

	public static EntityManagerFactory get() {
		return emfInstance;
	}
}
