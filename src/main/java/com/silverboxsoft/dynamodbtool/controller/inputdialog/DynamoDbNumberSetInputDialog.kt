package com.silverboxsoft.dynamodbtool.controller.inputdialog

import com.silverboxsoft.dynamodbtool.utils.DynamoDbUtils
import java.math.BigDecimal
import javafx.scene.control.Alert.AlertType
import javafx.scene.Node
import javafx.scene.control.*

class DynamoDbNumberSetInputDialog(dynamoDbRecord: List<BigDecimal>, dialogTitle: String) : AbsDynamoDbSetInputDialog<BigDecimal?>(dynamoDbRecord, dialogTitle) {
    override fun actAddNewAttribute() {
        val addValNode = addValueNode as TextField
        if (!DynamoDbUtils.Companion.isNumericStr(addValNode.text)) {
            val alert = Alert(AlertType.ERROR, VALIDATION_MSG_NOT_NUMERIC_STR)
            alert.showAndWait()
            addValueNode.requestFocus()
            return
        }
        super.actAddNewAttribute()
    }

    override val typeString: String
        get() = "NUMBER"

    override fun getAttributeBox(recIndex: Int, attr: BigDecimal?): Node {
        val textField = TextField()
        textField.text = DynamoDbUtils.Companion.getNumStr(attr)
        return textField
    }

    override fun getCurrentAttributeValue(valueNode: Node): BigDecimal? {
        val valField = valueNode as TextField?
        return if (!DynamoDbUtils.Companion.isNumericStr(valField!!.text)) {
            null
        } else DynamoDbUtils.Companion.getBigDecimal(valField.text)
    }

    override val initialAttribute: BigDecimal
        get() = BigDecimal(0)

    override fun isSameValue(valA: BigDecimal?, valB: BigDecimal?): Boolean {
        return valA == valB
    }

    companion object {
        const val VALIDATION_MSG_NOT_NUMERIC_STR = "It's not numeric string."
    }
}