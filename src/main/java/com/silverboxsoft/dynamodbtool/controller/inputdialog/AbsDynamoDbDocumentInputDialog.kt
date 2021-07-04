package com.silverboxsoft.dynamodbtool.controller.inputdialog

import javafx.scene.layout.HBox
import com.silverboxsoft.dynamodbtool.utils.DynamoDbUtils
import software.amazon.awssdk.core.SdkBytes
import java.util.stream.Collectors
import com.silverboxsoft.dynamodbtool.classes.DynamoDbColumnType
import java.math.BigDecimal
import javafx.scene.Node
import javafx.scene.control.*
import software.amazon.awssdk.services.dynamodb.model.*
import java.util.function.Function

abstract class AbsDynamoDbDocumentInputDialog<T>(dynamoDbRecord: T?, dialogTitle: String?) : AbsDynamoDbInputDialog<T>(dynamoDbRecord, dialogTitle) {
    private var typeComboBox: ComboBox<String?>? = null
    protected var tempAddAttrValue: AttributeValue? = null

    /*
	 * for open attribute edit dialog
	 */
    abstract fun getAttributeFromEditButtonId(btnId: String): AttributeValue?
    abstract fun getTitleAppendStr(btnId: String): String
    abstract fun callBackSetNewAttribute(btnId: String, attrVal: AttributeValue)

    /*
	 * for add attribute button
	 */
    abstract val addAttrEditButtonId: String
    abstract fun onAddTypeComboSelChanged(oldValue: String?, newValue: String?)

    /*
	 * 
	 */
    protected fun actOpenEditDialog(btnId: String) {
        val title = this.title + " > " + getTitleAppendStr(btnId)
        var attrVal = getAttributeFromEditButtonId(btnId)
        if (attrVal == null && btnId == addAttrEditButtonId) {
            attrVal = selectedAddType.getInitValue()
        }
        if (attrVal!!.hasSs()) {
            val dialog = DynamoDbStringSetInputDialog(attrVal.ss(), title)
            val newRec = dialog.showAndWait()
            if (newRec.isPresent) {
                val attrValue = AttributeValue.builder().ss(newRec.get()).build()
                callBackSetNewAttribute(btnId, attrValue)
            }
        } else if (attrVal.hasNs()) {
            val setList = attrVal.ns().stream()
                    .map(Function<String, BigDecimal> { strval: String? -> DynamoDbUtils.Companion.getBigDecimal(strval) })
                    .collect(Collectors.toList())
            val dialog = DynamoDbNumberSetInputDialog(setList, title)
            val newRec = dialog.showAndWait()
            if (newRec.isPresent) {
                val numStrList = newRec.get().stream()
                        .map(Function<BigDecimal?, String> { bd: BigDecimal? -> DynamoDbUtils.Companion.getNumStr(bd) })
                        .collect(Collectors.toList())
                val attrValue = AttributeValue.builder().ns(numStrList).build()
                callBackSetNewAttribute(btnId, attrValue)
            }
        } else if (attrVal.hasBs()) {
            val dialog = DynamoDbBinarySetInputDialog(attrVal.bs(), title)
            val newRec = dialog.showAndWait()
            if (newRec.isPresent) {
                val attrValue = AttributeValue.builder().bs(newRec.get()).build()
                callBackSetNewAttribute(btnId, attrValue)
            }
        } else if (attrVal.hasM()) {
            val dialog = DynamoDbMapInputDialog(attrVal.m(), title)
            val newRec = dialog.showAndWait()
            if (newRec.isPresent) {
                val attrValue = AttributeValue.builder().m(newRec.get()).build()
                callBackSetNewAttribute(btnId, attrValue)
            }
        } else if (attrVal.hasL()) {
            val dialog = DynamoDbListInputDialog(attrVal.l(), title)
            val newRec = dialog.showAndWait()
            if (newRec.isPresent) {
                val attrValue = AttributeValue.builder().l(newRec.get()).build()
                callBackSetNewAttribute(btnId, attrValue)
            }
        }
    }

    /*
	 * util method
	 */
    protected fun getAttributeFromNode(valueNode: Node?): AttributeValue? {
        if (valueNode is HBox) {
            val wkBox = valueNode
            val wkNode = wkBox.children[0]
            return if (wkNode is RadioButton) {
                AttributeValue.builder().bool(AbsDynamoDbInputDialog.Companion.getBooleanValue(wkBox)).build()
            } else {
                val button = wkNode as Button
                getAttributeFromEditButtonId(button.id)
            }
        } else if (valueNode is TextField) {
            val valTextField = valueNode
            val id = valTextField.id
            if (id.startsWith(AbsDynamoDbInputDialog.Companion.STRFLD_ID_PREFIX)) {
                return AttributeValue.builder().s(valTextField.text).build()
            } else if (id.startsWith(AbsDynamoDbInputDialog.Companion.NUMFLD_ID_PREFIX)) {
                return AttributeValue.builder().n(valTextField.text).build()
            } else if (id.startsWith(AbsDynamoDbInputDialog.Companion.BINFLD_ID_PREFIX)) {
                val sdkBytes: SdkBytes = DynamoDbUtils.Companion.getSdkBytesFromBase64String(valTextField.text)
                return AttributeValue.builder().b(sdkBytes).build()
            }
        } else if (valueNode is Label) {
            return AttributeValue.builder().nul(true).build()
        }
        return null
    }

    protected fun checkValueNode(valueNode: Node?): Boolean {
        if (valueNode is TextField) {
            val valTextField = valueNode
            val id = valTextField.id
            if (id.startsWith(AbsDynamoDbInputDialog.Companion.STRFLD_ID_PREFIX)) {
                return true
            } else if (id.startsWith(AbsDynamoDbInputDialog.Companion.NUMFLD_ID_PREFIX)) {
                return DynamoDbUtils.Companion.isNumericStr(valTextField.text)
            } else if (id.startsWith(AbsDynamoDbInputDialog.Companion.BINFLD_ID_PREFIX)) {
                return DynamoDbUtils.Companion.isBase64Str(valTextField.text)
            }
        }
        return true
    }

    /**
     * use for add button
     *
     * @return
     */
    protected val selectedAddType: DynamoDbColumnType?
        protected get() {
            val selStr = getTypeComboBox().value
            return DynamoDbColumnType.Companion.getColumnType(selStr)
        }

    protected fun getTypeComboBox(): ComboBox<String?> {
        if (typeComboBox == null) {
            typeComboBox = ComboBox()
            typeComboBox!!.isEditable = false
            typeComboBox!!.items.clear()
            for (type in DynamoDbColumnType.values()) {
                typeComboBox!!.items.add(type.dispStr)
            }
            typeComboBox!!.setValue(typeComboBox!!.items[0])
            typeComboBox!!.valueProperty().addListener { observable, oldValue, newValue -> onAddTypeComboSelChanged(oldValue, newValue) }
        }
        return typeComboBox
    }

    companion object {
        protected const val VALIDATION_MSG_NO_ATTR_VALUE = "Please set attribute value."
    }
}