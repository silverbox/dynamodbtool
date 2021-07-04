package com.silverboxsoft.dynamodbtool.dao

import junit.framework.TestCase
import junit.framework.Assert
import junit.framework.Test
import junit.framework.TestSuite
import com.silverboxsoft.dynamodbtool.dao.PartiQLTest
import com.silverboxsoft.dynamodbtool.dao.QueryDaoTest

/**
 * Unit test for simple App.
 */
class QueryDaoTest
/**
 * Create the test case
 *
 * @param testName
 * name of the test case
 */
    (testName: String?) : TestCase(testName) {
    /**
     * Rigourous Test :-)
     */
    fun testsGSIQuery() {
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
        assertTrue(true)
    }

    companion object {
        /**
         * @return the suite of tests being tested
         */
        fun suite(): Test {
            return TestSuite(QueryDaoTest::class.java)
        }
    }
}