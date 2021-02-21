package com.silverboxsoft.dynamodbtool.controller;

import java.util.List;

import com.silverboxsoft.dynamodbtool.utils.DynamoDbUtils;

import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import software.amazon.awssdk.core.SdkBytes;

public class DynamoDbBinarySetInputDialog extends AbsDynamoDbSetInputDialog<SdkBytes> {

	public DynamoDbBinarySetInputDialog(List<SdkBytes> dynamoDbRecord) {
		super(dynamoDbRecord);
	}

	protected String getTypeString() {
		return "BINARY";
	};

	protected HBox getAttrubuteBox(int recIndex, SdkBytes attr) {
		HBox hbox = new HBox();
		TextField textField = new TextField();
		textField.setMinWidth(FILELD_WIDTH);
		textField.setText(DynamoDbUtils.getBase64StringFromSdkBytes(attr));
		Control control = textField;
		hbox.getChildren().addAll(control);
		return hbox;
	};
}
