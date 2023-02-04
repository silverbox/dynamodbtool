package com.silverboxsoft.dynamodbtool.controller.inputdialog

import javafx.scene.control.CheckBox
import java.util.HashMap
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import java.util.HashSet
import javafx.scene.Node
import javafx.scene.control.TextField
import java.util.ArrayList

abstract class AbsDynamoDbSetInputDialog<T>(dynamoDbRecord: List<T>, dialogTitle: String)
    : AbsDynamoDbInputDialog<List<T>>(dynamoDbRecord, dialogTitle) {
    /*
	 * accessor
	 */
    protected var addValueNode: Node = TextField() //dummy
    private var addAttributeMap: MutableMap<String, T> = HashMap()
    override val headerWidthList: List<Int>
        get() {
            val retList: MutableList<Int> = ArrayList()
            retList.add(AbsDynamoDbInputDialog.Companion.FILELD_WIDTH)
            retList.add(AbsDynamoDbInputDialog.Companion.DEL_COL_WIDTH)
            return retList
        }
    override val headerLabelList: List<Node>
        get() {
            val valTitleLabel = getContentLabel("VALUE", true)
            val delTitleLabel = getContentLabel("DEL", true)
            val retList: MutableList<Node> = ArrayList()
            retList.add(valTitleLabel)
            retList.add(delTitleLabel)
            return retList
        }
    override val bodyAttributeNodeList: List<List<Node>>
        get() {
            val retList: MutableList<List<Node>> = ArrayList()
            for (recIdx in dynamoDbRecordOrg.indices) {
                val attrVal = dynamoDbRecordOrg[recIdx]
                retList.add(getOneBodyAttributeNodeList(recIdx, attrVal))
            }
            return retList
        }
    override val valueColIndex: Int
        get() = 0
    override val editedDynamoDbRecord: List<T>
        get() {
            val retList: MutableList<T> = ArrayList()
            val currentBodyNodeList = currentBodyNodeList
            for (wkNodeList in currentBodyNodeList) {
                val delCheck = wkNodeList[1] as CheckBox
                if (delCheck.isSelected) {
                    continue
                }
                val valueNode = wkNodeList[0]
                retList.add(getCurrentAttributeValue(valueNode))
            }
            return retList
        }
    override val emptyAttr: List<T>
        get() = ArrayList<T>()

    override fun getFooterNodeList(): List<Node> {
        addValueNode = getAttributeBox(newRecIdx, initialAttribute)
        val retList: MutableList<Node> = ArrayList()
        retList.add(addValueNode)
        retList.add(addButton)
        return retList
    }

    override fun isValueChanged(): Boolean {
        val edited =  editedDynamoDbRecord
        if (edited.size != dynamoDbRecordOrg.size) {
            return true
        }
        for (recIdx in dynamoDbRecordOrg.indices) {
            val orgVal = dynamoDbRecordOrg[recIdx]
            val curVal = edited[recIdx]
            if (!isSameValue(orgVal, curVal)) {
                return true
            }
        }
        return false
    }
    override fun isAddValueRemain(): Boolean {
        val curVal = getCurrentAttributeValue(addValueNode)
        return !isSameValue(initialAttribute, curVal)
    }

    override fun actAddNewAttribute() {
        val recIdx = newRecIdx
        val newIdStr = recIdx.toString()
        val attrVal = getCurrentAttributeValue(addValueNode)
        if (!validationCheck(attrVal)) {
            return
        }
        addAttributeMap[newIdStr] = attrVal
        val nodeList = getOneBodyAttributeNodeList(recIdx, attrVal)
        addAttributeNodeList(nodeList)
    }

    override val isFinalValidationOk: Boolean
        get() {
            val checkSet: MutableSet<T> = HashSet()
            val currentBodyNodeList = currentBodyNodeList
            var itemCount = 0
            for (wkNodeList in currentBodyNodeList) {
                val delCheck = wkNodeList[1] as CheckBox
                if (delCheck.isSelected) {
                    continue
                }
                val valueNode = wkNodeList[0]
                val wkVal: T = getCurrentAttributeValue(valueNode)
                if (wkVal == null) {
                    val alert = Alert(AlertType.ERROR, AbsDynamoDbInputDialog.Companion.VALIDATION_MSG_INVALID_VALUE)
                    alert.showAndWait()
                    valueNode.requestFocus()
                    return false
                }
                if (checkSet.contains(wkVal)) {
                    val alert = Alert(AlertType.ERROR, VALIDATION_MSG_DUP_VALUE)
                    alert.showAndWait()
                    valueNode.requestFocus()
                    return false
                }
                itemCount += 1
                checkSet.add(wkVal)
            }
            if (itemCount == 0) {
                val alert = Alert(AlertType.ERROR, VALIDATION_MSG_EMPTY)
                alert.showAndWait()
                return false
            }
            return true
        }

    /*
	 * 
	 */
    abstract val typeString: String
    abstract fun getAttributeBox(recIndex: Int, attr: T): Node
    abstract fun getCurrentAttributeValue(valueNode: Node): T
    abstract val initialAttribute: T

    /*
	 * for validation
	 */
    abstract fun isSameValue(valA: T, valB: T): Boolean

    /*
	 * 
	 */
    private val newRecIdx: Int
        get() = dynamoDbRecordOrg.size + getAddAttributeMap().size

    private fun getAddAttributeMap(): Map<String, T> {
        return addAttributeMap
    }

    private fun validationCheck(attrVal: T): Boolean {
        val currentBodyNodeList = currentBodyNodeList
        for (wkNodeList in currentBodyNodeList) {
            val valueBox = wkNodeList!![0]
            val wkAttr = getCurrentAttributeValue(valueBox)
            if (isSameValue(wkAttr, attrVal)) {
                val alert = Alert(AlertType.ERROR, VALIDATION_MSG_DUP_VALUE)
                alert.showAndWait()
                addValueNode.requestFocus()
                return false
            }
        }
        return true
    }

    private fun getOneBodyAttributeNodeList(recIdx: Int, attrVal: T): List<Node> {
        val valueNode = getAttributeBox(recIdx, attrVal)
        val delCheck = CheckBox()
        delCheck.id = AbsDynamoDbInputDialog.Companion.DEL_ID_PREFIX + recIdx.toString()
        val nodeList: MutableList<Node> = ArrayList()
        nodeList.add(valueNode)
        nodeList.add(delCheck)
        return nodeList
    }

    companion object {
        protected const val VALIDATION_MSG_DUP_VALUE = "Same value exists. please change the value"
        protected const val VALIDATION_MSG_EMPTY = "Empty set not allowed. please set one or more values"
    }

    init {
        this.title = this.title + String.format("[%s Set]", typeString)
    }
}