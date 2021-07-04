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
class PartiQLTest
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
    fun testsPartiQLQuery() {
//		String partiQLString = "select memo from account_kind_mst";
//		DynamoDbClient dbclient = DynamoDbClient.builder().region(Region.AP_NORTHEAST_1).build();
//		ExecuteStatementRequest executeStatementRequest = ExecuteStatementRequest.builder().statement(partiQLString)
//				.build();
//		ExecuteStatementResponse executeStatementResponse = dbclient.executeStatement(executeStatementRequest);
//		List<Map<String, AttributeValue>> items = executeStatementResponse.items();
//
//		System.out.println(items);
        assertTrue(true)
    }

    companion object {
        /**
         * @return the suite of tests being tested
         */
        fun suite(): Test {
            return TestSuite(PartiQLTest::class.java)
        }
    }
}