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
import software.amazon.awssdk.utils.StringUtils
import java.util.ArrayList

open class DynamoDbMapInputDialog(dynamoDbRecord: Map<String, AttributeValue>, dialogTitle: String)
    : AbsDynamoDbDocumentInputDialog<Map<String, AttributeValue>>(dynamoDbRecord, dialogTitle) {
    var addAttrNameTextField: TextField = TextField(ADD_ATTR_NAME)
    private var addAttrValueNode: Node = getAttributeBox(ADD_ATTR_NAME, selectedAddType.initValue)
    private val updAttributeMap: MutableMap<String, AttributeValue> = HashMap()
    private var tempAddAttrValue: AttributeValue = AbsDynamoDbInputDialog.NULL_ATTRIBUTE

    protected var attrNameList: MutableList<String> = ArrayList()
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
            val typeTitleLabel = getContentLabel("TYPE", true)
            val keyTitleLabel = getContentLabel("NAME", true)
            val valTitleLabel = getContentLabel("VALUE", true)
            val delTitleLabel = getContentLabel("DEL", true)
            val retList: MutableList<Node> = ArrayList()
            retList.add(typeTitleLabel)
            retList.add(keyTitleLabel)
            retList.add(valTitleLabel)
            retList.add(delTitleLabel)
            return retList
        }
    override val bodyAttributeNodeList: List<List<Node>>
        get() {
            val retList: MutableList<List<Node>> = ArrayList()
            val attrNameList: List<String> = DynamoDbUtils.getSortedAttrNameList(dynamoDbRecordOrg)
            for (attrName in attrNameList) {
                val wkAttr = dynamoDbRecordOrg.getOrDefault(attrName, NULL_ATTRIBUTE)
                retList.add(getOneBodyAttributeNodeList(attrName, wkAttr))
                //attrNameList.add(attrName)
            }
            return retList
        }
    override val editedDynamoDbRecord: Map<String, AttributeValue>
        get() {
            val retMap: MutableMap<String, AttributeValue> = HashMap()
            val currentBodyNodeList = currentBodyNodeList
            for (wkNodeList in currentBodyNodeList) {
                val delCheck = wkNodeList[delColIndex] as CheckBox
                if (delCheck.isSelected) {
                    continue
                }
                val valueNode = wkNodeList[valueColIndex]
                val keyLabel = wkNodeList[COL_IDX_NAME] as Label
                val newAttr = getAttributeFromNode(valueNode)
                retMap[keyLabel.text] = newAttr
            }
            return retMap
        }
    override val emptyAttr: Map<String, AttributeValue>
        get() = HashMap<String, AttributeValue>()

    override fun getFooterNodeList(): List<Node> {
        val retList: MutableList<Node> = ArrayList()
        addAttrValueNode = getAttributeBox(ADD_ATTR_NAME, selectedAddType.initValue)
        addAttrNameTextField.text = ""
        retList.add(typeComboBox)
        retList.add(addAttrNameTextField)
        retList.add(addAttrValueNode)
        retList.add(addButton)
        return retList
    }

    override fun actAddNewAttribute() {
        val attrName = addAttrNameTextField.text
        val attrVal = getAttributeFromNode(addAttrValueNode)
        if (!addValidationCheck(attrName, attrVal)) {
            return
        }
        updAttributeMap[attrName] = attrVal
        val nodeList = getOneBodyAttributeNodeList(attrName, attrVal)
        addAttributeNodeList(nodeList)
        attrNameList.add(attrName)
    }

    override val isFinalValidationOk: Boolean
        get() {
            val currentBodyNodeList = currentBodyNodeList
            for (wkNodeList in currentBodyNodeList) {
                val delCheck = wkNodeList[delColIndex] as CheckBox
                if (delCheck.isSelected) {
                    continue
                }
                val keylabel = wkNodeList[COL_IDX_NAME] as Label
                val valueNode = wkNodeList[valueColIndex]
                if (!checkValueNode(keylabel.text, valueNode)) {
                    val alert = Alert(AlertType.ERROR, AbsDynamoDbInputDialog.Companion.VALIDATION_MSG_INVALID_VALUE)
                    alert.showAndWait()
                    valueNode.requestFocus()
                    return false
                }
            }
            return true
        }

    protected open fun checkValueNode(attrName: String?, valueNode: Node?): Boolean {
        return super.checkValueNode(valueNode)
    }

    /*
	 * 
	 */
    override fun getAttributeFromEditButtonId(btnId: String): AttributeValue {
        val attrName: String = btnId.substring(AbsDynamoDbInputDialog.Companion.EDTBTN_ID_PREFIX.length)
        if (attrName == ADD_ATTR_NAME) {
            return tempAddAttrValue
        }
        return if (updAttributeMap.containsKey(attrName)) {
            updAttributeMap.getOrDefault(attrName, AbsDynamoDbInputDialog.NULL_ATTRIBUTE)
        } else {
            dynamoDbRecordOrg.getOrDefault(attrName, AbsDynamoDbInputDialog.NULL_ATTRIBUTE)
        }
    }

    public override fun getTitleAppendStr(btnId: String): String {
        return btnId.substring(AbsDynamoDbInputDialog.Companion.EDTBTN_ID_PREFIX.length)
    }

    override fun callBackSetNewAttribute(btnId: String, attrVal: AttributeValue) {
        val attrName: String = btnId.substring(AbsDynamoDbInputDialog.Companion.EDTBTN_ID_PREFIX.length)
        if (attrName == ADD_ATTR_NAME) {
            tempAddAttrValue = attrVal
        }
        val valLabelNode = dialogPane.lookup(String.format("#%s", AbsDynamoDbInputDialog.Companion.VALLBL_ID_PREFIX + attrName))
        if (valLabelNode != null && valLabelNode is Label) {
            valLabelNode.text = DynamoDbUtils.Companion.getAttrString(attrVal)
        }
        updAttributeMap[attrName] = attrVal
    }

    override val addAttrEditButtonId: String
        get() = AbsDynamoDbInputDialog.Companion.EDTBTN_ID_PREFIX + ADD_ATTR_NAME

    override fun onAddTypeComboSelChanged(oldValue: String?, newValue: String?) {
        tempAddAttrValue = AbsDynamoDbInputDialog.NULL_ATTRIBUTE
        updateFooter()
    }

    /*
	 * 
	 */
    protected open fun getOneBodyAttributeNodeList(attrName: String, attrValue: AttributeValue): List<Node> {
        val typeLabel: Label = AbsDynamoDbInputDialog.Companion.getContentLabel(DynamoDbUtils.Companion.getAttrTypeString(attrValue))
        val keyLabel: Label = AbsDynamoDbInputDialog.Companion.getContentLabel(attrName)
        val valueNode = getAttributeBox(attrName, attrValue)
        val delCheck = CheckBox()
        delCheck.id = AbsDynamoDbInputDialog.Companion.DEL_ID_PREFIX + attrName
        val nodeList: MutableList<Node> = ArrayList()
        nodeList.add(AbsDynamoDbInputDialog.Companion.addUnderlineStyleToNode(typeLabel))
        nodeList.add(AbsDynamoDbInputDialog.Companion.addUnderlineStyleToNode(keyLabel))
        nodeList.add(AbsDynamoDbInputDialog.Companion.addUnderlineStyleToNode(valueNode))
        nodeList.add(delCheck)
        return nodeList
    }

    private fun getAttributeBox(attrName: String?, attrVal: AttributeValue?): Node {
        val attrStr: String = DynamoDbUtils.Companion.getAttrString(attrVal)
        val textField = TextField(attrStr)
        when {
            attrVal!!.s() != null -> {
                textField.id = AbsDynamoDbInputDialog.Companion.STRFLD_ID_PREFIX + attrName
                return textField
            }
            attrVal.n() != null -> {
                textField.id = AbsDynamoDbInputDialog.Companion.NUMFLD_ID_PREFIX + attrName
                return textField
            }
            attrVal.b() != null -> {
                textField.id = AbsDynamoDbInputDialog.Companion.BINFLD_ID_PREFIX + attrName
                return textField
            }
            attrVal.bool() != null -> {
                return AbsDynamoDbInputDialog.Companion.getBooleanInput(attrVal)
            }
            DynamoDbUtils.Companion.isNullAttr(attrVal) -> {
                return AbsDynamoDbInputDialog.Companion.nullViewLabel
            }
            else -> {
                return getAttrEditButton(attrName, attrStr)
            }
        }
    }

    private fun getAttrEditButton(attrName: String?, text: String): HBox {
        val hbox = HBox(AbsDynamoDbInputDialog.Companion.HGAP)
        val valLabel: Label = AbsDynamoDbInputDialog.Companion.getContentLabel(text)
        valLabel.id = AbsDynamoDbInputDialog.Companion.VALLBL_ID_PREFIX + attrName
        val button = Button()
        button.text = AbsDynamoDbInputDialog.Companion.BTN_TITLE
        button.id = AbsDynamoDbInputDialog.Companion.EDTBTN_ID_PREFIX + attrName
        button.onAction = EventHandler { event: ActionEvent ->
            val wkBtn = event.source as Button
            actOpenEditDialog(wkBtn.id)
        }
        hbox.children.addAll(button, valLabel)
        return hbox
    }

    /*
	 * 
	 */
    private fun addValidationCheck(attrName: String, attrVal: AttributeValue?): Boolean {
        if (StringUtils.isEmpty(attrName)) {
            val alert = Alert(AlertType.ERROR, VALIDATION_MSG_NO_ATTR_NAME)
            alert.showAndWait()
            addAttrNameTextField!!.requestFocus()
            return false
        }
        if (updAttributeMap.containsKey(attrName)) {
            val alert = Alert(AlertType.ERROR, VALIDATION_MSG_DUP_ATTR_NAME)
            alert.showAndWait()
            addAttrNameTextField!!.requestFocus()
            return false
        }
        if (attrVal == null) {
            val alert = Alert(AlertType.ERROR, AbsDynamoDbDocumentInputDialog.Companion.VALIDATION_MSG_NO_ATTR_VALUE)
            alert.showAndWait()
            addButton.requestFocus()
            return false
        }
        return true
    }

    override val valueColIndex: Int
        get() = 2

    val delColIndex: Int
        get() = 3

    companion object {
        private const val ADD_ATTR_NAME = ""
        private const val COL_IDX_NAME = 1
        private const val ADD_INDEX = -1
        // private const val COL_IDX_VAL = 2
        // private const val COL_IDX_DEL = 3
        private const val VALIDATION_MSG_NO_ATTR_NAME = "Please input attribute name."
        private const val VALIDATION_MSG_DUP_ATTR_NAME = "Duplicated attribute name. please change the name."
    }
}