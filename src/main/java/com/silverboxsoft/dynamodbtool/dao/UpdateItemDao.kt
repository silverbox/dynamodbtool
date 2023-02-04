package com.silverboxsoft.dynamodbtool.dao

import com.silverboxsoft.dynamodbtool.classes.DynamoDbConnectInfo
import software.amazon.awssdk.services.dynamodb.model.*
import java.net.URISyntaxException
import java.util.HashMap

class UpdateItemDao(connInfo: DynamoDbConnectInfo) : AbsDao(connInfo) {
    @Throws(URISyntaxException::class)
    fun updateItem(tableInfo: TableDescription?, dynamoDbRec: Map<String, AttributeValue>): UpdateItemResponse {
        val tableName = tableInfo!!.tableName()
        val ddb = dbClient
        ddb.use { ddb ->
            val keyMapToUpd = HashMap<String, AttributeValue?>()
            val valMapToUpd = HashMap<String, AttributeValueUpdate?>()
            for (recElem in dynamoDbRec) {
                val attrName = recElem.key
                if (isKeyAttribute(tableInfo, attrName)) {
                    keyMapToUpd[attrName] = recElem.value
                } else {
                    val valUpd = AttributeValueUpdate.builder().value(recElem.value).action(AttributeAction.PUT).build()
                    valMapToUpd[attrName] = valUpd
                }
            }
            val updateReq = UpdateItemRequest.builder().tableName(tableName)
                .key(keyMapToUpd)
                .attributeUpdates(valMapToUpd)
                .build()
            return ddb.updateItem(updateReq)
        }
    }

    private fun isKeyAttribute(tableInfo: TableDescription?, attributeName: String): Boolean {
        for (keyElem in tableInfo!!.keySchema()) {
            if (attributeName == keyElem.attributeName()) {
                return true
            }
        }
        return false
    }
}