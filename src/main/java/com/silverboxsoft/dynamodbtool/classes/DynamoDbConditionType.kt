package com.silverboxsoft.dynamodbtool.classes

enum class DynamoDbConditionType(val condStr: String) {
    EQUAL(" = "), GREATER_THAN(" > "), LESSER_THAN(" < "), GREATER_THAN_EQ(" >= "), LESSER_THAN_EQ(" <= ");
}