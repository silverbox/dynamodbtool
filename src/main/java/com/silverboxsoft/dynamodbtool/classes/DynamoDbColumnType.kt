package com.silverboxsoft.dynamodbtool.classes

import java.util.HashMap
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.dynamodb.model.*
import java.util.ArrayList

enum class DynamoDbColumnType(//
        val dispStr: String, val dispOrd: Int, val category: DynamoDbColumnTypeCategory) {
    STRING("STRING", 0, DynamoDbColumnTypeCategory.SCALAR),  //
    NUMBER("NUMBER", 1, DynamoDbColumnTypeCategory.SCALAR),  //
    BOOLEAN("BOOL", 2, DynamoDbColumnTypeCategory.SCALAR),  //
    BINARY("BINARY", 3, DynamoDbColumnTypeCategory.SCALAR),  //
    NULL("NULL", 4, DynamoDbColumnTypeCategory.SCALAR),  //
    STRING_SET("Set of STRING", 5, DynamoDbColumnTypeCategory.SET),  //
    NUMBER_SET("Set of NUMBER", 6, DynamoDbColumnTypeCategory.SET),  //
    BINARY_SET("Set of BINARY", 7, DynamoDbColumnTypeCategory.SET),  //
    MAP("MAP", 8, DynamoDbColumnTypeCategory.DOCUMENT),  //
    LIST("LIST", 9, DynamoDbColumnTypeCategory.DOCUMENT);

    val initValue: AttributeValue?
        get() {
            if (this == STRING) {
                return AttributeValue.builder().s("").build()
            } else if (this == NUMBER) {
                return AttributeValue.builder().n("0").build()
            } else if (this == BOOLEAN) {
                return AttributeValue.builder().bool(true).build()
            } else if (this == BINARY) {
                return AttributeValue.builder().b(SdkBytes.fromByteArray(ByteArray(0))).build()
            } else if (this == NULL) {
                return AttributeValue.builder().nul(true).build()
            } else if (this == STRING_SET) {
                return AttributeValue.builder().ss().build()
            } else if (this == NUMBER_SET) {
                return AttributeValue.builder().ns().build()
            } else if (this == BINARY_SET) {
                return AttributeValue.builder().bs().build()
            } else if (this == LIST) {
                return AttributeValue.builder().l(ArrayList()).build()
            } else if (this == MAP) {
                return AttributeValue.builder().m(HashMap()).build()
            }
            return null
        }

    companion object {
        fun getColumnType(dispStr: String?): DynamoDbColumnType? {
            for (type in values()) {
                if (type.dispStr == dispStr) {
                    return type
                }
            }
            return null
        }
    }
}