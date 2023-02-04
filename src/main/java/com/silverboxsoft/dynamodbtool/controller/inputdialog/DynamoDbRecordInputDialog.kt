package com.silverboxsoft.dynamodbtool.controller.inputdialog

import com.silverboxsoft.dynamodbtool.utils.DynamoDbUtils
import java.util.stream.Collectors
import com.silverboxsoft.dynamodbtool.classes.DynamoDbColumn
import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.control.Alert.AlertType
import java.util.HashSet
import com.silverboxsoft.dynamodbtool.classes.DynamoDbEditMode
import javafx.scene.Node
import javafx.scene.control.*
import software.amazon.awssdk.services.dynamodb.model.*
import software.amazon.awssdk.utils.StringUtils
import java.util.ArrayList

class DynamoDbRecordInputDialog(tableInfo: TableDescription, dynamoDbRecord: Map<String, AttributeValue>,
                                private val editMode: DynamoDbEditMode
) : DynamoDbMapInputDialog(dynamoDbRecord, "") {
    private var tblStructColumnList: List<DynamoDbColumn> = ArrayList()
    private val keyColumnSet: MutableSet<String> = HashSet()
    private val requireColumnSet: MutableSet<String> = HashSet()
    // add key info first

    // pick up the data which attribute name is come yet.
    // Set<String> unsetKeyNameSet = getDynamoDbRecordOrg().keySet();
    override val bodyAttributeNodeList: List<List<Node>>
        get() {
            val retList: MutableList<List<Node>> = ArrayList()
            if (tblStructColumnList == null) {
                return retList
            }
            if (attrNameList == null) {
                attrNameList = ArrayList()
            }

            // add key info first
            for (colIdx in tblStructColumnList.indices) {
                val attrName = tblStructColumnList[colIdx].columnName
                val wkAttr = dynamoDbRecordOrg.getOrDefault(attrName, AbsDynamoDbInputDialog.NULL_ATTRIBUTE)
                retList.add(getOneBodyAttributeNodeList(attrName, wkAttr))
                attrNameList.add(attrName)
            }

            // pick up the data which attribute name is come yet.
            // Set<String> unsetKeyNameSet = getDynamoDbRecordOrg().keySet();
            val allAttrNameList: List<String> = DynamoDbUtils.Companion.getSortedAttrNameList(dynamoDbRecordOrg)
            val keyNameSet = attrNameList.stream().collect(Collectors.toSet())
            for (newAttrName in allAttrNameList) {
                if (keyNameSet.contains(newAttrName)) {
                    continue
                }
                val wkAttr = dynamoDbRecordOrg.getOrDefault(newAttrName, AbsDynamoDbInputDialog.NULL_ATTRIBUTE)
                retList.add(getOneBodyAttributeNodeList(newAttrName, wkAttr))
                attrNameList.add(newAttrName)
            }
            return retList
        }

    override fun getOneBodyAttributeNodeList(attrName: String, attrValue: AttributeValue): List<Node> {
        val retList = super.getOneBodyAttributeNodeList(attrName, attrValue)
        if (keyColumnSet.contains(attrName)) {
            val valNode = retList[valueColIndex]
            if (valNode is TextField && editMode == DynamoDbEditMode.UPD) {
                valNode.style = "-fx-background-color: lightgray;"
                valNode.isEditable = false
            }
            val delBox = retList[delColIndex] as CheckBox
            delBox.isDisable = true
        }
        return retList
    }

    override fun doFinalConfirmation(dialogEvent: DialogEvent) {
        super.doFinalConfirmation(dialogEvent)
        if (!dialogEvent.isConsumed && buttonData == ButtonData.OK_DONE) {
            val dialog = Dialog<ButtonType>()
            dialog.contentText = "This DynamoDB record will be update. Is it OK?"
            dialog.dialogPane.buttonTypes.addAll(ButtonType.YES, ButtonType.NO)
            dialog.showAndWait().ifPresent { buttonType: ButtonType ->
                if (buttonType != ButtonType.YES) {
                    dialogEvent.consume()
                }
            }
        }
    }

    override fun checkAndGetErrorMessage(attrName: String?, valueNode: Node?): String? {
        if (requireColumnSet.contains(attrName) && valueNode is TextField) {
            if (StringUtils.isEmpty(valueNode.text)) {
                return VALIDATION_MSG_EMPTY_VALUE
            }
        }
        return super.checkAndGetErrorMessage(attrName, valueNode)
    }

    companion object {
        const val VALIDATION_MSG_EMPTY_VALUE = "Attribute for key (or index) value should not be null."
    }

    init {
        this.title = DynamoDbUtils.Companion.getKeyValueStr(tableInfo, dynamoDbRecord)
        tblStructColumnList = DynamoDbUtils.Companion.getSortedDynamoDbColumnList(tableInfo)
        val keyInfoList = tableInfo.keySchema()
        for (elem in keyInfoList) {
            keyColumnSet.add(elem.attributeName())
            requireColumnSet.add(elem.attributeName())
        }
        tableInfo.globalSecondaryIndexes().stream().forEach {
            gsiDesc: GlobalSecondaryIndexDescription -> gsiDesc.keySchema().stream().forEach {
                elem: KeySchemaElement -> requireColumnSet.add(elem.attributeName())
            }
        }
        tableInfo.localSecondaryIndexes().stream().forEach {
            lsiDesc: LocalSecondaryIndexDescription -> lsiDesc.keySchema().stream().forEach {
                elem: KeySchemaElement -> requireColumnSet.add(elem.attributeName())
            }
        }
        // initialize() // TODO work around
    }
}