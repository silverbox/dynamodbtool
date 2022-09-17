package com.silverboxsoft.dynamodbtool.controller.inputdialog

import com.silverboxsoft.dynamodbtool.utils.DynamoDbUtils
import javafx.scene.Node
import javafx.scene.control.*

class DynamoDbStringSetInputDialog(dynamoDbRecord: List<String>, dialogTitle: String) : AbsDynamoDbSetInputDialog<String>(dynamoDbRecord, dialogTitle) {
    override val typeString: String
        get() = "STRING"

    override fun getAttributeBox(recIndex: Int, attr: String): Node {
        val textField = TextField()
        textField.text = attr
        return textField
    }

    override fun getCurrentAttributeValue(valueNode: Node): String {
        val valField = valueNode as TextField?
        return valField!!.text
    }

    override val initialAttribute: String
        get() = ""

    override fun isSameValue(valA: String, valB: String): Boolean {
        return valA == valB
    }
}