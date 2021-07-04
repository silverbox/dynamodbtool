package com.silverboxsoft.dynamodbtool.dao

import kotlin.Throws
import com.silverboxsoft.dynamodbtool.classes.DynamoDbConnectInfo
import java.net.URISyntaxException
import software.amazon.awssdk.services.dynamodb.model.TableDescription
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest

/**
 * https://github.com/awsdocs/aws-doc-sdk-examples/blob/master/javav2/example_code/dynamodb/src/main/java/com/example/dynamodb/DescribeTable.java
 *
 * @author tanakaeiji
 */
class TableInfoDao(connInfo: DynamoDbConnectInfo?) : AbsDao(connInfo) {
    @Throws(URISyntaxException::class)
    fun getTableDescription(tableName: String?): TableDescription {
        val request = DescribeTableRequest.builder()
                .tableName(tableName)
                .build()
        val ddb = dbClient
        return try {
            ddb!!.describeTable(request).table()
        } finally {
            ddb!!.close()
        }
    }
}