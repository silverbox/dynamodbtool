package com.silverboxsoft.dynamodbtool.controller.inputdialog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.silverboxsoft.dynamodbtool.classes.DynamoDbColumn;
import com.silverboxsoft.dynamodbtool.controller.DynamoDbEditMode;
import com.silverboxsoft.dynamodbtool.utils.DynamoDbUtils;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.TextField;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;
import software.amazon.awssdk.utils.StringUtils;

public class DynamoDbRecordInputDialog extends DynamoDbMapInputDialog {
	protected static final String VALIDATION_MSG_EMPTY_VALUE = "Key value should be non null.";

	private List<DynamoDbColumn> tblStructColumnList = new ArrayList<>();
	private Set<String> keyColumnSet;
	private final DynamoDbEditMode editMode;

	public DynamoDbRecordInputDialog(TableDescription tableInfo, Map<String, AttributeValue> dynamoDbRecord,
			DynamoDbEditMode editMode) {
		super(dynamoDbRecord, "");
		this.editMode = editMode;
		this.setTitle(DynamoDbUtils.getKeyValueStr(tableInfo, dynamoDbRecord));
		tblStructColumnList = DynamoDbUtils.getSortedDynamoDbColumnList(tableInfo);
		List<KeySchemaElement> keyInfos = tableInfo.keySchema();
		keyInfos.stream().forEach(elem -> getKeyColumnSet().add(elem.attributeName()));
		this.initialize(); // TODO work around
	}

	@Override
	protected List<List<Node>> getBodyAttributeNodeList() {
		List<List<Node>> retList = new ArrayList<>();
		if (tblStructColumnList == null) {
			return retList;
		}
		if (attrNameList == null) {
			attrNameList = new ArrayList<>();
		}

		// add key info first
		for (int colIdx = 0; colIdx < tblStructColumnList.size(); colIdx++) {
			String attrName = tblStructColumnList.get(colIdx).getColumnName();
			retList.add(getOneBodyAttributeNodeList(attrName, getDynamoDbRecordOrg().get(attrName)));
			attrNameList.add(attrName);
		}

		// pick up the data which attribute name is come yet.
		// Set<String> unsetKeyNameSet = getDynamoDbRecordOrg().keySet();
		List<String> allAttrNameList = DynamoDbUtils.getSortedAttrNameList(getDynamoDbRecordOrg());
		Set<String> keyNameSet = attrNameList.stream().collect(Collectors.toSet());
		for (String newAttrName : allAttrNameList) {
			if (keyNameSet.contains(newAttrName)) {
				continue;
			}
			retList.add(getOneBodyAttributeNodeList(newAttrName, getDynamoDbRecordOrg().get(newAttrName)));
			attrNameList.add(newAttrName);
		}
		return retList;
	}

	@Override
	protected List<Node> getOneBodyAttributeNodeList(String attrName, AttributeValue attrValue) {
		List<Node> retList = super.getOneBodyAttributeNodeList(attrName, attrValue);
		if (getKeyColumnSet().contains(attrName)) {
			Node valNode = retList.get(getValueColIndex());
			if (valNode instanceof TextField && editMode == DynamoDbEditMode.UPD) {
				TextField textField = (TextField) valNode;
				textField.setStyle("-fx-background-color: lightgray;");
				textField.setEditable(false);
			}
			CheckBox delbox = (CheckBox) retList.get(getDelColIndex());
			delbox.setDisable(true);
		}
		return retList;
	}

	@Override
	protected void doFinalConfirmation(DialogEvent dialogEvent) {
		super.doFinalConfirmation(dialogEvent);
		if (!dialogEvent.isConsumed() && getButtonData() == ButtonData.OK_DONE) {
			Dialog<ButtonType> dialog = new Dialog<>();
			dialog.setContentText("This DynamoDB record will be update. Is it OK?");
			dialog.getDialogPane().getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
			dialog.showAndWait().ifPresent(buttonType -> {
				if (buttonType != ButtonType.YES) {
					dialogEvent.consume();
				}
			});
		}
	}

	@Override
	protected boolean checkValueNode(String attrName, Node valueNode) {
		if (!super.checkValueNode(valueNode)) {
			return false;
		}
		if (getKeyColumnSet().contains(attrName) && valueNode instanceof TextField) {
			TextField valTextField = (TextField) valueNode;
			if (StringUtils.isEmpty(valTextField.getText())) {
				Alert alert = new Alert(AlertType.ERROR, VALIDATION_MSG_EMPTY_VALUE);
				alert.showAndWait();
				return false;
			}
		}
		return true;
	}

	private Set<String> getKeyColumnSet() {
		if (keyColumnSet == null) {
			keyColumnSet = new HashSet<>();
		}
		return keyColumnSet;
	}

}
