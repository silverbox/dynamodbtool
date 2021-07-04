package com.silverboxsoft.dynamodbtool.dao

import kotlin.Throws
import com.silverboxsoft.dynamodbtool.classes.DynamoDbConnectInfo
import java.net.URISyntaxException
import software.amazon.awssdk.services.dynamodb.model.TableDescription
import com.silverboxsoft.dynamodbtool.classes.DynamoDbResult
import software.amazon.awssdk.services.dynamodb.model.ExecuteStatementRequest

/*
 * https://github.com/awsdocs/aws-doc-sdk-examples/blob/master/javav2/example_code/dynamodb/src/main/java/com/example/dynamodb/Query.java
 */
class PartiQLDao(connInfo: DynamoDbConnectInfo?) : AbsDao(connInfo) {
    @Throws(URISyntaxException::class)
    fun getResult(tableInfo: TableDescription?, partiQL: String?): DynamoDbResult {
        val ddb = dbClient
        return try {
            val executeStatementRequest =  //
                    ExecuteStatementRequest.builder().statement(partiQL).build()
            val executeStatementResponse = ddb!!.executeStatement(executeStatementRequest)
            DynamoDbResult(executeStatementResponse.items(), tableInfo)
        } finally {
            ddb!!.close()
        }
    }
}