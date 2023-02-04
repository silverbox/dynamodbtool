package com.silverboxsoft.dynamodbtool.classes

import com.silverboxsoft.dynamodbtool.controller.inputdialog.*
import com.silverboxsoft.dynamodbtool.utils.DynamoDbUtils
import java.util.HashMap
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.dynamodb.model.*
import java.math.BigDecimal
import java.util.ArrayList
import java.util.stream.Collectors

enum class DynamoDbColumnType(//
        val displayStr: String, val displayOrder: Int, val category: DynamoDbColumnTypeCategory) {
    STRING("STRING", 0, DynamoDbColumnTypeCategory.SCALAR),  //
    NUMBER("NUMBER", 1, DynamoDbColumnTypeCategory.SCALAR),  //
    BOOLEAN("BOOL", 2, DynamoDbColumnTypeCategory.SCALAR),  //
    BINARY("BINARY", 3, DynamoDbColumnTypeCategory.SCALAR),  //
    NULL("NULL", 4, DynamoDbColumnTypeCategory.SCALAR),  //
    STRING_SET("Set of STRING", 5, DynamoDbColumnTypeCategory.SET),  //
    NUMBER_SET("Set of NUMBER", 6, DynamoDbColumnTypeCategory.SET),  //
    BINARY_SET("Set of BINARY", 7, DynamoDbColumnTypeCategory.SET),  //
    MAP("MAP", 8, DynamoDbColumnTypeCategory.DOCUMENT),  //
    LIST("LIST", 9, DynamoDbColumnTypeCategory.DOCUMENT),  //
    UNKNOWN("----", 99, DynamoDbColumnTypeCategory.SCALAR);

    val initValue: AttributeValue
        get() {
            return when(this){
                STRING -> AttributeValue.builder().s("").build()
                NUMBER -> AttributeValue.builder().n("0").build()
                BOOLEAN -> AttributeValue.builder().bool(true).build()
                BINARY -> AttributeValue.builder().b(SdkBytes.fromByteArray(ByteArray(0))).build()
                NULL -> AttributeValue.builder().nul(true).build()
                STRING_SET -> {
                    val initSs = ArrayList<String?>()
                    initSs.add("")
                    AttributeValue.builder().ss(initSs).build()
                }
                NUMBER_SET -> {
                    val initNs = ArrayList<String?>()
                    initNs.add("0")
                    AttributeValue.builder().ns(initNs).build()
                }
                BINARY_SET -> {
                    val initBs = ArrayList<SdkBytes?>()
                    initBs.add(SdkBytes.fromByteArray(ByteArray(0)))
                    AttributeValue.builder().bs(initBs).build()
                }
                LIST -> AttributeValue.builder().l(ArrayList()).build()
                MAP -> AttributeValue.builder().m(HashMap()).build()
                else -> AttributeValue.builder().s("").build()
            }
        }

    companion object {
        fun getColumnType(displayStr: String): DynamoDbColumnType {
            for (type in values()) {
                if (type.displayStr == displayStr) {
                    return type
                }
            }
            return UNKNOWN
        }
    }
}