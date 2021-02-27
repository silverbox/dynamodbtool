package com.silverboxsoft.dynamodbtool.controller.inputdialog;

import java.util.List;

import javafx.scene.Node;
import javafx.scene.control.TextField;

public class DynamoDbStringSetInputDialog extends AbsDynamoDbSetInputDialog<String> {

	public DynamoDbStringSetInputDialog(List<String> dynamoDbRecord) {
		super(dynamoDbRecord);
	}

	@Override
	protected String getTypeString() {
		return "STRING";
	};

	@Override
	protected Node getAttrubuteBox(int recIndex, String attr) {
		TextField textField = new TextField();
		textField.setText(attr);
		return textField;
	}

	@Override
	protected String getCurrentAttrubuteValue(Node valueNode) {
		TextField valField = (TextField) valueNode;
		return valField.getText();
	};
}
