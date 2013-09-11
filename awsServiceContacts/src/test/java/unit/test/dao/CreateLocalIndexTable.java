package unit.test.dao;

import java.io.IOException;
import java.util.ArrayList;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteTableResult;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.LocalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.model.TableStatus;
import com.enterpriseandroid.awsContacts.dao.impl.ContactDaoDynamoDBImpl;


public class CreateLocalIndexTable {
	private static AmazonDynamoDBClient client;

	public static void main(String[] args) throws IOException {
	    AWSCredentials credentials = new PropertiesCredentials(
                ContactDaoDynamoDBImpl.class
                        .getResourceAsStream("AwsCredentials.properties"));

        ClientConfiguration config = new ClientConfiguration();
        client =  new AmazonDynamoDBClient(credentials, config);

        
		String tableName = "Contact_LSI";
		deleteTable(tableName);
		CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName);

		//ProvisionedThroughput
		createTableRequest.setProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits((long)3).withWriteCapacityUnits((long)3));

		//AttributeDefinitions
		ArrayList<AttributeDefinition> attributeDefinitions= new ArrayList<AttributeDefinition>();
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("userId").withAttributeType("S"));
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("id").withAttributeType("S"));
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("firstName").withAttributeType("S"));
//		attributeDefinitions.add(new AttributeDefinition().withAttributeName("lastName").withAttributeType("S"));
//		attributeDefinitions.add(new AttributeDefinition().withAttributeName("email").withAttributeType("S"));
//		attributeDefinitions.add(new AttributeDefinition().withAttributeName("phone").withAttributeType("S"));
//		attributeDefinitions.add(new AttributeDefinition().withAttributeName("version").withAttributeType("N"));
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("updateTime").withAttributeType("N"));

		createTableRequest.setAttributeDefinitions(attributeDefinitions);
		        
		//KeySchema
		ArrayList<KeySchemaElement> tableKeySchema = new ArrayList<KeySchemaElement>();
		tableKeySchema.add(new KeySchemaElement().withAttributeName("userId").withKeyType(KeyType.HASH));
		tableKeySchema.add(new KeySchemaElement().withAttributeName("id").withKeyType(KeyType.RANGE));

		createTableRequest.setKeySchema(tableKeySchema);
		
		
		LocalSecondaryIndex firstNameLocalIndex = createIndex("firstNameIndex", "firstName");
		LocalSecondaryIndex updateTimeLocalIndex = createIndex("updateTimeIndex", "updateTime");
		
	
		
		ArrayList<LocalSecondaryIndex> localSecondaryIndexes = new ArrayList<LocalSecondaryIndex>();
		localSecondaryIndexes.add(firstNameLocalIndex);
		localSecondaryIndexes.add(updateTimeLocalIndex);
		
		createTableRequest.setLocalSecondaryIndexes(localSecondaryIndexes);

		CreateTableResult result = client.createTable(createTableRequest);
		waitForTableToBecomeAvailable(tableName);
		System.out.println(result.getTableDescription());
	}
	
	static private LocalSecondaryIndex createIndex(String indexName, String attributeName) {
		ArrayList<KeySchemaElement> indexKeySchema = new ArrayList<KeySchemaElement>();
		indexKeySchema.add(new KeySchemaElement().withAttributeName("userId").withKeyType(KeyType.HASH));
		indexKeySchema.add(new KeySchemaElement().withAttributeName(attributeName).withKeyType(KeyType.RANGE));
		Projection projection = createProjection();
		LocalSecondaryIndex firstNamelocalSecondaryIndex = new LocalSecondaryIndex()
		    .withIndexName(indexName).withKeySchema(indexKeySchema).withProjection(projection);
		return firstNamelocalSecondaryIndex;
		
	}

	static private Projection createProjection() {
		Projection projection = new Projection().withProjectionType(ProjectionType.INCLUDE);
		ArrayList<String> nonKeyAttributes = new ArrayList<String>();
//		nonKeyAttributes.add("id");
//		nonKeyAttributes.add("firstName");
		nonKeyAttributes.add("lastName");
		nonKeyAttributes.add("email");
		nonKeyAttributes.add("phone");
		nonKeyAttributes.add("version");
//		nonKeyAttributes.add("updateTime");
		projection.setNonKeyAttributes(nonKeyAttributes);
		return projection;
	}
	static public void  deleteTable( String tableName) {
		DeleteTableRequest deleteTableRequest = new DeleteTableRequest()
		  .withTableName(tableName);
		try {
			DeleteTableResult result = client.deleteTable(deleteTableRequest);
			TableDescription  tableDescription  =result.getTableDescription();
			waitForTableToBeDeleted(tableName);
		} catch (ResourceNotFoundException e) {
			
		}
	}
	
	private static void waitForTableToBeDeleted(String tableName) {
	        System.out.println("Waiting for " + tableName + " while status DELETING...");

	        long startTime = System.currentTimeMillis();
	        long endTime = startTime + (10 * 60 * 1000);
	        while (System.currentTimeMillis() < endTime) {
	            try {
	                DescribeTableRequest request = new DescribeTableRequest().withTableName(tableName);
	                TableDescription tableDescription = client.describeTable(request).getTable();
	                String tableStatus = tableDescription.getTableStatus();
	                System.out.println("  - current state: " + tableStatus);
	                if (tableStatus.equals(TableStatus.ACTIVE.toString())) return;
	            } catch (ResourceNotFoundException e) {
	                System.out.println("Table " + tableName + " is not found. It was deleted.");
	                return;
	            }
	            try {Thread.sleep(1000 * 20);} catch (Exception e) {}
	        }
	        throw new RuntimeException("Table " + tableName + " was never deleted");
	    }

	
	private static void waitForTableToBecomeAvailable(String tableName) {
        System.out.println("Waiting for " + tableName + " to become ACTIVE...");

        long startTime = System.currentTimeMillis();
        long endTime = startTime + (10 * 60 * 1000);
        while (System.currentTimeMillis() < endTime) {
            DescribeTableRequest request = new DescribeTableRequest()
                    .withTableName(tableName);
            TableDescription tableDescription = client.describeTable(
                    request).getTable();
            String tableStatus = tableDescription.getTableStatus();
            System.out.println("  - current state: " + tableStatus);
            if (tableStatus.equals(TableStatus.ACTIVE.toString()))
                return;
            try { Thread.sleep(1000 * 20); } catch (Exception e) { }
        }
        throw new RuntimeException("Table " + tableName + " never went active");
    }
}
