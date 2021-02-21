package com.silverboxsoft.dynamodbtool.controller;

import java.util.List;

import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class DynamoDbNumberSetInputDialog extends AbsDynamoDbSetInputDialog<String> {

	public DynamoDbNumberSetInputDialog(List<String> dynamoDbRecord) {
		super(dynamoDbRecord);
	}

	protected String getTypeString() {
		return "NUMBER";
	};

	protected HBox getAttrubuteBox(int recIndex, String attr) {
		HBox hbox = new HBox();
		TextField textField = new TextField();
		textField.setMinWidth(FILELD_WIDTH);
		textField.setText(attr.toString());
		Control control = textField;
		hbox.getChildren().addAll(control);
		return hbox;
	};
}
