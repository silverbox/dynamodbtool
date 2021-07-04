package com.silverboxsoft

import junit.framework.TestCase
import junit.framework.Assert
import junit.framework.Test
import junit.framework.TestSuite
import com.silverboxsoft.AppTest

/**
 * Unit test for simple App.
 */
class AppTest
/**
 * Create the test case
 *
 * @param testName name of the test case
 */
    (testName: String?) : TestCase(testName) {
    /**
     * Rigourous Test :-)
     */
    fun testApp() {
        assertTrue(true)
    }

    companion object {
        /**
         * @return the suite of tests being tested
         */
        fun suite(): Test {
            return TestSuite(AppTest::class.java)
        }
    }
}