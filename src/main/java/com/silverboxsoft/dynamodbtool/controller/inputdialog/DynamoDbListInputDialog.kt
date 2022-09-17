package com.silverboxsoft.dynamodbtool.controller.inputdialog

import javafx.scene.layout.HBox
import java.util.HashMap
import com.silverboxsoft.dynamodbtool.utils.DynamoDbUtils
import javafx.scene.control.Alert.AlertType
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.*
import software.amazon.awssdk.services.dynamodb.model.*
import java.util.ArrayList

class DynamoDbListInputDialog(dynamoDbRecord: List<AttributeValue>, dialogTitle: String)
    : AbsDynamoDbDocumentInputDialog<List<AttributeValue>>(dynamoDbRecord, dialogTitle) {
    private var addAttrValueNode: Node = getAttributeBox(ADD_INDEX, selectedAddType.initValue)
    private val updAttributeMap: MutableMap<String, AttributeValue> = HashMap()
    private var addAttributeMap: MutableMap<String, AttributeValue> = HashMap()
    private var tempAddAttrValue: AttributeValue = AbsDynamoDbInputDialog.NULL_ATTRIBUTE
    override val headerWidthList: List<Int>
        get() {
            val retList: MutableList<Int> = ArrayList()
            retList.add(AbsDynamoDbInputDialog.Companion.NAME_COL_WIDTH)
            retList.add(AbsDynamoDbInputDialog.Companion.NAME_COL_WIDTH)
            retList.add(AbsDynamoDbInputDialog.Companion.FILELD_WIDTH)
            retList.add(AbsDynamoDbInputDialog.Companion.DEL_COL_WIDTH)
            return retList
        }
    override val headerLabelList: List<Node>
        get() {
            val indexTitleLabel = getContentLabel("INDEX", true)
            val typeTitleLabel = getContentLabel("TYPE", true)
            val valTitleLabel = getContentLabel("VALUE", true)
            val delTitleLabel = getContentLabel("DEL", true)
            val retList: MutableList<Node> = ArrayList()
            retList.add(indexTitleLabel)
            retList.add(typeTitleLabel)
            retList.add(valTitleLabel)
            retList.add(delTitleLabel)
            return retList
        }
    override val bodyAttributeNodeList: List<List<Node>>
        get() {
            val retList: MutableList<List<Node>> = ArrayList()
            for (recIdx in dynamoDbRecordOrg.indices) {
                val attrVal: AttributeValue = dynamoDbRecordOrg.get(recIdx)
                retList.add(getOneBodyAttributeNodeList(recIdx, attrVal))
            }
            return retList
        }
    override val valueColIndex: Int
        get() = 2
    override val editedDynamoDbRecord: List<AttributeValue>
        get() {
            val retList: MutableList<AttributeValue> = ArrayList()
            val currentBodyNodeList = currentBodyNodeList
            for (wkNodeList in currentBodyNodeList) {
                val delCheck = wkNodeList[COL_IDX_DEL] as CheckBox
                if (delCheck.isSelected) {
                    continue
                }
                val valueNode = wkNodeList[COL_IDX_FIELD] as Node
                val newAttr = getAttributeFromNode(valueNode)
                retList.add(newAttr)
            }
            return retList
        }
    override val emptyAttr: List<AttributeValue>
        get() = ArrayList<AttributeValue>()

    override fun getFooterNodeList(): List<Node> {
        val retList: MutableList<Node> = ArrayList()
        addAttrValueNode = getAttributeBox(ADD_INDEX, selectedAddType.initValue)
        retList.add(Label("new Index"))
        retList.add(typeComboBox)
        retList.add(addAttrValueNode)
        retList.add(addButton)
        return retList
    }

    override fun actAddNewAttribute() {
        val recIdx = newRecIdx
        val newIdStr = recIdx.toString()
        val attrVal = getAttributeFromNode(addAttrValueNode)
        if (!addValidationCheck(attrVal)) {
            return
        }
        getAddAttributeMap()[newIdStr] = attrVal
        val nodelList = getOneBodyAttributeNodeList(recIdx, attrVal)
        addAttributeNodeList(nodelList)
    }

    override val isFinalValidationOk: Boolean
        get() {
            val currentBodyNodeList = currentBodyNodeList
            for (wkNodeList in currentBodyNodeList!!) {
                val delCheck = wkNodeList!![COL_IDX_DEL] as CheckBox
                if (delCheck.isSelected) {
                    continue
                }
                val valueNode = wkNodeList[COL_IDX_FIELD]
                if (!checkValueNode(valueNode)) {
                    val alert = Alert(AlertType.ERROR, AbsDynamoDbInputDialog.Companion.VALIDATION_MSG_INVALID_VALUE)
                    alert.showAndWait()
                    valueNode.requestFocus()
                    return false
                }
            }
            return true
        }

    /*
	 * 
	 */
    override fun getAttributeFromEditButtonId(btnId: String): AttributeValue {
        val recIndexStr: String = btnId.substring(AbsDynamoDbInputDialog.Companion.EDTBTN_ID_PREFIX.length)
        if (recIndexStr == ADD_INDEX.toString()) {
            return tempAddAttrValue
        }
        return when {
            updAttributeMap.containsKey(recIndexStr) -> {
                updAttributeMap.getOrDefault(recIndexStr, AbsDynamoDbInputDialog.NULL_ATTRIBUTE)
            }
            addAttributeMap.containsKey(recIndexStr) -> {
                addAttributeMap.getOrDefault(recIndexStr, AbsDynamoDbInputDialog.NULL_ATTRIBUTE)
            }
            else -> {
                dynamoDbRecordOrg[Integer.valueOf(recIndexStr)]
            }
        }
    }

    public override fun getTitleAppendStr(btnId: String): String {
        return btnId.substring(AbsDynamoDbInputDialog.Companion.EDTBTN_ID_PREFIX.length)
    }

    override fun callBackSetNewAttribute(btnId: String, attrVal: AttributeValue) {
        val idStr: String = btnId.substring(AbsDynamoDbInputDialog.Companion.EDTBTN_ID_PREFIX.length)
        val valLabelNode = dialogPane.lookup(String.format("#%s", AbsDynamoDbInputDialog.Companion.VALLBL_ID_PREFIX + idStr))
        if (valLabelNode != null && valLabelNode is Label) {
            valLabelNode.text = DynamoDbUtils.Companion.getAttrString(attrVal)
        }
        if (idStr == ADD_INDEX.toString()) {
            tempAddAttrValue = attrVal
        }
        updAttributeMap[idStr] = attrVal
    }

    override val addAttrEditButtonId: String
        get() = AbsDynamoDbInputDialog.Companion.EDTBTN_ID_PREFIX + ADD_INDEX.toString()

    override fun onAddTypeComboSelChanged(oldValue: String?, newValue: String?) {
        tempAddAttrValue = AbsDynamoDbInputDialog.NULL_ATTRIBUTE
        updateFooter()
    }

    /*
	 * 
	 */
    private fun getOneBodyAttributeNodeList(recIdx: Int, attrVal: AttributeValue): List<Node> {
        val indexLabel: Label = AbsDynamoDbInputDialog.Companion.getContentLabel(recIdx.toString())
        val typeLabel: Label = AbsDynamoDbInputDialog.Companion.getContentLabel(DynamoDbUtils.Companion.getAttrTypeString(attrVal))
        val valueBox = getAttributeBox(recIdx, attrVal)
        val delCheck = CheckBox()
        delCheck.id = AbsDynamoDbInputDialog.Companion.DEL_ID_PREFIX + recIdx.toString()
        val nodeList: MutableList<Node> = ArrayList()
        nodeList.add(AbsDynamoDbInputDialog.Companion.addUnderlineStyleToNode(indexLabel))
        nodeList.add(AbsDynamoDbInputDialog.Companion.addUnderlineStyleToNode(typeLabel))
        nodeList.add(AbsDynamoDbInputDialog.Companion.addUnderlineStyleToNode(valueBox))
        nodeList.add(delCheck)
        return nodeList
    }

    private fun getAttributeBox(recIndex: Int, attrVal: AttributeValue): Node {
        val attrStr: String = DynamoDbUtils.Companion.getAttrString(attrVal)
        val textField = TextField(attrStr)
        when {
            attrVal!!.s() != null -> {
                textField.id = AbsDynamoDbInputDialog.Companion.STRFLD_ID_PREFIX + recIndex.toString()
                return textField
            }
            attrVal.n() != null -> {
                textField.id = AbsDynamoDbInputDialog.Companion.NUMFLD_ID_PREFIX + recIndex.toString()
                return textField
            }
            attrVal.b() != null -> {
                textField.id = AbsDynamoDbInputDialog.Companion.BINFLD_ID_PREFIX + recIndex.toString()
                return textField
            }
            attrVal.bool() != null -> {
                return AbsDynamoDbInputDialog.Companion.getBooleanInput(attrVal)
            }
            DynamoDbUtils.Companion.isNullAttr(attrVal) -> {
                return AbsDynamoDbInputDialog.Companion.nullViewLabel
            }
            else -> {
                return getAttrEditButton(recIndex, attrStr)
            }
        }
    }

    private fun getAttrEditButton(recIndex: Int, text: String): Node {
        val idStr = recIndex.toString()
        val hbox = HBox(AbsDynamoDbInputDialog.Companion.HGAP)
        val valLabel: Label = AbsDynamoDbInputDialog.Companion.getContentLabel(text)
        valLabel.id = AbsDynamoDbInputDialog.Companion.VALLBL_ID_PREFIX + idStr
        val button = Button()
        button.text = AbsDynamoDbInputDialog.Companion.BTN_TITLE
        button.id = AbsDynamoDbInputDialog.Companion.EDTBTN_ID_PREFIX + idStr
        button.onAction = EventHandler { event: ActionEvent ->
            val wkBtn = event.source as Button
            actOpenEditDialog(wkBtn.id)
        }
        hbox.children.addAll(button, valLabel)
        return hbox
    }

    private fun getAddAttributeMap(): MutableMap<String, AttributeValue> {
        return addAttributeMap
    }

    private val newRecIdx: Int
        get() = dynamoDbRecordOrg.size + getAddAttributeMap().size

    private fun addValidationCheck(attrVal: AttributeValue?): Boolean {
        if (attrVal == null) {
            val alert = Alert(AlertType.ERROR, AbsDynamoDbDocumentInputDialog.Companion.VALIDATION_MSG_NO_ATTR_VALUE)
            alert.showAndWait()
            addButton.requestFocus()
            return false
        }
        return true
    }

    companion object {
        private const val ADD_INDEX = -1
        private const val COL_IDX_FIELD = 2
        private const val COL_IDX_DEL = 3
    }
}