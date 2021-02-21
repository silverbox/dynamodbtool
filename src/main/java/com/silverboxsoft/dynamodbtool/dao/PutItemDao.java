package com.silverboxsoft.dynamodbtool.dao;

import java.net.URISyntaxException;
import java.util.Map;

import com.silverboxsoft.dynamodbtool.classes.DynamoDbConnectInfo;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;

/**
 * https://github.com/awsdocs/aws-doc-sdk-examples/blob/master/javav2/example_code/dynamodb/src/main/java/com/example/dynamodb/PutItem.java
 * 
 * @author tanakaeiji
 *
 */
public class PutItemDao extends AbsDao {

	public PutItemDao(DynamoDbConnectInfo connInfo) {
		super(connInfo);
	}

	public PutItemResponse putItem(TableDescription tableInfo, Map<String, AttributeValue> dynamoDbRec)
			throws URISyntaxException {
		String tableName = tableInfo.tableName();
		DynamoDbClient ddb = getDbClient();
		try {
			PutItemRequest request = PutItemRequest.builder().tableName(tableName).item(dynamoDbRec).build();
			return ddb.putItem(request);
		} finally {
			ddb.close();
		}
	}
}
