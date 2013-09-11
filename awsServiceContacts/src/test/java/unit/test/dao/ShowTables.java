package unit.test.dao;

import java.io.IOException;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.ListTablesRequest;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.enterpriseandroid.awsContacts.dao.impl.ContactDaoDynamoDBImpl;

public class ShowTables {


	public static void main(String[] args) throws IOException {
	    AWSCredentials credentials = new PropertiesCredentials(
                ContactDaoDynamoDBImpl.class
                        .getResourceAsStream("AwsCredentials.properties"));

        ClientConfiguration config = new ClientConfiguration();
        AmazonDynamoDBClient client =  new AmazonDynamoDBClient(credentials, config);
        
		// Initial value for the first page of table names.
		String lastEvaluatedTableName = null;
		do {
		    
		    ListTablesRequest listTablesRequest = new ListTablesRequest()
		    .withLimit(10)
		    .withExclusiveStartTableName(lastEvaluatedTableName);
		    
		    ListTablesResult result = client.listTables(listTablesRequest);
		    lastEvaluatedTableName = result.getLastEvaluatedTableName();
		    
		    for (String name : result.getTableNames()) {
		        System.out.println(name);
		    }
		    
		} while (lastEvaluatedTableName != null);
	}

}
