package com.silverboxsoft.dynamodbtool.classes

import lombok.Data

@Data
class DynamoDbCondition(columnName: String, conditionType: DynamoDbConditionType, value: String) {
    private val columnName: String = columnName
    private val conditionType: DynamoDbConditionType = conditionType
    private val value: String = value
    val conditionExpression: String
        get() = alias + conditionType.getCondStr() + ":" + columnName
    val alias: String
        get() = "#$columnName"
}