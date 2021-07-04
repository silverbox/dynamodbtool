package com.silverboxsoft.dynamodbtool.controller.inputdialog

import javafx.scene.control.CheckBox
import java.util.HashMap
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import java.util.HashSet
import javafx.scene.Node
import java.util.ArrayList

abstract class AbsDynamoDbSetInputDialog<T>(dynamoDbRecord: List<T>?, dialogTitle: String?) : AbsDynamoDbInputDialog<List<T>>(dynamoDbRecord, dialogTitle) {
    /*
	 * accessor
	 */
    protected var addValueNode: Node? = null
    private var addAttributeMap: MutableMap<String, T>? = null
    protected override val headerWidthList: List<Int>
        protected get() {
            val retList: MutableList<Int> = ArrayList()
            retList.add(AbsDynamoDbInputDialog.Companion.FILELD_WIDTH)
            retList.add(AbsDynamoDbInputDialog.Companion.DEL_COL_WIDTH)
            return retList
        }
    protected override val headerLabelList: List<Node?>
        protected get() {
            val valTtilelabel = getContentLabel("VALUE", true)
            val delTtilelabel = getContentLabel("DEL", true)
            val retList: MutableList<Node?> = ArrayList()
            retList.add(valTtilelabel)
            retList.add(delTtilelabel)
            return retList
        }
    protected override val bodyAttributeNodeList: List<List<Node?>?>
        protected get() {
            val retList: MutableList<List<Node?>?> = ArrayList()
            for (recIdx in dynamoDbRecordOrg.indices) {
                val attrVal = dynamoDbRecordOrg[recIdx]
                retList.add(getOneBodyAttributeNodeList(recIdx, attrVal))
            }
            return retList
        }
    protected override val footerNodeList: List<Node?>
        protected get() {
            val attrVal = initAttribute
            val recIdx = newRecIdx
            addValueNode = getAttrubuteBox(recIdx, attrVal)
            val retList: MutableList<Node?> = ArrayList()
            retList.add(addValueNode)
            retList.add(addButton)
            return retList
        }
    protected override val valueColIndex: Int
        protected get() = 0
    protected override val editedDynamoDbRecord: R
        protected get() {
            val retList: MutableList<T> = ArrayList()
            val currentBodyNodeList = currentBodyNodeList
            for (wkNodeList in currentBodyNodeList!!) {
                val delCheck = wkNodeList!![1] as CheckBox
                if (delCheck.isSelected) {
                    continue
                }
                val valueNode = wkNodeList[0]
                retList.add(getCurrentAttrubuteValue(valueNode))
            }
            return retList
        }
    protected override val emptyAttr: R
        protected get() = ArrayList<T>()

    override fun actAddNewAttribute() {
        val recIdx = newRecIdx
        val newIdStr = recIdx.toString()
        val attrVal = getCurrentAttrubuteValue(addValueNode)
        if (!validationCheck(attrVal)) {
            return
        }
        addAttributeMap!![newIdStr] = attrVal
        val nodelList = getOneBodyAttributeNodeList(recIdx, attrVal)
        addAttributeNodeList(nodelList)
    }

    override val isFinalValidationOk: Boolean
        get() {
            val checkSet: MutableSet<T> = HashSet()
            val currentBodyNodeList = currentBodyNodeList
            for (wkNodeList in currentBodyNodeList!!) {
                val delCheck = wkNodeList!![1] as CheckBox
                if (delCheck.isSelected) {
                    continue
                }
                val valueNode = wkNodeList[0]
                val wkVal: T? = getCurrentAttrubuteValue(valueNode)
                if (wkVal == null) {
                    val alert = Alert(AlertType.ERROR, AbsDynamoDbInputDialog.Companion.VALIDATION_MSG_INVALID_VALUE)
                    alert.showAndWait()
                    valueNode!!.requestFocus()
                    return false
                }
                if (checkSet.contains(wkVal)) {
                    val alert = Alert(AlertType.ERROR, VALIDATION_MSG_DUP_VALUE)
                    alert.showAndWait()
                    valueNode!!.requestFocus()
                    return false
                }
                checkSet.add(wkVal)
            }
            return true
        }

    /*
	 * 
	 */
    abstract val typeString: String?
    abstract fun getAttrubuteBox(recIndex: Int, attr: T): Node?
    abstract fun getCurrentAttrubuteValue(valueNode: Node?): T
    abstract val initAttribute: T

    /*
	 * for validation
	 */
    abstract fun isSameValue(valA: T, valB: T): Boolean

    /*
	 * 
	 */
    protected val newRecIdx: Int
        protected get() = dynamoDbRecordOrg.size + getAddAttributeMap().size

    protected fun getAddAttributeMap(): Map<String, T> {
        if (addAttributeMap == null) {
            addAttributeMap = HashMap()
        }
        return addAttributeMap
    }

    protected fun validationCheck(attrVal: T): Boolean {
        val currentBodyNodeList = currentBodyNodeList
        for (wkNodeList in currentBodyNodeList!!) {
            val valuebox = wkNodeList!![0]
            val wkAttr = getCurrentAttrubuteValue(valuebox)
            if (isSameValue(wkAttr, attrVal)) {
                val alert = Alert(AlertType.ERROR, VALIDATION_MSG_DUP_VALUE)
                alert.showAndWait()
                addValueNode!!.requestFocus()
                return false
            }
        }
        return true
    }

    private fun getOneBodyAttributeNodeList(recIdx: Int, attrVal: T): List<Node?> {
        val valueNode = getAttrubuteBox(recIdx, attrVal)
        val delCheck = CheckBox()
        delCheck.id = AbsDynamoDbInputDialog.Companion.DEL_ID_PREFIX + recIdx.toString()
        val nodeList: MutableList<Node?> = ArrayList()
        nodeList.add(valueNode)
        nodeList.add(delCheck)
        return nodeList
    }

    companion object {
        protected const val VALIDATION_MSG_DUP_VALUE = "Same value exists. please change the value"
    }

    init {
        this.title = this.title + String.format("[%s Set]", typeString)
    }
}