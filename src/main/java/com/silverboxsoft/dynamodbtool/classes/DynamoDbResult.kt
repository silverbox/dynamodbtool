package com.silverboxsoft.dynamodbtool.classes

import java.util.HashMap
import com.silverboxsoft.dynamodbtool.utils.DynamoDbUtils
import java.util.stream.Collectors
import javafx.collections.ObservableList
import javafx.collections.FXCollections

import software.amazon.awssdk.services.dynamodb.model.*
import java.util.ArrayList

class DynamoDbResult(items: List<Map<String, AttributeValue>>, tableInfo: TableDescription) {
    private var columnList: MutableList<DynamoDbColumn> = ArrayList()
    private val colNameIndex: MutableMap<String, Int> = HashMap()
    private val resItems: MutableList<DynamoDbViewRecord> = ArrayList()
    private val rawResItems: MutableList<Map<String, AttributeValue>> = ArrayList()

    private fun prepareKeyInfo(tableInfo: TableDescription) {
        columnList = DynamoDbUtils.Companion.getSortedDynamoDbColumnList(tableInfo)
        for (idx in columnList.indices) {
            colNameIndex[columnList[idx].columnName] = idx
        }
    }

    private fun initilize(items: List<Map<String, AttributeValue>>) {
        for (resItem in items) {
            addRecord(resItem)
        }
    }

    fun prepareOneTableRecord(resItem: Map<String, AttributeValue>): ObservableList<String> {
        val record = FXCollections.observableArrayList<String>()

        // pick up data which attribute name is set.
        for (colIdx in columnList.indices) {
            val dbCol = getDynamoDbColumn(colIdx)
            val columnName = dbCol.columnName
            record.add(DynamoDbUtils.Companion.getAttrString(resItem[columnName]))
        }

        // pick up the data which attribute name is come yet.
        val unsetKeyNameSet = resItem.keys.stream().collect(Collectors.toSet())
        unsetKeyNameSet.removeAll(colNameIndex.keys)
        for (newColName in unsetKeyNameSet) {
            val attrVal = resItem[newColName]
            val dbCol = DynamoDbColumn(newColName, DynamoDbUtils.Companion.getDynamoDbColumnType(attrVal))
            fillNewColumn()
            record.add(DynamoDbUtils.Companion.getAttrString(attrVal))
            colNameIndex[newColName] = columnList.size
            columnList.add(dbCol)
        }
        return record
    }

    private fun fillNewColumn() {
        resItems.stream().forEach { rec: DynamoDbViewRecord -> rec.getData().plus(DynamoDbUtils.Companion.NO_VALSTR) }
    }

    val columnCount: Int
        get() = columnList.size
    val recordCount: Int
        get() = resItems.size
    val resultItems: List<DynamoDbViewRecord>
        get() = resItems

    fun getRawResItems(): List<Map<String, AttributeValue>> {
        return rawResItems
    }

    fun getDynamoDbColumn(index: Int): DynamoDbColumn {
        return columnList[index]
    }

    fun addRecord(newRec: Map<String, AttributeValue>): DynamoDbViewRecord {
        rawResItems.add(newRec)
        val data = prepareOneTableRecord(newRec)
        val record: DynamoDbViewRecord = DynamoDbViewRecord(resItems.size, data)
        resItems.add(record)
        return record
    }

    fun updateRecord(rowIndex: Int, newRec: Map<String, AttributeValue>): DynamoDbViewRecord {
        rawResItems[rowIndex] = newRec
        val data = prepareOneTableRecord(newRec)
        val record = resItems[rowIndex]
        record.setData(data)
        resItems[rowIndex] = record
        return record
    }

    fun removeRecord(rowIndex: Int) {
        rawResItems.removeAt(rowIndex)
    }

    fun getColumnIndexByName(colName: String): Int {
        return colNameIndex.getOrDefault(colName, -1)
    }

    init {
        prepareKeyInfo(tableInfo)
        initilize(items)
    }
}