package com.silverboxsoft.dynamodbtool.dao

import kotlin.Throws
import com.silverboxsoft.dynamodbtool.classes.DynamoDbConnectInfo
import java.net.URISyntaxException
import software.amazon.awssdk.services.dynamodb.model.TableDescription
import com.silverboxsoft.dynamodbtool.classes.DynamoDbResult
import software.amazon.awssdk.services.dynamodb.model.ScanRequest

/**
 * https://github.com/awsdocs/aws-doc-sdk-examples/blob/master/javav2/example_code/dynamodb/src/main/java/com/example/dynamodb/DynamoDBScanItems.java
 *
 * @author tanakaeiji
 */
class ScanDao(connInfo: DynamoDbConnectInfo?) : AbsDao(connInfo) {
    @Throws(URISyntaxException::class)
    fun getResult(tableInfo: TableDescription?): DynamoDbResult {
        val tableName = tableInfo!!.tableName()
        val ddb = dbClient
        return try {
            val scanRequest = ScanRequest.builder().tableName(tableName).build()
            DynamoDbResult(ddb!!.scan(scanRequest).items(), tableInfo)
        } finally {
            ddb!!.close()
        }
    }
}