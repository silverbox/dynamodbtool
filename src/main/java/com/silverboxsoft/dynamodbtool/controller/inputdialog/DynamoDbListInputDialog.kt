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

class DynamoDbListInputDialog(dynamoDbRecord: List<AttributeValue?>?, dialogTitle: String?) : AbsDynamoDbDocumentInputDialog<List<AttributeValue?>?>(dynamoDbRecord, dialogTitle) {
    private var addAttrValueNode: Node? = null
    private val updAttributeMap: MutableMap<String, AttributeValue> = HashMap()
    private var addAttributeMap: MutableMap<String, AttributeValue?>? = HashMap()
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
            val indexTitilelabel = getContentLabel("INDEX", true)
            val typeTitilelabel = getContentLabel("TYPE", true)
            val valTtilelabel = getContentLabel("VALUE", true)
            val delTtilelabel = getContentLabel("DEL", true)
            val retList: MutableList<Node?> = ArrayList()
            retList.add(indexTitilelabel)
            retList.add(typeTitilelabel)
            retList.add(valTtilelabel)
            retList.add(delTtilelabel)
            return retList
        }
    protected override val bodyAttributeNodeList: List<List<Node?>?>
        protected get() {
            val retList: MutableList<List<Node?>?> = ArrayList()
            for (recIdx in dynamoDbRecordOrg.indices) {
                val attrVal: AttributeValue = dynamoDbRecordOrg.get(recIdx)
                retList.add(getOneBodyAttributeNodeList(recIdx, attrVal))
            }
            return retList
        }
    protected override val footerNodeList: List<Node?>
        protected get() {
            val retList: MutableList<Node?> = ArrayList()
            addAttrValueNode = getAtrributeBox(ADD_INDEX, selectedAddType.initValue)
            retList.add(Label("new Index"))
            retList.add(typeComboBox)
            retList.add(addAttrValueNode)
            retList.add(addButton)
            return retList
        }
    protected override val valueColIndex: Int
        protected get() = 2
    protected override val editedDynamoDbRecord: R
        protected get() {
            val retList: MutableList<AttributeValue?> = ArrayList()
            val currentBodyNodeList = currentBodyNodeList
            for (wkNodeList in currentBodyNodeList!!) {
                val delCheck = wkNodeList!![COL_IDX_DEL] as CheckBox
                if (delCheck.isSelected) {
                    continue
                }
                val valueNode = wkNodeList[COL_IDX_FIELD] as Node
                val newAttr = getAttributeFromNode(valueNode)
                retList.add(newAttr)
            }
            return retList
        }
    protected override val emptyAttr: R
        protected get() = ArrayList<AttributeValue>()

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

    protected override val isFinalValidationOk: Boolean
        protected get() {
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
                    valueNode!!.requestFocus()
                    return false
                }
            }
            return true
        }

    /*
	 * 
	 */
    override fun getAttributeFromEditButtonId(btnId: String): AttributeValue? {
        val recIndexStr: String = btnId.substring(AbsDynamoDbInputDialog.Companion.EDTBTN_ID_PREFIX.length)
        if (recIndexStr == ADD_INDEX.toString()) {
            return tempAddAttrValue
        }
        return if (updAttributeMap.containsKey(recIndexStr)) {
            updAttributeMap[recIndexStr]
        } else if (addAttributeMap!!.containsKey(recIndexStr)) {
            addAttributeMap!![recIndexStr]
        } else {
            dynamoDbRecordOrg.get(Integer.valueOf(recIndexStr))
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

    protected override val addAttrEditButtonId: String
        protected get() = AbsDynamoDbInputDialog.Companion.EDTBTN_ID_PREFIX + ADD_INDEX.toString()

    override fun onAddTypeComboSelChanged(oldValue: String?, newValue: String?) {
        tempAddAttrValue = null
        updateFooter()
    }

    /*
	 * 
	 */
    private fun getOneBodyAttributeNodeList(recIdx: Int, attrVal: AttributeValue?): List<Node?> {
        val indexLabel: Label = AbsDynamoDbInputDialog.Companion.getContentLabel(recIdx.toString())
        val typeLabel: Label = AbsDynamoDbInputDialog.Companion.getContentLabel(DynamoDbUtils.Companion.getAttrTypeString(attrVal))
        val valueBox = getAtrributeBox(recIdx, attrVal)
        val delCheck = CheckBox()
        delCheck.id = AbsDynamoDbInputDialog.Companion.DEL_ID_PREFIX + recIdx.toString()
        val nodeList: MutableList<Node?> = ArrayList()
        nodeList.add(AbsDynamoDbInputDialog.Companion.addUnderlineStyleToNode(indexLabel))
        nodeList.add(AbsDynamoDbInputDialog.Companion.addUnderlineStyleToNode(typeLabel))
        nodeList.add(AbsDynamoDbInputDialog.Companion.addUnderlineStyleToNode(valueBox))
        nodeList.add(delCheck)
        return nodeList
    }

    protected fun getAtrributeBox(recIndex: Int, attrVal: AttributeValue?): Node {
        val attrStr: String = DynamoDbUtils.Companion.getAttrString(attrVal)
        val textField = TextField(attrStr)
        if (attrVal!!.s() != null) {
            textField.id = AbsDynamoDbInputDialog.Companion.STRFLD_ID_PREFIX + recIndex.toString()
        } else if (attrVal.n() != null) {
            textField.id = AbsDynamoDbInputDialog.Companion.NUMFLD_ID_PREFIX + recIndex.toString()
        } else if (attrVal.b() != null) {
            textField.id = AbsDynamoDbInputDialog.Companion.BINFLD_ID_PREFIX + recIndex.toString()
        } else return if (attrVal.bool() != null) {
            AbsDynamoDbInputDialog.Companion.getBooleanInput(attrVal)
        } else if (DynamoDbUtils.Companion.isNullAttr(attrVal)) {
            AbsDynamoDbInputDialog.Companion.getNullViewLabel()
        } else {
            getAttrEditButton(recIndex, attrStr)
        }
        return textField
    }

    protected fun getAttrEditButton(recIndex: Int, text: String?): Node {
        val idStr = recIndex.toString()
        val hbox = HBox(AbsDynamoDbInputDialog.Companion.HGAP)
        val vallabel: Label = AbsDynamoDbInputDialog.Companion.getContentLabel(text)
        vallabel.id = AbsDynamoDbInputDialog.Companion.VALLBL_ID_PREFIX + idStr
        val button = Button()
        button.text = AbsDynamoDbInputDialog.Companion.BTN_TITLE
        button.id = AbsDynamoDbInputDialog.Companion.EDTBTN_ID_PREFIX + idStr
        button.onAction = EventHandler { event: ActionEvent ->
            val wkBtn = event.source as Button
            actOpenEditDialog(wkBtn.id)
        }
        hbox.children.addAll(button, vallabel)
        return hbox
    }

    protected fun getAddAttributeMap(): MutableMap<String, AttributeValue?> {
        if (addAttributeMap == null) {
            addAttributeMap = HashMap()
        }
        return addAttributeMap!!
    }

    protected val newRecIdx: Int
        protected get() = dynamoDbRecordOrg.size + getAddAttributeMap().size

    protected fun addValidationCheck(attrVal: AttributeValue?): Boolean {
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