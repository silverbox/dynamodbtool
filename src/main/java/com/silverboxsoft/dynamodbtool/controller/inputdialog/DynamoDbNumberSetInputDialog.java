package com.silverboxsoft.dynamodbtool.controller.inputdialog;

import java.math.BigDecimal;
import java.util.List;

import com.silverboxsoft.dynamodbtool.utils.DynamoDbUtils;

import javafx.scene.Node;
import javafx.scene.control.TextField;

public class DynamoDbNumberSetInputDialog extends AbsDynamoDbSetInputDialog<BigDecimal> {

	public DynamoDbNumberSetInputDialog(List<BigDecimal> dynamoDbRecord) {
		super(dynamoDbRecord);
	}

	@Override
	protected String getTypeString() {
		return "NUMBER";
	};

	@Override
	protected Node getAttrubuteBox(int recIndex, BigDecimal attr) {
		TextField textField = new TextField();
		textField.setText(DynamoDbUtils.getNumStr(attr));
		return textField;
	}

	@Override
	protected BigDecimal getCurrentAttrubuteValue(Node valueNode) {
		TextField valField = (TextField) valueNode;
		return DynamoDbUtils.getBigDecimal(valField.getText());
	}

	@Override
	BigDecimal getInitAttribute() {
		return new BigDecimal(0);
	}
}
