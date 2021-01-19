package com.silverboxsoft.dao;

import java.util.HashMap;
import java.util.List;

import com.silverboxsoft.classes.DynamoDbCondition;
import com.silverboxsoft.classes.DynamoDbConditionJoinType;
import com.silverboxsoft.classes.DynamoDbResult;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

public class DynamicDao {

	private DynamoDbClient ddb;

	public DynamicDao() {
		Region region = Region.AP_NORTHEAST_1;
		this.ddb = DynamoDbClient.builder().region(region).build();
	}

	public DynamoDbResult getResult(String tableName, DynamoDbConditionJoinType conditionJoinType,
			List<DynamoDbCondition> conditionList) {
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

		System.out.println(conditionExpression.toString());
		QueryRequest queryReq = QueryRequest.builder()
				.tableName(tableName)
				.keyConditionExpression(conditionExpression.toString())
				.expressionAttributeNames(attrNameAlias)
				.expressionAttributeValues(attrValues)
				.build();

		QueryResponse response = ddb.query(queryReq);
		return new DynamoDbResult(response);
	}
}
