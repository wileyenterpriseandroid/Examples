package com.enterpriseandroid.awsContacts.dao.impl;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.*;
import com.enterpriseandroid.awsContacts.dao.ContactDao;
import com.enterpriseandroid.awsContacts.dao.VersionNotMatchException;
import com.enterpriseandroid.awsContacts.dataModel.Contact;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * Enterprise Android contacts RESTful service implementation that uses the
 * Amazon Dynamo DB API for scalable, hosted persistence.
 */
public class ContactDaoDynamoDBV2Impl implements ContactDao {
    private final String USER_ID = "userId";
    private final String  ID = "id";
    private final String FIRST_NAME = "firstName";
    private final String LAST_NAME = "lastName";
    private final String EMAIL = "email";
    private final String UPDATE_TIME = "updateTime";
    private final String VERSION = "version";
    private final String DELETED = "deleted";

    private final String FIRST_NAME_INDEX = "firstNameIndex";
    private final String UPDATE_TIME_INDEX = "updateTimeIndex";
    private final String CONTACT_TABLE = "Contact_LSI";

    private AmazonDynamoDBClient client;

    public ContactDaoDynamoDBV2Impl() throws IOException {
        AWSCredentials credentials = new PropertiesCredentials(
                ContactDaoDynamoDBImpl.class
                        .getResourceAsStream("AwsCredentials.properties"));

        ClientConfiguration config = new ClientConfiguration();
        client =  new AmazonDynamoDBClient(credentials, config);
    }

    @Override
    public Contact getContact(String userId, String id) throws IOException {
     	GetItemRequest getItemRequest = new GetItemRequest()
    	    .withTableName(CONTACT_TABLE)
    	    .withKey(createKey(userId, id))
    	    .withConsistentRead(true);
    	
    	awsQuotaDelay();
    	GetItemResult result = client.getItem(getItemRequest);
        Map<String, AttributeValue> item = result.getItem();
        if (item == null) {
            return null;
        }
        return itemToContact(item);
    }

    @Override
    public String storeOrUpdateContact(String userId, Contact contact)
            throws IOException
    {
       	contact.setUpdateTime(System.currentTimeMillis());
    	Contact oldContact = getContact(userId, contact.getId());
    	if ( oldContact != null) {
    		return updateContact(userId, oldContact, contact); 
    	}
    	
    	/*** create a new contact ***/
    	contact.setVersion(1);
     	Map<String, AttributeValue> item = contactToItem(contact);
//     	item.put(USER_ID, new AttributeValue().
//                withS(composeKeys(userId, contact.getId())));

    	item.put(USER_ID, new AttributeValue().withS(userId));

      putItem(CONTACT_TABLE, item, null);
       return contact.getId();    	
    	

    }
    private String updateContact(String userId, Contact oldContact, Contact newContact) throws IOException {
		Map<String, ExpectedAttributeValue> expectedValues =
		new HashMap<String, ExpectedAttributeValue>();
		expectedValues.put(VERSION, new ExpectedAttributeValue().withValue(new AttributeValue()
                      .withN(Long.toString(newContact.getVersion()))));
		 Map<String, AttributeValue> item = contactToItem(newContact);
		 item.put(VERSION, new AttributeValue().withN(Long.toString(newContact.getVersion() + 1)));
		 item.put(USER_ID, new AttributeValue().withS(userId)); 
		 
		 putItem(CONTACT_TABLE, item, expectedValues);
		 return newContact.getId();
    }
    
    
    private void putItem(String indexName, Map<String, AttributeValue> item,
                         Map<String, ExpectedAttributeValue> expectedValues) throws VersionNotMatchException
    {
        PutItemRequest putItemRequest = new PutItemRequest()
                .withTableName(indexName)
                .withItem(item);
        if (expectedValues != null) {
            putItemRequest.withExpected(expectedValues);
        }
        awsQuotaDelay();
        try {
        	client.putItem(putItemRequest);
        } catch (ConditionalCheckFailedException e) {
        	throw new VersionNotMatchException("version not match", e);
        }
    }


    @Override
    public List<Contact> findContactFirstName(String userId, String firstName,
                                              int start, int numOfmatches)
    {

        Condition rangeKeyCondition = new Condition().withComparisonOperator(
                ComparisonOperator.BEGINS_WITH.toString())
                .withAttributeValueList(              		
                new AttributeValue().withS(firstName));
        return query(userId, rangeKeyCondition, start, numOfmatches,
                FIRST_NAME, FIRST_NAME_INDEX);
    }

