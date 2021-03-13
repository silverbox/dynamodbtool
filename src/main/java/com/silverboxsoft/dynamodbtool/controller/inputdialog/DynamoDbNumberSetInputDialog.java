package com.silverboxsoft.dynamodbtool.controller.inputdialog;

import java.math.BigDecimal;
import java.util.List;

import com.silverboxsoft.dynamodbtool.utils.DynamoDbUtils;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;

public class DynamoDbNumberSetInputDialog extends AbsDynamoDbSetInputDialog<BigDecimal> {

	protected static final String VALIDATION_MSG_NOT_NUMERIC_STR = "It's not numeric string.";

	public DynamoDbNumberSetInputDialog(List<BigDecimal> dynamoDbRecord, String dialogTitle) {
		super(dynamoDbRecord, dialogTitle);
	}

	@Override
	protected void actAddNewAttribute() {
		TextField addValNode = (TextField) getAddValueNode();
		if (!DynamoDbUtils.isNumericStr(addValNode.getText())) {
			Alert alert = new Alert(AlertType.ERROR, VALIDATION_MSG_NOT_NUMERIC_STR);
			alert.showAndWait();
			getAddValueNode().requestFocus();
			return;
		}
		super.actAddNewAttribute();
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
		if (!DynamoDbUtils.isNumericStr(valField.getText())) {
			return null;
		}
		return DynamoDbUtils.getBigDecimal(valField.getText());
	}

	@Override
	protected BigDecimal getInitAttribute() {
		return new BigDecimal(0);
	}

	@Override
	protected boolean isSameValue(BigDecimal valA, BigDecimal valB) {
		return valA.equals(valB);
	}
}
