package com.silverboxsoft.dynamodbtool.controller.inputdialog;

import java.math.BigDecimal;
import java.util.List;

import com.silverboxsoft.dynamodbtool.utils.DynamoDbUtils;

import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class DynamoDbNumberSetInputDialog extends AbsDynamoDbSetInputDialog<BigDecimal> {

	public DynamoDbNumberSetInputDialog(List<BigDecimal> dynamoDbRecord) {
		super(dynamoDbRecord);
	}

	@Override
	protected String getTypeString() {
		return "NUMBER";
	};

	@Override
	protected HBox getAttrubuteBox(int recIndex, BigDecimal attr) {
		HBox hbox = new HBox();
		TextField textField = new TextField();
		textField.setMinWidth(FILELD_WIDTH);
		textField.setText(DynamoDbUtils.getNumStr(attr));
		Control control = textField;
		hbox.getChildren().addAll(control);
		return hbox;
	}

	@Override
	protected BigDecimal getCurrentAttrubuteValue(HBox valuebox) {
		TextField valField = (TextField) valuebox.getChildren().get(0);
		return DynamoDbUtils.getBigDecimal(valField.getText());
	}
}
