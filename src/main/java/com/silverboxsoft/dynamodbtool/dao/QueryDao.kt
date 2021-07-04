package com.silverboxsoft.dynamodbtool.dao

import kotlin.Throws
import com.silverboxsoft.dynamodbtool.classes.DynamoDbConnectInfo
import java.net.URISyntaxException
import com.silverboxsoft.dynamodbtool.classes.DynamoDbResult
import com.silverboxsoft.dynamodbtool.classes.DynamoDbConditionJoinType
import com.silverboxsoft.dynamodbtool.classes.DynamoDbCondition
import java.util.HashMap
import software.amazon.awssdk.services.dynamodb.model.*
import java.lang.StringBuilder

/*
 * https://github.com/awsdocs/aws-doc-sdk-examples/blob/master/javav2/example_code/dynamodb/src/main/java/com/example/dynamodb/Query.java
 */
class QueryDao(connInfo: DynamoDbConnectInfo?) : AbsDao(connInfo) {
    @Throws(URISyntaxException::class)
    fun getResult(tableInfo: TableDescription?, conditionJoinType: DynamoDbConditionJoinType,
                  conditionList: List<DynamoDbCondition>): DynamoDbResult {
        val tableName = tableInfo!!.tableName()
        val ddb = dbClient
        return try {
            val attrNameAlias = HashMap<String?, String>()
            val attrValues = HashMap<String, AttributeValue>()
            val conditionExpression = StringBuilder()
            for (dbCond in conditionList) {
                attrNameAlias[dbCond.alias] = dbCond.columnName
                attrValues[":" + dbCond.columnName] = AttributeValue.builder().s(dbCond.value).build()
                if (conditionExpression.length > 0) {
                    conditionExpression.append(conditionJoinType.joinStr)
                }
                conditionExpression.append(dbCond.conditionExpression)
            }
            val queryReq = QueryRequest.builder()
                    .tableName(tableName)
                    .keyConditionExpression(conditionExpression.toString())
                    .expressionAttributeNames(attrNameAlias)
                    .expressionAttributeValues(attrValues)
                    .build()
            val response = ddb!!.query(queryReq)
            DynamoDbResult(response.items(), tableInfo)
        } finally {
            ddb!!.close()
        }
    }
}