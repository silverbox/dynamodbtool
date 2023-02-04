package com.silverboxsoft.dynamodbtool.utils

import java.lang.Exception
import com.silverboxsoft.dynamodbtool.classes.DynamoDbConnectInfo
import software.amazon.awssdk.core.SdkBytes
import java.util.stream.Collectors
import com.silverboxsoft.dynamodbtool.classes.DynamoDbColumnType
import java.math.BigDecimal
import com.silverboxsoft.dynamodbtool.classes.DynamoDbColumn
import com.silverboxsoft.dynamodbtool.classes.DynamoDbIndex
import java.lang.NumberFormatException

import software.amazon.awssdk.services.dynamodb.model.*
import java.lang.StringBuilder
import java.util.*
import java.util.function.Function

class DynamoDbUtils(connInfo: DynamoDbConnectInfo?) {
    companion object {
        const val STR_NO_VAL = "<unset>"
        fun getAttrString(attrVal: AttributeValue?): String {
            when {
                attrVal == null -> {
                    return STR_NO_VAL
                }
                attrVal.s() != null -> {
                    return attrVal.s()
                }
                attrVal.n() != null -> {
                    return attrVal.n()
                }
                attrVal.b() != null -> {
                    return getBase64StringFromSdkBytes(attrVal.b())
                }
                attrVal.bool() != null -> {
                    return attrVal.bool().toString()
                }
                attrVal.hasSs() -> {
                    return attrVal.ss().toString()
                }
                attrVal.hasNs() -> {
                    return attrVal.ns().toString()
                }
                attrVal.hasBs() -> {
                    return attrVal.bs().stream()
                        .map { attr: SdkBytes -> getBase64StringFromSdkBytes(attr) }
                        .collect(Collectors.toList()).toString()
                }
                attrVal.hasM() -> {
                    return attrVal.m().entries.stream()
                        .collect(Collectors.toMap(Function<Map.Entry<String?, AttributeValue?>, String?> { (key) -> key }, Function<Map.Entry<String?, AttributeValue?>, String> { (_, value) -> getAttrString(value) }))
                        .toString()
                }
                attrVal.hasL() -> {
                    return attrVal.l().stream().map { attr: AttributeValue? -> getAttrString(attr) }.collect(Collectors.toList()).toString()
                }
                isNullAttr(attrVal) -> {
                    return "<null>"
                }
                else -> return attrVal.toString()
            }
        }

        fun getAttrTypeString(attrVal: AttributeValue): String {
            when {
                attrVal == null -> {
                    return STR_NO_VAL
                }
                attrVal.s() != null -> {
                    return DynamoDbColumnType.STRING.displayStr
                }
                attrVal.n() != null -> {
                    return DynamoDbColumnType.NUMBER.displayStr
                }
                attrVal.b() != null -> {
                    return DynamoDbColumnType.BINARY.displayStr
                }
                attrVal.bool() != null -> {
                    return DynamoDbColumnType.BOOLEAN.displayStr
                }
                attrVal.hasSs() -> {
                    return DynamoDbColumnType.STRING_SET.displayStr
                }
                attrVal.hasNs() -> {
                    return DynamoDbColumnType.NUMBER_SET.displayStr
                }
                attrVal.hasBs() -> {
                    return DynamoDbColumnType.BINARY_SET.displayStr
                }
                attrVal.hasM() -> {
                    return DynamoDbColumnType.MAP.displayStr
                }
                attrVal.hasL() -> {
                    return DynamoDbColumnType.LIST.displayStr
                }
                isNullAttr(attrVal) -> {
                    return DynamoDbColumnType.NULL.displayStr
                }
                else -> return DynamoDbColumnType.UNKNOWN.displayStr
            }
        }

        /**
         * for item field
         *
         * @param attrVal
         * @return
         */
        // https://sdk.amazonaws.com/java/api/2.0.0/software/amazon/awssdk/services/dynamodb/model/AttributeValue.html
        fun getDynamoDbColumnType(attrVal: AttributeValue?): DynamoDbColumnType {
            when {
                attrVal == null -> {
                    return DynamoDbColumnType.NULL
                }
                attrVal.hasSs() -> {
                    return DynamoDbColumnType.STRING_SET
                }
                attrVal.hasNs() -> {
                    return DynamoDbColumnType.NUMBER_SET
                }
                attrVal.hasBs() -> {
                    return DynamoDbColumnType.BINARY_SET
                }
                attrVal.hasM() -> {
                    return DynamoDbColumnType.MAP
                }
                attrVal.hasL() -> {
                    return DynamoDbColumnType.LIST
                }
                attrVal.s() != null -> {
                    return DynamoDbColumnType.STRING
                }
                attrVal.b() != null -> {
                    return DynamoDbColumnType.BINARY
                }
                attrVal.bool() != null -> {
                    return DynamoDbColumnType.BOOLEAN
                }
                attrVal.n() != null -> {
                    return DynamoDbColumnType.NUMBER
                }
                else -> return DynamoDbColumnType.NULL
            }
        }

        /**
         * for primary key field
         *
         * @param attr
         * @return
         */
        private fun getDynamoDbColumnType(attr: AttributeDefinition): DynamoDbColumnType {
            return when {
                attr.attributeType() == ScalarAttributeType.S -> {
                    DynamoDbColumnType.STRING
                }
                attr.attributeType() == ScalarAttributeType.N -> {
                    DynamoDbColumnType.NUMBER
                }
                attr.attributeType() == ScalarAttributeType.B -> {
                    DynamoDbColumnType.BINARY
                }
                else -> DynamoDbColumnType.NULL
            }
        }

        fun getIndexInfo(indexName: String, keyInfo: List<KeySchemaElement>): DynamoDbIndex {
            var partitionKeyName: String = ""
            var sortKeyName: String? = null
            for (k in keyInfo) {
                if (k.keyType() == KeyType.HASH) {
                    partitionKeyName = k.attributeName()
                } else if (k.keyType() == KeyType.RANGE) {
                    sortKeyName = k.attributeName()
                }
            }
            return DynamoDbIndex(indexName, partitionKeyName, sortKeyName)
        }

        /**
         * sdkbytes converter
	     */
        fun getBase64StringFromSdkBytes(sdkByte: SdkBytes): String {
            return Base64.getEncoder().encodeToString(sdkByte.asByteArray())
        }

        fun getSdkBytesFromBase64String(base64Str: String?): SdkBytes {
            val byteAry = Base64.getDecoder().decode(base64Str)
            return SdkBytes.fromByteArray(byteAry)
        }

        /**
         * number converter
         */
        fun getNumStr(num: BigDecimal?): String {
            return num.toString()
        }

        fun getBigDecimal(str: String?): BigDecimal {
            return BigDecimal(str)
        }

        // work around of AttributeValue#nul()
        fun isNullAttr(attrVal: AttributeValue?): Boolean {
            val wkStr = attrVal.toString()
            return wkStr == "AttributeValue(NUL=true)"
        }

        fun getSortedAttrNameList(dynamoDbRecord: Map<String, AttributeValue>): List<String> {
            val attrNameList: List<String> = ArrayList(dynamoDbRecord.keys)
            return attrNameList.sortedWith(
                Comparator.comparing { attrName: String -> getDynamoDbColumnType(dynamoDbRecord[attrName]).displayOrder }
            )
        }

        fun getKeyValueStr(tableInfo: TableDescription?, dynamoDbRecord: Map<String, AttributeValue>): String {
            val sb = StringBuilder()

            val keyInfoList = tableInfo!!.keySchema()
            // prepare Key Info
            val indexInfo = getIndexInfo(tableInfo.tableName(), keyInfoList)
            sb.append(getAttrString(dynamoDbRecord!![indexInfo.hashKey]))
            if (indexInfo.sortKey != null) {
                sb.append(" - ")
                sb.append(getAttrString(dynamoDbRecord[indexInfo.sortKey]))
            }
            return sb.toString()
        }

        fun getSortedDynamoDbColumnList(tableInfo: TableDescription?): MutableList<DynamoDbColumn> {
            val columnList: MutableList<DynamoDbColumn> = ArrayList()
            val colNameIndex: MutableMap<String, Int> = HashMap()
            val keyInfoList = tableInfo!!.keySchema()
            val indexInfo = getIndexInfo(tableInfo.tableName(), keyInfoList)

            var isPartitionKeySet = false
            val wkSiColMap: MutableMap<String, DynamoDbColumn> = HashMap()
            // at first, set key column info
            for (attr in tableInfo.attributeDefinitions()) {
                val colName = attr.attributeName()
                val dbCol = DynamoDbColumn(colName, getDynamoDbColumnType(attr))
                if (colName == indexInfo.hashKey) {
                    colNameIndex[colName] = 0
                    columnList.add(0, dbCol)
                    isPartitionKeySet = true
                } else if (indexInfo.sortKey != null && colName == indexInfo.sortKey) {
                    colNameIndex[colName] = 1
                    columnList.add(if (isPartitionKeySet) 1 else 0, dbCol)
                } else {
                    wkSiColMap[colName] = dbCol
                }
            }

            // add global secondary index
            for (gsiDesc in tableInfo.globalSecondaryIndexes()) {
                val wkKeyInfoList = gsiDesc.keySchema()
                for (kse in wkKeyInfoList) {
                    val gsiColName = kse.attributeName()
                    if (!colNameIndex.containsKey(gsiColName)) {
                        val dummyDbCol = DynamoDbColumn(gsiColName, DynamoDbColumnType.NULL)
                        colNameIndex[gsiColName] = columnList.size
                        columnList.add(wkSiColMap.getOrDefault(gsiColName, dummyDbCol))
                    }
                }
            }

            // add global secondary index
            for (lsiDesc in tableInfo.localSecondaryIndexes()) {
                val wkKeyInfoList = lsiDesc.keySchema()
                for (kse in wkKeyInfoList) {
                    val lsiColName = kse.attributeName()
                    if (!colNameIndex.containsKey(lsiColName)) {
                        val dummyDbCol = DynamoDbColumn(lsiColName, DynamoDbColumnType.NULL)
                        colNameIndex[lsiColName] = columnList.size
                        columnList.add(wkSiColMap.getOrDefault(lsiColName, dummyDbCol))
                    }
                }
            }
            return columnList
        }

        // TODO don't use exception
        fun isNumericStr(checkStr: String?): Boolean {
            return try {
                getBigDecimal(checkStr)
                true
            } catch (e: NumberFormatException) {
                false
            }
        }

        fun isBase64Str(checkStr: String?): Boolean {
            return try {
                getSdkBytesFromBase64String(checkStr)
                true
            } catch (e: NumberFormatException) {
                false
            }
        }
    }

    init {
        throw Exception("it's util class")
    }
}