package com.silverboxsoft.dynamodbtool.classes

enum class DynamoDbConditionType(condStr: String) {
    EQUAL(" = "), GREATER_THAN(" > "), LESSER_THAN(" < "), GREATER_THAN_EQ(" >= "), LESSER_THAN_EQ(" <= ");
    private val condStr = condStr
    fun getCondStr(): String{
        return this.condStr
    }
}