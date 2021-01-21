package com.silverboxsoft.dynamodbtool.dao;

import java.net.URISyntaxException;

import com.silverboxsoft.dynamodbtool.classes.DynamoDbConnectInfo;
import com.silverboxsoft.dynamodbtool.classes.DynamoDbResult;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

/**
 * https://github.com/awsdocs/aws-doc-sdk-examples/blob/master/javav2/example_code/dynamodb/src/main/java/com/example/dynamodb/DynamoDBScanItems.java
 * 
 * @author tanakaeiji
 *
 */
public class ScanDao extends AbsDao {

	public ScanDao(DynamoDbConnectInfo connInfo) {
		super(connInfo);
	}

	public DynamoDbResult getResult(String tableName) throws URISyntaxException {
		DynamoDbClient ddb = getDbClient();
		try {
			ScanRequest scanRequest = ScanRequest.builder()
					.tableName(tableName)
					.build();

			return new DynamoDbResult(ddb.scan(scanRequest));
			// for (Map<String, AttributeValue> item : response.items()) {
			// Set<String> keys = item.keySet();
			// for (String key : keys) {
			//
			// System.out.println("The key name is " + key + "\n");
			// System.out.println("The value is " + item.get(key).s());
			// }
			// }
		} finally {
			ddb.close();
		}
	}
}
