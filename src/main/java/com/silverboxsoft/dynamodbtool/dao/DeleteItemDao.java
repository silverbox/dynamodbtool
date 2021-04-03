package com.silverboxsoft.dynamodbtool.dao;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import com.silverboxsoft.dynamodbtool.classes.DynamoDbConnectInfo;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;

/**
 * https://github.com/awsdocs/aws-doc-sdk-examples/blob/master/javav2/example_code/dynamodb/src/main/java/com/example/dynamodb/DeleteItem.java
 * 
 * @author tanakaeiji
 *
 */
public class DeleteItemDao extends AbsDao {

	public DeleteItemDao(DynamoDbConnectInfo connInfo) {
		super(connInfo);
	}

	public DeleteItemResponse deleteItem(TableDescription tableInfo, Map<String, AttributeValue> dynamoDbRec)
			throws URISyntaxException {
		String tableName = tableInfo.tableName();
		DynamoDbClient ddb = getDbClient();
		try {
			HashMap<String, AttributeValue> keyToGet = new HashMap<String, AttributeValue>();
			for (KeySchemaElement keyElem : tableInfo.keySchema()) {
				String keyAttr = keyElem.attributeName();
				keyToGet.put(keyAttr, dynamoDbRec.get(keyAttr));
			}
			DeleteItemRequest deleteReq = DeleteItemRequest.builder().tableName(tableName).key(keyToGet).build();
			return ddb.deleteItem(deleteReq);
		} finally {
			ddb.close();
		}
	}
}
