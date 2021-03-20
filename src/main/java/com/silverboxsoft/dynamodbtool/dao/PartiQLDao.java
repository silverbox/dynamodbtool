package com.silverboxsoft.dynamodbtool.dao;

import java.net.URISyntaxException;

import com.silverboxsoft.dynamodbtool.classes.DynamoDbConnectInfo;
import com.silverboxsoft.dynamodbtool.classes.DynamoDbResult;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ExecuteStatementRequest;
import software.amazon.awssdk.services.dynamodb.model.ExecuteStatementResponse;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;

/*
 * https://github.com/awsdocs/aws-doc-sdk-examples/blob/master/javav2/example_code/dynamodb/src/main/java/com/example/dynamodb/Query.java
 */
public class PartiQLDao extends AbsDao {

	public PartiQLDao(DynamoDbConnectInfo connInfo) {
		super(connInfo);
	}

	public DynamoDbResult getResult(TableDescription tableInfo, String partiQL) throws URISyntaxException {
		DynamoDbClient ddb = getDbClient();
		try {
			ExecuteStatementRequest executeStatementRequest = //
					ExecuteStatementRequest.builder().statement(partiQL).build();
			ExecuteStatementResponse executeStatementResponse = ddb.executeStatement(executeStatementRequest);

			return new DynamoDbResult(executeStatementResponse.items(), tableInfo);
		} finally {
			ddb.close();
		}
	}
}
