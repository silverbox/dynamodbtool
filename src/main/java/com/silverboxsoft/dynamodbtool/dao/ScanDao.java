package com.silverboxsoft.dynamodbtool.dao;

import java.net.URISyntaxException;

import com.silverboxsoft.dynamodbtool.classes.DynamoDbConnectInfo;
import com.silverboxsoft.dynamodbtool.classes.DynamoDbResult;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;

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

	public DynamoDbResult getResult(TableDescription tableInfo) throws URISyntaxException {
		String tableName = tableInfo.tableName();
		DynamoDbClient ddb = getDbClient();
		try {
			ScanRequest scanRequest = ScanRequest.builder().tableName(tableName).build();

			return new DynamoDbResult(ddb.scan(scanRequest).items(), tableInfo);
		} finally {
			ddb.close();
		}
	}
}
