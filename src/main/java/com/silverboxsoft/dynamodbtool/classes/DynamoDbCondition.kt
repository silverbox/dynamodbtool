package com.silverboxsoft.dynamodbtool.classes

import lombok.Data

@Data
class DynamoDbCondition(val columnName: String, private val conditionType: DynamoDbConditionType, val value: String) {
    val conditionExpression: String
        get() = alias + conditionType.condStr + ":" + columnName
    val alias: String
        get() = "#$columnName"
}