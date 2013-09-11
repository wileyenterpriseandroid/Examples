package com.enterpriseandroid.awsContacts.dao.impl;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;

import com.amazonaws.services.dynamodb.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodb.model.*;
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
public class ContactDaoDynamoDBImpl implements ContactDao {
    private final String ID = "id";
    private final String FIRST_NAME = "firstName";
    private final String LAST_NAME = "lastName";
    private final String EMAIL = "email";
    private final String UPDATE_TIME = "updateTime";
    private final String VERSION = "version";
    private final String DELETED = "deleted";
    private final String HASH_KEY = "userId";

    private final String FIRST_NAME_INDEX_TABLE = "ContactFNameIndex";
    private final String UPDATE_TIME_INDEX = "ContactUpdateTimeIndex";
    private final String CONTACT_TABLE = "Contact";

    private AmazonDynamoDBClient client;

    public ContactDaoDynamoDBImpl() throws IOException {
        AWSCredentials credentials = new PropertiesCredentials(
                ContactDaoDynamoDBImpl.class
                        .getResourceAsStream("AwsCredentials.properties"));

        ClientConfiguration config = new ClientConfiguration();
        client =  new AmazonDynamoDBClient(credentials, config);
    }

    @Override
    public Contact getContact(String userId, String id) throws IOException {

        // Get a contact with the composed string key, userId:id to identify
        // the row for the contact.  We're not explicitly specifying the
        // columns to get, since we want all of the columns. But you could
        // specify columns using the code commented out above and below.
        GetItemRequest getItemRequest = new GetItemRequest()
                .withTableName(CONTACT_TABLE)
                .withKey(new Key().withHashKeyElement(new AttributeValue()
                        .withS(this.composeKeys(userId, id))))
                .withConsistentRead(true);

        awsQuotaDelay();

        GetItemResult result = client.getItem(getItemRequest);
        Map<String, AttributeValue> item = result.getItem();
        if (item == null) {
            return null;
        }
        return item2Contact(item);
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
    	item.put(ID, new AttributeValue().
              withS(composeKeys(userId, contact.getId())));

      /***
       * AWS does not provide transaction support, and cannot guarantee that
       * all the writes to DynamoDB are successful. To avoid this problem, we
       * recommend using AWS SQS service. The idea is to wrap both update
       * operations into a task, and then put the task into SQS. We remove the
       * the task from SQS if both operations successes.
       */
      putItem(CONTACT_TABLE, item, null);
      updateUpdateTimeIndex(userId, 0, contact);
      updateFnameIndex(userId, contact.getFirstName(),
              null, contact.getId());

      return contact.getId();    	
    	

    }
    private String updateContact(String userId, Contact oldContact, Contact newContact) throws IOException {
		Map<String, ExpectedAttributeValue> expectedValues =
		new HashMap<String, ExpectedAttributeValue>();
		expectedValues.put(VERSION, new ExpectedAttributeValue().withValue(new AttributeValue()
                      .withN(Long.toString(newContact.getVersion()))));
		 Map<String, AttributeValue> item = contactToItem(newContact);
		 item.put(VERSION, new AttributeValue().withN(Long.toString(newContact.getVersion() + 1)));
		 item.put(ID, new AttributeValue().withS(composeKeys(userId, newContact.getId())));
		 putItem(CONTACT_TABLE, item, expectedValues);
		 return newContact.getId();
    }
    
    
    private void updateUpdateTimeIndex(String userId, 
    		long oldUpdatTime, Contact contact) throws IOException {
        if (oldUpdatTime == contact.getUpdateTime() )
    {
            return;
        }

        Map<String, AttributeValue> item =
                new HashMap<String, AttributeValue>();
        item.put(HASH_KEY, new AttributeValue().withS(userId));
        item.put(UPDATE_TIME, new AttributeValue().
                withS(composeKeys(Long.toString(contact.getUpdateTime()),
                        contact.getId())));
        putItem(UPDATE_TIME_INDEX, item, null);
        if ( oldUpdatTime > 0  ) {
            // delete the old index
            deleteDo(userId,
                    composeKeys(Long.toString(oldUpdatTime), contact.getId()),
                    UPDATE_TIME_INDEX);
        }
    }

