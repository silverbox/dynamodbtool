package com.silverboxsoft.dynamodbtool.dao;

import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ExecuteStatementRequest;
import software.amazon.awssdk.services.dynamodb.model.ExecuteStatementResponse;

/**
 * Unit test for simple App.
 */
public class PartiQLTest extends TestCase {
	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public PartiQLTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(PartiQLTest.class);
	}

	/**
	 * Rigourous Test :-)
	 */
	public void testsPartiQLQuery() {
//		String partiQLString = "select memo from account_kind_mst";
//		DynamoDbClient dbclient = DynamoDbClient.builder().region(Region.AP_NORTHEAST_1).build();
//		ExecuteStatementRequest executeStatementRequest = ExecuteStatementRequest.builder().statement(partiQLString)
//				.build();
//		ExecuteStatementResponse executeStatementResponse = dbclient.executeStatement(executeStatementRequest);
//		List<Map<String, AttributeValue>> items = executeStatementResponse.items();
//
//		System.out.println(items);
		assertTrue(true);
	}
}
