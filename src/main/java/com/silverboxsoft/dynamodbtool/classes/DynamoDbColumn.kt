package com.silverboxsoft.dynamodbtool.classes

import lombok.Data

@Data
class DynamoDbColumn(columnNamePrm: String, columnType: DynamoDbColumnType) {
    private val columnName: String = columnNamePrm
    private val columnType: DynamoDbColumnType = columnType
    fun getColumnName(): String {
        return columnName
    }
    fun getColumnType(): DynamoDbColumnType {
        return columnType
    }
}