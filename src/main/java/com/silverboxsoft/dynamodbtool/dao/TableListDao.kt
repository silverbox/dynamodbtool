package com.silverboxsoft.dynamodbtool.dao

import com.silverboxsoft.dynamodbtool.classes.DynamoDbConnectInfo
import kotlin.Throws
import java.net.URISyntaxException
import com.silverboxsoft.dynamodbtool.fxmodel.TableNameCondType
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse
import software.amazon.awssdk.services.dynamodb.model.ListTablesRequest
import software.amazon.awssdk.utils.StringUtils
import java.util.ArrayList

/**
 * https://github.com/awsdocs/aws-doc-sdk-examples/blob/master/javav2/example_code/dynamodb/src/main/java/com/example/dynamodb/ListTables.java
 *
 * @author tanakaeiji
 */
class TableListDao(connInfo: DynamoDbConnectInfo?) : AbsDao(connInfo) {
    @Throws(URISyntaxException::class)
    fun getTableList(condition: String, conditionType: TableNameCondType): List<String> {
        val ddb = dbClient
        return try {
            getAllTableNameList(ddb, condition, conditionType)
        } finally {
            ddb.close()
        }
    }

    private fun getAllTableNameList(ddb: DynamoDbClient, condition: String, conditionType: TableNameCondType): List<String> {
        var moreTables = true
        var lastName: String? = null
        val retList: MutableList<String> = ArrayList()
        while (moreTables) {
            var response: ListTablesResponse? = null
            response = if (lastName == null) {
                val request = ListTablesRequest.builder().build()
                ddb.listTables(request)
            } else {
                val request = ListTablesRequest.builder()
                        .exclusiveStartTableName(lastName).build()
                ddb.listTables(request)
            }
            val tableNames = response.tableNames()
            if (tableNames.size > 0) {
                for (curName in tableNames) {
                    if (isMatchName(curName, condition, conditionType)) {
                        retList.add(curName)
                    }
                }
            }
            lastName = response.lastEvaluatedTableName()
            if (lastName == null) {
                moreTables = false
            }
        }
        return retList
    }

    private fun isMatchName(targetStr: String, condition: String, conditionType: TableNameCondType): Boolean {
        if (StringUtils.isEmpty(condition)) {
            return true
        }
        if (conditionType == TableNameCondType.PARTIAL_MATCH) {
            return targetStr.indexOf(condition) >= 0
        } else if (conditionType == TableNameCondType.HEAD_MATCH) {
            return targetStr.startsWith(condition)
        }
        return targetStr.endsWith(condition)
    }
}