    private void updateFnameIndex(String hashKey,
    		String fname, String oldFirstName, String id) throws IOException
    {
        if (oldFirstName !=null && oldFirstName.equals(fname)) {
            return;
        }

        Map<String, AttributeValue> item =
                new HashMap<String, AttributeValue>();
        item.put(HASH_KEY, new AttributeValue().withS(hashKey));
        item.put(FIRST_NAME, new AttributeValue()
                .withS(composeKeys(fname, id)));
        putItem(FIRST_NAME_INDEX_TABLE, item, null);
        if( oldFirstName != null) {
            deleteDo(hashKey, composeKeys(oldFirstName, id),
                    FIRST_NAME_INDEX_TABLE);
        }
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
                FIRST_NAME, FIRST_NAME_INDEX_TABLE);
    }

    @Override
    public List<Contact> findChanged(String userId, long timestamp, int start,
                                     int numOfmatches)
    {
        Condition rangeKeyCondition = new Condition().withComparisonOperator(
                ComparisonOperator.GE.toString()).withAttributeValueList(
                new AttributeValue().withS(Long.toString(timestamp)));

        return query(userId, rangeKeyCondition, start, numOfmatches,
                UPDATE_TIME, this.UPDATE_TIME_INDEX);
    }

    private List<Contact> query(String userId, Condition rangeKeyCondition,
                                int start,
                                int numOfmatches, String rangeKeyName,
                                String table)
    {
        Key lastKeyEvaluated = null;
        List<Contact> ret = new ArrayList<Contact>();

        QueryRequest queryRequest = new QueryRequest()
                .withTableName(table)
                .withHashKeyValue(new AttributeValue().withS(userId))
                .withRangeKeyCondition(rangeKeyCondition)
                .withLimit(numOfmatches)
                .withExclusiveStartKey(lastKeyEvaluated)
                .withScanIndexForward(true);

        QueryResult result = client.query(queryRequest);

        int pos = 0;
        for (Map<String, AttributeValue> indexItem : result.getItems()) {
            pos++;
            if (pos > start ) {
                String[] ids = fromComposedKeys(indexItem
                        .get(rangeKeyName).getS());
                Contact contact;
                try {
                    contact = getContact(userId, ids[1]);
                    if (contact != null) {
                        ret.add(contact);
                    } else {
                        // delete the index if the data does not exists
                        deleteDo(userId, composeKeys(ids[0], ids[1]), table);
                    }

                    // awsQuotaDelay();
                } catch (Exception e) {
                    // if we cannot load the contact, we just continue
                }
                if (ret.size() == numOfmatches) {
                    break;
                }
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
        deleteDo(composeKeys(userId, id), null, CONTACT_TABLE);
        deleteDo(userId, composeKeys(contact.getFirstName(), id),
                FIRST_NAME_INDEX_TABLE);
        deleteDo(userId, composeKeys(
                Long.toString(contact.getUpdateTime()), id),
                UPDATE_TIME_INDEX);
    }

    private void deleteDo(String hashKey, String rangeKey, String table) {
        Key key = new Key()
                .withHashKeyElement(new AttributeValue().withS(hashKey));

        if ( rangeKey != null) {
            key = key.withRangeKeyElement(new AttributeValue().withS(rangeKey));
        }

        DeleteItemRequest deleteItemRequest = new DeleteItemRequest()
                .withTableName(table)
                .withKey(key);
        DeleteItemResult result = client.deleteItem(deleteItemRequest);
    }

    @Override
    public List<Contact> getAll(String userId, int start, int numOfmatches)
            throws IOException
    {
        return findChanged(userId, 0, start, numOfmatches );
    }

    private Contact item2Contact(Map<String, AttributeValue>item ) {
        Contact contact = new Contact();
        String ids[] = fromComposedKeys(item.get(ID).getS());
        contact.setId(ids[1]);
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
    private Long getLong(AttributeValue attr) {
        return Long.parseLong(attr.getN());
    }

    private Boolean getBoolean(AttributeValue attr) {
        return Boolean.parseBoolean(attr.getS());
    }

    private String composeKeys(String k1, String k2) {
        return k1 + ":" + k2;
    }

    private String[] fromComposedKeys(String k) {
        return k.split(":");
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
