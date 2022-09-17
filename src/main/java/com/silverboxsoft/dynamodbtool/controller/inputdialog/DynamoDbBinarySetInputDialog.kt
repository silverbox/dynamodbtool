package com.silverboxsoft.dynamodbtool.controller.inputdialog

import com.silverboxsoft.dynamodbtool.utils.DynamoDbUtils
import software.amazon.awssdk.core.SdkBytes
import javafx.scene.Node
import javafx.scene.control.*

class DynamoDbBinarySetInputDialog(dynamoDbRecord: List<SdkBytes>, dialogTitle: String) : AbsDynamoDbSetInputDialog<SdkBytes>(dynamoDbRecord, dialogTitle) {
    override val typeString: String
        get() = "BINARY"

    override fun getAttributeBox(recIndex: Int, attr: SdkBytes): Node {
        val textField = TextField()
        textField.text = DynamoDbUtils.Companion.getBase64StringFromSdkBytes(attr)
        return textField
    }

    override fun getCurrentAttributeValue(valueNode: Node): SdkBytes {
        val valField = valueNode as TextField
        return DynamoDbUtils.Companion.getSdkBytesFromBase64String(valField.text)
    }

    override val initialAttribute: SdkBytes
        get() = SdkBytes.fromByteArray(ByteArray(0))

    override fun isSameValue(valA: SdkBytes, valB: SdkBytes): Boolean {
        return valA == valB
    }
}