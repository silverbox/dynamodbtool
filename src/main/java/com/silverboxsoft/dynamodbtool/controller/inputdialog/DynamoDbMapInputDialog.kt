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

open class DynamoDbMapInputDialog(dynamoDbRecord: Map<String?, AttributeValue?>?, dialogTitle: String?) : AbsDynamoDbDocumentInputDialog<Map<String?, AttributeValue?>?>(dynamoDbRecord, dialogTitle) {
    protected var addAttrNameTextField: TextField? = null
        protected get() {
            if (field == null) {
                field = TextField(ADD_ATTR_NAME)
            }
            return field
        }
        private set
    private var addAttrValueNode: Node? = null
    private val updAttributeMap: MutableMap<String, AttributeValue?> = HashMap()
    protected var attrNameList: MutableList<String?>? = null
    protected override val headerWidthList: List<Int>
        protected get() {
            val retList: MutableList<Int> = ArrayList()
            retList.add(AbsDynamoDbInputDialog.Companion.NAME_COL_WIDTH)
            retList.add(AbsDynamoDbInputDialog.Companion.NAME_COL_WIDTH)
            retList.add(AbsDynamoDbInputDialog.Companion.FILELD_WIDTH)
            retList.add(AbsDynamoDbInputDialog.Companion.DEL_COL_WIDTH)
            return retList
        }
    protected override val headerLabelList: List<Node?>
        protected get() {
            val typeTitilelabel = getContentLabel("TYPE", true)
            val keyTitilelabel = getContentLabel("NAME", true)
            val valTtilelabel = getContentLabel("VALUE", true)
            val delTtilelabel = getContentLabel("DEL", true)
            val retList: MutableList<Node?> = ArrayList()
            retList.add(typeTitilelabel)
            retList.add(keyTitilelabel)
            retList.add(valTtilelabel)
            retList.add(delTtilelabel)
            return retList
        }
    protected override val bodyAttributeNodeList: List<List<Node?>?>
        protected get() {
            val retList: MutableList<List<Node?>?> = ArrayList()
            val attrNameList: List<String?> = DynamoDbUtils.Companion.getSortedAttrNameList(dynamoDbRecordOrg)
            for (attrName in attrNameList) {
                retList.add(getOneBodyAttributeNodeList(attrName, dynamoDbRecordOrg.get(attrName)))
                getAttrNameList().add(attrName)
            }
            return retList
        }
    protected override val footerNodeList: List<Node?>
        protected get() {
            val retList: MutableList<Node?> = ArrayList()
            addAttrValueNode = getAtrributeBox(ADD_ATTR_NAME, selectedAddType.initValue)
            retList.add(typeComboBox)
            retList.add(addAttrNameTextField)
            retList.add(addAttrValueNode)
            retList.add(addButton)
            return retList
        }
    protected override val editedDynamoDbRecord: R
        protected get() {
            val retMap: MutableMap<String, AttributeValue?> = HashMap()
            val currentBodyNodeList = currentBodyNodeList
            for (wkNodeList in currentBodyNodeList!!) {
                val delCheck = wkNodeList!![delColIndex] as CheckBox
                if (delCheck.isSelected) {
                    continue
                }
                val valueNode = wkNodeList[Companion.valueColIndex]
                val keylabel = wkNodeList[COL_IDX_NAME] as Label
                val newAttr = getAttributeFromNode(valueNode)
                retMap[keylabel.text] = newAttr
            }
            return retMap
        }
    protected override val emptyAttr: R
        protected get() = HashMap<String, AttributeValue>()

    override fun actAddNewAttribute() {
        val attrName = addAttrNameTextField!!.text
        val attrVal = getAttributeFromNode(addAttrValueNode)
        if (!addValidationCheck(attrName, attrVal)) {
            return
        }
        updAttributeMap[attrName] = attrVal
        val nodelList = getOneBodyAttributeNodeList(attrName, attrVal)
        addAttributeNodeList(nodelList!!)
        getAttrNameList().add(attrName)
    }

