package com.silverboxsoft.dynamodbtool.dao;

import java.net.URISyntaxException;

import com.silverboxsoft.dynamodbtool.classes.DynamoDbConnectInfo;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;

/**
 * https://github.com/awsdocs/aws-doc-sdk-examples/blob/master/javav2/example_code/dynamodb/src/main/java/com/example/dynamodb/DescribeTable.java
 * 
 * @author tanakaeiji
 *
 */
public class TableInfoDao extends AbsDao {

	public TableInfoDao(DynamoDbConnectInfo connInfo) {
		super(connInfo);
	}

	public TableDescription getTableDescription(String tableName) throws URISyntaxException {
		DescribeTableRequest request = DescribeTableRequest.builder()
				.tableName(tableName)
				.build();
		DynamoDbClient ddb = getDbClient();
		try {
			return ddb.describeTable(request).table();
		} finally {
			ddb.close();
		}
	}
}
