package com.silverboxsoft.dynamodbtool.classes

import lombok.Data

@Data
class DynamoDbCondition(columnName: String, conditionType: DynamoDbConditionType, value: String) {
    val columnName: String = columnName
    val conditionType: DynamoDbConditionType = conditionType
    val value: String = value
    val conditionExpression: String
        get() = alias + conditionType.getCondStr() + ":" + columnName
    val alias: String
        get() = "#$columnName"
}