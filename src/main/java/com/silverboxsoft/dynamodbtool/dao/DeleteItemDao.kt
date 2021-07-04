package com.silverboxsoft.dynamodbtool.dao

import kotlin.Throws
import com.silverboxsoft.dynamodbtool.classes.DynamoDbConnectInfo
import java.net.URISyntaxException
import java.util.HashMap

import software.amazon.awssdk.services.dynamodb.model.*

/**
 * https://github.com/awsdocs/aws-doc-sdk-examples/blob/master/javav2/example_code/dynamodb/src/main/java/com/example/dynamodb/DeleteItem.java
 *
 * @author tanakaeiji
 */
class DeleteItemDao(connInfo: DynamoDbConnectInfo) : AbsDao(connInfo) {
    @Throws(URISyntaxException::class)
    fun deleteItem(tableInfo: TableDescription?, dynamoDbRec: Map<String, AttributeValue>): DeleteItemResponse {
        val tableName = tableInfo!!.tableName()
        val ddb = dbClient
        ddb.use { ddb ->
            val keyToGet = HashMap<String, AttributeValue?>()
            for (keyElem in tableInfo.keySchema()) {
                val keyAttr = keyElem.attributeName()
                keyToGet[keyAttr] = dynamoDbRec[keyAttr]
            }
            val deleteReq = DeleteItemRequest.builder().tableName(tableName).key(keyToGet).build()
            return ddb.deleteItem(deleteReq)
        }
    }
}