package com.silverboxsoft.dynamodbtool.classes

import lombok.Data

@Data
class DynamoDbColumn(val columnName: String, val columnType: DynamoDbColumnType)