    @Override
    public List<Contact> findChanged(String userId, long timestamp, int start,
                                     int numOfmatches)
    {
        Condition rangeKeyCondition = new Condition().withComparisonOperator(
                ComparisonOperator.GE.toString()).withAttributeValueList(
                new AttributeValue().withN(Long.toString(timestamp)));

        return query(userId, rangeKeyCondition, start, numOfmatches,
                UPDATE_TIME, UPDATE_TIME_INDEX);
    }

    private List<Contact> query(String userId, Condition rangeKeyCondition,
                                int start,
                                int numOfmatches, String rangeKeyName, String indexName)
    {
		Map<String, AttributeValue> lastEvaluatedKey = null;
		List<Contact> ret = new ArrayList<Contact>();
		Condition hashKeyCondition = new Condition().withComparisonOperator(
				ComparisonOperator.EQ.toString()).withAttributeValueList(
				new AttributeValue().withS(userId));

		Map<String, Condition> keyConditions = new HashMap<String, Condition>();
		keyConditions.put(USER_ID, hashKeyCondition);
		keyConditions.put(rangeKeyName, rangeKeyCondition);

		QueryRequest queryRequest = new QueryRequest().withTableName(CONTACT_TABLE)
				.withConsistentRead(true).withSelect("ALL_ATTRIBUTES")
				.withScanIndexForward(true).withLimit(start + numOfmatches)
				.withExclusiveStartKey(lastEvaluatedKey);

		queryRequest.setIndexName(indexName);
		queryRequest.setKeyConditions(keyConditions);

		QueryResult result = client.query(queryRequest);

		int pos = 0;
		for (Map<String, AttributeValue> item : result.getItems()) {
			pos++;
			if (pos > start) {
				Contact contact = itemToContact(item);
				ret.add(contact);
			}
			if (ret.size() == numOfmatches) {
				break;
			}
		}

		return ret;
    }

    @Override
    public void delete(String userId, String id) throws IOException {
        Contact contact = getContact(userId, id);
        if ( contact == null) {
        	return;
        }
        DeleteItemRequest deleteItemRequest = new DeleteItemRequest()
        .withTableName(CONTACT_TABLE)
        .withKey(createKey(userId, id));
        client.deleteItem(deleteItemRequest);


    }


    @Override
    public List<Contact> getAll(String userId, int start, int numOfmatches)
            throws IOException
    {
        return findChanged(userId, 0, start, numOfmatches );
    }

    private Contact itemToContact(Map<String, AttributeValue>item ) {
        Contact contact = new Contact();       
        contact.setId(item.get(ID).getS());
        contact.setFirstName(item.get(FIRST_NAME).getS());
        contact.setLastName(item.get(LAST_NAME).getS());
        contact.setEmail(item.get(EMAIL).getS());
        contact.setUpdateTime(getLong(item.get(UPDATE_TIME)));
        contact.setVersion(getLong(item.get(VERSION)));
        contact.setDeleted(getBoolean(item.get(DELETED)));
       return contact;
    }

    private Map<String, AttributeValue> contactToItem(Contact contact) {
        String id = contact.getId();
        Map<String, AttributeValue> item =
                new HashMap<String, AttributeValue>();
        if ( id == null) {
            id = UUID.randomUUID().toString();
            contact.setId(id);
        }
        item.put(ID, new AttributeValue().withS(id));
        item.put(FIRST_NAME, new AttributeValue()
                .withS(contact.getFirstName()));
        item.put(LAST_NAME, new AttributeValue().withS(contact.getLastName()));
        item.put(EMAIL, new AttributeValue().withS(contact.getEmail()));
        item.put(UPDATE_TIME, new AttributeValue()
                .withN(Long.toString(contact.getUpdateTime())));
        item.put(VERSION, new AttributeValue()
                .withN(Long.valueOf(contact.getVersion()).toString()));
        item.put(DELETED, new AttributeValue()
        	.withS(contact.isDeleted() ? Boolean.TRUE.toString(): Boolean.FALSE.toString() ));
        return item;

    }
    
    private Map<String, AttributeValue> createKey(String hashKey, String rangeKey) {
      	Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
    	key.put(USER_ID, new AttributeValue().withS(hashKey));
    	key.put(ID, new AttributeValue().withS(rangeKey));
    	return key;
    }

    private Long getLong(AttributeValue attr) {
        return Long.parseLong(attr.getN());
    }

    private Boolean getBoolean(AttributeValue attr) {
        return Boolean.parseBoolean(attr.getS());
    }

  
    /**
     * The free AWS account only allows 5 read/write per second, so insert a
     * delay to stay under that quota.
     */
    private void awsQuotaDelay() {
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
