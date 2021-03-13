package com.silverboxsoft.dynamodbtool.controller.inputdialog;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.silverboxsoft.dynamodbtool.classes.DynamoDbColumnType;
import com.silverboxsoft.dynamodbtool.utils.DynamoDbUtils;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public abstract class AbsDynamoDbDocumentInputDialog<T> extends AbsDynamoDbInputDialog<T> {

	protected static final String VALIDATION_MSG_NO_ATTR_VALUE = "Please set attribute value.";

	private ComboBox<String> typeComboBox;
	protected AttributeValue tempAddAttrValue = null;

	public AbsDynamoDbDocumentInputDialog(T dynamoDbRecord, String dialogTitle) {
		super(dynamoDbRecord, dialogTitle);
	}

	/*
	 * for open attribute edit dialog
	 */
	abstract AttributeValue getAttributeFromEditButtonId(String btnId);

	abstract String getTitleAppendStr(String btnId);

	abstract void callBackSetNewAttribute(String btnId, AttributeValue attrVal);

	/*
	 * for add attribute button
	 */
	abstract String getAddAttrEditButtonId();

	abstract void onAddTypeComboSelChanged(String oldValue, String newValue);

	/*
	 * 
	 */
	protected void actOpenEditDialog(String btnId) {
		String title = this.getTitle().concat(" > ").concat(getTitleAppendStr(btnId));
		AttributeValue attrVal = getAttributeFromEditButtonId(btnId);
		if (attrVal == null && btnId.equals(getAddAttrEditButtonId())) {
			attrVal = getSelectedAddType().getInitValue();
		}
		if (attrVal.hasSs()) {
			DynamoDbStringSetInputDialog dialog = new DynamoDbStringSetInputDialog(attrVal.ss(), title);
			Optional<List<String>> newRec = dialog.showAndWait();
			if (newRec.isPresent()) {
				AttributeValue attrValue = AttributeValue.builder().ss(newRec.get()).build();
				callBackSetNewAttribute(btnId, attrValue);
			}
		} else if (attrVal.hasNs()) {
			List<BigDecimal> setList = attrVal.ns().stream()
					.map(strval -> DynamoDbUtils.getBigDecimal(strval))
					.collect(Collectors.toList());
			DynamoDbNumberSetInputDialog dialog = new DynamoDbNumberSetInputDialog(setList, title);
			Optional<List<BigDecimal>> newRec = dialog.showAndWait();
			if (newRec.isPresent()) {
				List<String> numStrList = newRec.get().stream()
						.map(bd -> DynamoDbUtils.getNumStr(bd))
						.collect(Collectors.toList());
				AttributeValue attrValue = AttributeValue.builder().ns(numStrList).build();
				callBackSetNewAttribute(btnId, attrValue);
			}
		} else if (attrVal.hasBs()) {
			DynamoDbBinarySetInputDialog dialog = new DynamoDbBinarySetInputDialog(attrVal.bs(), title);
			Optional<List<SdkBytes>> newRec = dialog.showAndWait();
			if (newRec.isPresent()) {
				AttributeValue attrValue = AttributeValue.builder().bs(newRec.get()).build();
				callBackSetNewAttribute(btnId, attrValue);
			}
		} else if (attrVal.hasM()) {
			DynamoDbMapInputDialog dialog = new DynamoDbMapInputDialog(attrVal.m(), title);
			Optional<Map<String, AttributeValue>> newRec = dialog.showAndWait();
			if (newRec.isPresent()) {
				AttributeValue attrValue = AttributeValue.builder().m(newRec.get()).build();
				callBackSetNewAttribute(btnId, attrValue);
			}
		} else if (attrVal.hasL()) {
			DynamoDbListInputDialog dialog = new DynamoDbListInputDialog(attrVal.l(), title);
			Optional<List<AttributeValue>> newRec = dialog.showAndWait();
			if (newRec.isPresent()) {
				AttributeValue attrValue = AttributeValue.builder().l(newRec.get()).build();
				callBackSetNewAttribute(btnId, attrValue);
			}
		}
	}

	/*
	 * util method
	 */
	protected AttributeValue getAttributeFromNode(Node valueNode) {
		if (valueNode instanceof HBox) {
			HBox wkBox = (HBox) valueNode;
			Node wkNode = wkBox.getChildren().get(0);
			if (wkNode instanceof RadioButton) {
				return AttributeValue.builder().bool(getBooleanValue(wkBox)).build();
			} else {
				Button button = (Button) wkNode;
				return getAttributeFromEditButtonId(button.getId());
			}
		} else if (valueNode instanceof TextField) {
			TextField valTextField = (TextField) valueNode;
			String id = valTextField.getId();
			if (id.startsWith(STRFLD_ID_PREFIX)) {
				return AttributeValue.builder().s(valTextField.getText()).build();
			} else if (id.startsWith(NUMFLD_ID_PREFIX)) {
				return AttributeValue.builder().n(valTextField.getText()).build();
			} else if (id.startsWith(BINFLD_ID_PREFIX)) {
				SdkBytes sdkBytes = DynamoDbUtils.getSdkBytesFromBase64String(valTextField.getText());
				return AttributeValue.builder().b(sdkBytes).build();
			}
		} else if (valueNode instanceof Label) {
			return AttributeValue.builder().nul(true).build();
		}
		return null;
	}

	protected boolean checkValueNode(Node valueNode) {
		if (valueNode instanceof TextField) {
			TextField valTextField = (TextField) valueNode;
			String id = valTextField.getId();
			if (id.startsWith(STRFLD_ID_PREFIX)) {
				return true;
			} else if (id.startsWith(NUMFLD_ID_PREFIX)) {
				return DynamoDbUtils.isNumericStr(valTextField.getText());
			} else if (id.startsWith(BINFLD_ID_PREFIX)) {
				return DynamoDbUtils.isBase64Str(valTextField.getText());
			}
		}
		return true;
	}

	/**
	 * use for add button
	 * 
	 * @return
	 */
	protected DynamoDbColumnType getSelectedAddType() {
		String selStr = getTypeComboBox().getValue();
		return DynamoDbColumnType.getColumnType(selStr);
	}

	protected ComboBox<String> getTypeComboBox() {
		if (typeComboBox == null) {
			typeComboBox = new ComboBox<>();
			typeComboBox.setEditable(false);
			typeComboBox.getItems().clear();
			for (DynamoDbColumnType type : DynamoDbColumnType.values()) {
				typeComboBox.getItems().add(type.getDispStr());
			}
			typeComboBox.setValue(typeComboBox.getItems().get(0));
			typeComboBox.valueProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					onAddTypeComboSelChanged(oldValue, newValue);
				}
			});
		}
		return typeComboBox;
	}

}
