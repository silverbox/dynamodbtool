package com.silverboxsoft.dynamodbtool.dao;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ComparisonOperator;
import software.amazon.awssdk.services.dynamodb.model.Condition;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

/**
 * Unit test for simple App.
 */
public class QueryDaoTest extends TestCase {
	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public QueryDaoTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(QueryDaoTest.class);
	}

	/**
	 * Rigourous Test :-)
	 */
	public void testsGSIQuery() {
//		DynamoDbClient dbclient = DynamoDbClient.builder().region(Region.AP_NORTHEAST_1).build();
//
//		Map<String, Condition> keyConditions = new HashMap<String, Condition>();
//		keyConditions.put(
//				"col-string1",
//				Condition.builder()
//						.attributeValueList(AttributeValue.builder().s("string-attr").build())
//						.comparisonOperator(ComparisonOperator.EQ).build());
//		QueryRequest queryReq = QueryRequest.builder()
//				.tableName("attrbute_test")
//				.indexName("test-gsi")
//				.keyConditions(keyConditions).build();
//
//		QueryResponse response = dbclient.query(queryReq);
//		System.out.println(response);
		assertTrue(true);
	}
}
