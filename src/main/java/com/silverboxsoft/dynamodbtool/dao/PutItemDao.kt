package com.silverboxsoft.dynamodbtool.dao

import kotlin.Throws
import com.silverboxsoft.dynamodbtool.classes.DynamoDbConnectInfo
import java.net.URISyntaxException
import software.amazon.awssdk.services.dynamodb.model.*

/**
 * https://github.com/awsdocs/aws-doc-sdk-examples/blob/master/javav2/example_code/dynamodb/src/main/java/com/example/dynamodb/PutItem.java
 *
 * @author tanakaeiji
 */
class PutItemDao(connInfo: DynamoDbConnectInfo) : AbsDao(connInfo) {
    @Throws(URISyntaxException::class)
    fun putItem(tableInfo: TableDescription?, dynamoDbRec: Map<String, AttributeValue>): PutItemResponse {
        val tableName = tableInfo!!.tableName()
        val ddb = dbClient
        return try {
            val request = PutItemRequest.builder().tableName(tableName).item(dynamoDbRec).build()
            ddb.putItem(request)
        } finally {
            ddb.close()
        }
    }
}