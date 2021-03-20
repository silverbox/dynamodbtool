package com.silverboxsoft.dynamodbtool.controller.inputdialog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.silverboxsoft.dynamodbtool.classes.DynamoDbColumn;
import com.silverboxsoft.dynamodbtool.utils.DynamoDbUtils;

import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;

public class DynamoDbRecordInputDialog extends DynamoDbMapInputDialog {
	private List<DynamoDbColumn> tblStructColumnList = new ArrayList<>();
	private Set<String> keyColumnSet;

	public DynamoDbRecordInputDialog(TableDescription tableInfo, Map<String, AttributeValue> dynamoDbRecord) {
		super(dynamoDbRecord, "");
		this.setTitle(DynamoDbUtils.getKeyValueStr(tableInfo, dynamoDbRecord));
		tblStructColumnList = DynamoDbUtils.getSortedSchemeAttrNameList(tableInfo);
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

	protected List<Node> getOneBodyAttributeNodeList(String attrName, AttributeValue attrValue) {
		List<Node> retList = super.getOneBodyAttributeNodeList(attrName, attrValue);
		if (getKeyColumnSet().contains(attrName)) {
			Node valNode = retList.get(2);
			if (valNode instanceof TextField) {
				TextField textField = (TextField) valNode;
				textField.setStyle("-fx-background-color: lightgray;");
				textField.setEditable(false);
			}
			CheckBox delbox = (CheckBox) retList.get(3);
			delbox.setDisable(true);
		}
		return retList;
	}

	@Override
	boolean isFinalValidationOk() {
		if (super.isFinalValidationOk()) {
			Dialog<ButtonType> dialog = new Dialog<>();
			dialog.setContentText("This DynamoDB record will be update. Is it OK?");
			dialog.getDialogPane().getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
			final DialogResult dialogResult = new DialogResult();
			dialog.showAndWait().ifPresent(buttonType -> {
				if (buttonType == ButtonType.YES) {
					dialogResult.result = true;
				}
			});
			return dialogResult.result;
		}
		return false;
	}

	private Set<String> getKeyColumnSet() {
		if (keyColumnSet == null) {
			keyColumnSet = new HashSet<>();
		}
		return keyColumnSet;
	}

	private class DialogResult {
		boolean result = false;
	}
}
