package com.silverboxsoft.dynamodbtool.controller.inputdialog;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.silverboxsoft.dynamodbtool.classes.DynamoDbColumnType;
import com.silverboxsoft.dynamodbtool.classes.DynamoDbColumnTypeCategory;
import com.silverboxsoft.dynamodbtool.utils.DynamoDbUtils;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public abstract class AbsDynamoDbDocumentInputDialog<T> extends AbsDynamoDbInputDialog<T> {

	private ComboBox<String> typeComboBox;

	public AbsDynamoDbDocumentInputDialog(T dynamoDbRecord) {
		super(dynamoDbRecord);
	}

	abstract AttributeValue getCurrentAttribute(String btnId);

	abstract void callBackSetNewAttribute(String btnId, AttributeValue attrVal);

	abstract void onAddTypeComboSelChanged(String oldValue, String newValue);

	@Override
	protected void initialize() {
		super.initialize();
		Button addButton = super.getAddButton();
		addButton.setOnAction((event) -> {
			Button wkBtn = (Button) event.getSource();
			DynamoDbColumnTypeCategory category = getSelectedAddType().getCategory();
			if (category == DynamoDbColumnTypeCategory.DOCUMENT || category == DynamoDbColumnTypeCategory.SET) {
				actOpenEditDialog(wkBtn.getId());
			} else {
				actAddScalarAttribute();
			}
		});
	}

	protected void actOpenEditDialog(String btnId) {
		AttributeValue attrVal = getCurrentAttribute(btnId);
		if (attrVal.hasSs()) {
			DynamoDbStringSetInputDialog dialog = new DynamoDbStringSetInputDialog(attrVal.ss());
			Optional<List<String>> newRec = dialog.showAndWait();
			if (newRec.isPresent()) {
				AttributeValue attrValue = AttributeValue.builder().ss(newRec.get()).build();
				callBackSetNewAttribute(btnId, attrValue);
			}
		} else if (attrVal.hasNs()) {
			List<BigDecimal> setList = attrVal.ns().stream()
					.map(strval -> DynamoDbUtils.getBigDecimal(strval))
					.collect(Collectors.toList());
			DynamoDbNumberSetInputDialog dialog = new DynamoDbNumberSetInputDialog(setList);
			Optional<List<BigDecimal>> newRec = dialog.showAndWait();
			if (newRec.isPresent()) {
				List<String> numStrList = newRec.get().stream()
						.map(bd -> DynamoDbUtils.getNumStr(bd))
						.collect(Collectors.toList());
				AttributeValue attrValue = AttributeValue.builder().ns(numStrList).build();
				callBackSetNewAttribute(btnId, attrValue);
			}
		} else if (attrVal.hasBs()) {
			DynamoDbBinarySetInputDialog dialog = new DynamoDbBinarySetInputDialog(attrVal.bs());
			Optional<List<SdkBytes>> newRec = dialog.showAndWait();
			if (newRec.isPresent()) {
				AttributeValue attrValue = AttributeValue.builder().bs(newRec.get()).build();
				callBackSetNewAttribute(btnId, attrValue);
			}
		} else if (attrVal.hasM()) {
			DynamoDbMapInputDialog dialog = new DynamoDbMapInputDialog(attrVal.m());
			Optional<Map<String, AttributeValue>> newRec = dialog.showAndWait();
			if (newRec.isPresent()) {
				AttributeValue attrValue = AttributeValue.builder().m(newRec.get()).build();
				callBackSetNewAttribute(btnId, attrValue);
			}
		} else if (attrVal.hasL()) {
			DynamoDbListInputDialog dialog = new DynamoDbListInputDialog(attrVal.l());
			Optional<List<AttributeValue>> newRec = dialog.showAndWait();
			if (newRec.isPresent()) {
				AttributeValue attrValue = AttributeValue.builder().l(newRec.get()).build();
				callBackSetNewAttribute(btnId, attrValue);
			}
		}
	}

	/**
	 * use for add button
	 * 
	 * @return
	 */
	protected DynamoDbColumnType getSelectedAddType() {
		String selStr = getTypeComboBox().getValue();// .getSelectionModel().getSelectedItem();
		return DynamoDbColumnType.getColumnType(selStr);
	}

	protected AttributeValue getInitAttributeValue() {
		return getSelectedAddType().getInitValue();
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
