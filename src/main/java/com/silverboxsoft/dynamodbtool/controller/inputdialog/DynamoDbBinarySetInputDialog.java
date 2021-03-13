package com.silverboxsoft.dynamodbtool.controller.inputdialog;

import java.util.List;

import com.silverboxsoft.dynamodbtool.utils.DynamoDbUtils;

import javafx.scene.Node;
import javafx.scene.control.TextField;
import software.amazon.awssdk.core.SdkBytes;

public class DynamoDbBinarySetInputDialog extends AbsDynamoDbSetInputDialog<SdkBytes> {

	public DynamoDbBinarySetInputDialog(List<SdkBytes> dynamoDbRecord, String dialogTitle) {
		super(dynamoDbRecord, dialogTitle);
	}

	@Override
	protected String getTypeString() {
		return "BINARY";
	};

	@Override
	protected Node getAttrubuteBox(int recIndex, SdkBytes attr) {
		TextField textField = new TextField();
		textField.setText(DynamoDbUtils.getBase64StringFromSdkBytes(attr));
		return textField;
	}

	@Override
	protected SdkBytes getCurrentAttrubuteValue(Node valueNode) {
		TextField valField = (TextField) valueNode;
		return DynamoDbUtils.getSdkBytesFromBase64String(valField.getText());
	}

	@Override
	protected SdkBytes getInitAttribute() {
		return SdkBytes.fromByteArray(new byte[0]);
	}

	@Override
	protected boolean isSameValue(SdkBytes valA, SdkBytes valB) {
		return valA.equals(valB);
	}
}
