package com.silverboxsoft.dynamodbtool.controller.inputdialog

import com.silverboxsoft.dynamodbtool.utils.DynamoDbUtils
import software.amazon.awssdk.core.SdkBytes
import javafx.scene.Node
import javafx.scene.control.*

class DynamoDbBinarySetInputDialog(dynamoDbRecord: List<SdkBytes>?, dialogTitle: String?) : AbsDynamoDbSetInputDialog<SdkBytes>(dynamoDbRecord, dialogTitle) {
    protected override val typeString: String?
        protected get() = "BINARY"

    override fun getAttrubuteBox(recIndex: Int, attr: SdkBytes): Node? {
        val textField = TextField()
        textField.text = DynamoDbUtils.Companion.getBase64StringFromSdkBytes(attr)
        return textField
    }

    override fun getCurrentAttrubuteValue(valueNode: Node?): SdkBytes {
        val valField = valueNode as TextField?
        return DynamoDbUtils.Companion.getSdkBytesFromBase64String(valField!!.text)
    }

    protected override val initAttribute: T
        protected get() = SdkBytes.fromByteArray(ByteArray(0))

    override fun isSameValue(valA: SdkBytes, valB: SdkBytes): Boolean {
        return valA == valB
    }
}