    protected override val isFinalValidationOk: Boolean
        protected get() {
            val currentBodyNodeList = currentBodyNodeList
            for (wkNodeList in currentBodyNodeList!!) {
                val delCheck = wkNodeList!![delColIndex] as CheckBox
                if (delCheck.isSelected) {
                    continue
                }
                val keylabel = wkNodeList[COL_IDX_NAME] as Label
                val valueNode = wkNodeList[Companion.valueColIndex]
                if (!checkValueNode(keylabel.text, valueNode)) {
                    val alert = Alert(AlertType.ERROR, AbsDynamoDbInputDialog.Companion.VALIDATION_MSG_INVALID_VALUE)
                    alert.showAndWait()
                    valueNode!!.requestFocus()
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
    override fun getAttributeFromEditButtonId(btnId: String): AttributeValue? {
        val attrName: String = btnId.substring(AbsDynamoDbInputDialog.Companion.EDTBTN_ID_PREFIX.length)
        if (attrName == ADD_ATTR_NAME) {
            return tempAddAttrValue
        }
        return if (updAttributeMap.containsKey(attrName)) {
            updAttributeMap[attrName]
        } else {
            dynamoDbRecordOrg.get(attrName)
        }
    }

    public override fun getTitleAppendStr(btnId: String): String {
        return btnId.substring(AbsDynamoDbInputDialog.Companion.EDTBTN_ID_PREFIX.length)
    }

    override fun callBackSetNewAttribute(btnId: String, attrVal: AttributeValue) {
        val attrName: String = btnId.substring(AbsDynamoDbInputDialog.Companion.EDTBTN_ID_PREFIX.length)
        if (attrName == ADD_ATTR_NAME) {
            tempAddAttrValue = attrVal
        } else {
            val valLabelNode = dialogPane.lookup(String.format("#%s", AbsDynamoDbInputDialog.Companion.VALLBL_ID_PREFIX + attrName))
            if (valLabelNode != null && valLabelNode is Label) {
                valLabelNode.text = DynamoDbUtils.Companion.getAttrString(attrVal)
            }
        }
        updAttributeMap[attrName] = attrVal
    }

    protected override val addAttrEditButtonId: String
        protected get() = AbsDynamoDbInputDialog.Companion.EDTBTN_ID_PREFIX + ADD_ATTR_NAME

    override fun onAddTypeComboSelChanged(oldValue: String?, newValue: String?) {
        tempAddAttrValue = null
        updateFooter()
    }

    /*
	 * 
	 */
    protected open fun getOneBodyAttributeNodeList(attrName: String?, attrValue: AttributeValue?): List<Node?>? {
        val typelabel: Label = AbsDynamoDbInputDialog.Companion.getContentLabel(DynamoDbUtils.Companion.getAttrTypeString(attrValue))
        val keylabel: Label = AbsDynamoDbInputDialog.Companion.getContentLabel(attrName)
        val valueNode = getAtrributeBox(attrName, attrValue)
        val delCheck = CheckBox()
        delCheck.id = AbsDynamoDbInputDialog.Companion.DEL_ID_PREFIX + attrName
        val nodeList: MutableList<Node?> = ArrayList()
        nodeList.add(AbsDynamoDbInputDialog.Companion.addUnderlineStyleToNode(typelabel))
        nodeList.add(AbsDynamoDbInputDialog.Companion.addUnderlineStyleToNode(keylabel))
        nodeList.add(AbsDynamoDbInputDialog.Companion.addUnderlineStyleToNode(valueNode))
        nodeList.add(delCheck)
        return nodeList
    }

    protected fun getAtrributeBox(attrName: String?, attrVal: AttributeValue?): Node {
        val attrStr: String = DynamoDbUtils.Companion.getAttrString(attrVal)
        val textField = TextField(attrStr)
        if (attrVal!!.s() != null) {
            textField.id = AbsDynamoDbInputDialog.Companion.STRFLD_ID_PREFIX + attrName
        } else if (attrVal.n() != null) {
            textField.id = AbsDynamoDbInputDialog.Companion.NUMFLD_ID_PREFIX + attrName
        } else if (attrVal.b() != null) {
            textField.id = AbsDynamoDbInputDialog.Companion.BINFLD_ID_PREFIX + attrName
        } else return if (attrVal.bool() != null) {
            AbsDynamoDbInputDialog.Companion.getBooleanInput(attrVal)
        } else if (DynamoDbUtils.Companion.isNullAttr(attrVal)) {
            AbsDynamoDbInputDialog.Companion.getNullViewLabel()
        } else {
            getAttrEditButton(attrName, attrStr)
        }
        return textField
    }

    protected fun getAttrEditButton(attrName: String?, text: String?): HBox {
        val hbox = HBox(AbsDynamoDbInputDialog.Companion.HGAP)
        val vallabel: Label = AbsDynamoDbInputDialog.Companion.getContentLabel(text)
        vallabel.id = AbsDynamoDbInputDialog.Companion.VALLBL_ID_PREFIX + attrName
        val button = Button()
        button.text = AbsDynamoDbInputDialog.Companion.BTN_TITLE
        button.id = AbsDynamoDbInputDialog.Companion.EDTBTN_ID_PREFIX + attrName
        button.onAction = EventHandler { event: ActionEvent ->
            val wkBtn = event.source as Button
            actOpenEditDialog(wkBtn.id)
        }
        hbox.children.addAll(button, vallabel)
        return hbox
    }

    /*
	 * 
	 */
    protected fun addValidationCheck(attrName: String, attrVal: AttributeValue?): Boolean {
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

    /*
	 * accessor
	 */
    protected fun getAttrNameList(): MutableList<String?> {
        if (attrNameList == null) {
            attrNameList = ArrayList()
        }
        return attrNameList!!
    }

    companion object {
        private const val ADD_ATTR_NAME = ""
        private const val COL_IDX_NAME = 1
        protected val valueColIndex = 2
            protected get() = Companion.field
        protected val delColIndex = 3
            protected get() = Companion.field
        private const val VALIDATION_MSG_NO_ATTR_NAME = "Please input attribute name."
        private const val VALIDATION_MSG_DUP_ATTR_NAME = "Duplicated attribute name. please change the name."
    }
}