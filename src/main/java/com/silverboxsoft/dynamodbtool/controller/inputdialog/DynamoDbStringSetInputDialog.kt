package com.silverboxsoft.dynamodbtool.controller.inputdialog

import javafx.scene.Node
import javafx.scene.control.*

class DynamoDbStringSetInputDialog(dynamoDbRecord: List<String>, dialogTitle: String) : AbsDynamoDbSetInputDialog<String>(dynamoDbRecord, dialogTitle) {
    override val typeString: String
        get() = "STRING"

    override fun getAttrubuteBox(recIndex: Int, attr: String): Node {
        val textField = TextField()
        textField.text = attr
        return textField
    }

    override fun getCurrentAttrubuteValue(valueNode: Node): String {
        val valField = valueNode as TextField?
        return valField!!.text
    }

    override val initAttribute: String
        get() = ""

    override fun isSameValue(valA: String, valB: String): Boolean {
        return valA == valB
    }
}