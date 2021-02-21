package com.silverboxsoft.dynamodbtool.controller;

import java.util.List;

import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class DynamoDbStringSetInputDialog extends AbsDynamoDbSetInputDialog<String> {

	public DynamoDbStringSetInputDialog(List<String> dynamoDbRecord) {
		super(dynamoDbRecord);
	}

	protected String getTypeString() {
		return "STRING";
	};

	protected HBox getAttrubuteBox(int recIndex, String attr) {
		HBox hbox = new HBox();
		TextField textField = new TextField();
		textField.setMinWidth(FILELD_WIDTH);
		textField.setText(attr);
		Control control = textField;
		hbox.getChildren().addAll(control);
		return hbox;
	};
}
