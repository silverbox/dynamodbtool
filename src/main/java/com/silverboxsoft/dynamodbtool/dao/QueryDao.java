package com.silverboxsoft.dynamodbtool.dao;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

import com.silverboxsoft.dynamodbtool.classes.DynamoDbCondition;
import com.silverboxsoft.dynamodbtool.classes.DynamoDbConditionJoinType;
import com.silverboxsoft.dynamodbtool.classes.DynamoDbConnectInfo;
import com.silverboxsoft.dynamodbtool.classes.DynamoDbResult;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;

/*
 * https://github.com/awsdocs/aws-doc-sdk-examples/blob/master/javav2/example_code/dynamodb/src/main/java/com/example/dynamodb/Query.java
 */
public class QueryDao extends AbsDao {

	public QueryDao(DynamoDbConnectInfo connInfo) {
		super(connInfo);
	}

	public DynamoDbResult getResult(TableDescription tableInfo, DynamoDbConditionJoinType conditionJoinType,
			List<DynamoDbCondition> conditionList) throws URISyntaxException {
		String tableName = tableInfo.tableName();
		DynamoDbClient ddb = getDbClient();
		try {
			HashMap<String, String> attrNameAlias = new HashMap<String, String>();
			HashMap<String, AttributeValue> attrValues = new HashMap<String, AttributeValue>();
			StringBuilder conditionExpression = new StringBuilder();

			for (DynamoDbCondition dbCond : conditionList) {
				attrNameAlias.put(dbCond.getAlias(), dbCond.getColumnName());
				attrValues.put(":" + dbCond.getColumnName(), AttributeValue.builder().s(dbCond.getValue()).build());

				if (conditionExpression.length() > 0) {
					conditionExpression.append(conditionJoinType.getJoinStr());
				}
				conditionExpression.append(dbCond.getConditionExpression());
			}
			QueryRequest queryReq = QueryRequest.builder()
					.tableName(tableName)
					.keyConditionExpression(conditionExpression.toString())
					.expressionAttributeNames(attrNameAlias)
					.expressionAttributeValues(attrValues)
					.build();

			QueryResponse response = ddb.query(queryReq);
			return new DynamoDbResult(response.items(), tableInfo);
		} finally {
			ddb.close();
		}
	}
}
