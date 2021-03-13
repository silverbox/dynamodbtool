package com.silverboxsoft.dynamodbtool.controller.inputdialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.silverboxsoft.dynamodbtool.utils.DynamoDbUtils;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.utils.StringUtils;

public class DynamoDbMapInputDialog extends AbsDynamoDbDocumentInputDialog<Map<String, AttributeValue>> {

	private static final String ADD_ATTR_NAME = "";

	private static final int COL_IDX_NAME = 1;
	private static final int COL_IDX_FIELD = 2;
	private static final int COL_IDX_DEL = 3;

	private static final String VALIDATION_MSG_NO_ATTR_NAME = "Please input attribute name.";
	private static final String VALIDATION_MSG_DUP_ATTR_NAME = "Duplicated attribute name. please change the name.";

	private TextField addAttrNameTextField;
	private Node addAttrValueNode;
	private Map<String, AttributeValue> updAttributeMap = new HashMap<>();

	protected List<String> attrNameList = null;

	public DynamoDbMapInputDialog(Map<String, AttributeValue> dynamoDbRecord, String dialogTitle) {
		super(dynamoDbRecord, dialogTitle);
	}

	@Override
	protected List<Integer> getHeaderWidthList() {
		List<Integer> retList = new ArrayList<>();
		retList.add(NAME_COL_WIDTH);
		retList.add(NAME_COL_WIDTH);
		retList.add(FILELD_WIDTH);
		retList.add(DEL_COL_WIDTH);
		return retList;
	};

	@Override
	protected List<Node> getHeaderLabelList() {
		Label typeTitilelabel = getContentLabel("TYPE", true);
		Label keyTitilelabel = getContentLabel("NAME", true);
		Label valTtilelabel = getContentLabel("VALUE", true);
		Label delTtilelabel = getContentLabel("DEL", true);
		List<Node> retList = new ArrayList<Node>();
		retList.add(typeTitilelabel);
		retList.add(keyTitilelabel);
		retList.add(valTtilelabel);
		retList.add(delTtilelabel);
		return retList;
	}

	@Override
	protected List<List<Node>> getBodyAttributeNodeList() {
		List<List<Node>> retList = new ArrayList<>();
		List<String> attrNameList = DynamoDbUtils.getSortedAttrNameList(getDynamoDbRecordOrg());
		for (String attrName : attrNameList) {
			retList.add(getOneBodyAttributeNodeList(attrName, getDynamoDbRecordOrg().get(attrName)));
			getAttrNameList().add(attrName);
		}
		return retList;
	}

	@Override
	protected List<Node> getFooterNodeList() {

		List<Node> retList = new ArrayList<>();
		addAttrValueNode = getAtrributeBox(ADD_ATTR_NAME, getSelectedAddType().getInitValue());
		retList.add(getTypeComboBox());
		retList.add(getAddAttrNameTextField());
		retList.add(addAttrValueNode);
		retList.add(getAddButton());
		return retList;
	}

	@Override
	protected int getValueColIndex() {
		return 2;
	};

	@Override
	protected Map<String, AttributeValue> getEditedDynamoDbRecord() {
		Map<String, AttributeValue> retMap = new HashMap<>();
		List<List<Node>> currentBodyNodeList = getCurrentBodyNodeList();
		for (List<Node> wkNodeList : currentBodyNodeList) {
			CheckBox delCheck = (CheckBox) wkNodeList.get(COL_IDX_DEL);
			if (delCheck.isSelected()) {
				continue;
			}
			Node valueNode = wkNodeList.get(COL_IDX_FIELD);
			Label keylabel = (Label) wkNodeList.get(COL_IDX_NAME);
			AttributeValue newAttr = getAttributeFromNode(valueNode);
			retMap.put(keylabel.getText(), newAttr);
		}
		return retMap;
	}

	@Override
	void actAddNewAttribute() {
		String attrName = getAddAttrNameTextField().getText();
		AttributeValue attrVal = getAttributeFromNode(addAttrValueNode);
		if (!addValidationCheck(attrName, attrVal)) {
			return;
		}
		updAttributeMap.put(attrName, attrVal);
		List<Node> nodelList = getOneBodyAttributeNodeList(attrName, attrVal);
		addAttributeNodeList(nodelList);
		getAttrNameList().add(attrName);
	}

	@Override
	boolean isFinalValidationOk() {
		List<List<Node>> currentBodyNodeList = getCurrentBodyNodeList();
		for (List<Node> wkNodeList : currentBodyNodeList) {
			CheckBox delCheck = (CheckBox) wkNodeList.get(COL_IDX_DEL);
			if (delCheck.isSelected()) {
				continue;
			}
			Node valueNode = wkNodeList.get(COL_IDX_FIELD);
			if (!checkValueNode(valueNode)) {
				Alert alert = new Alert(AlertType.ERROR, VALIDATION_MSG_INVALID_VALUE);
				alert.showAndWait();
				valueNode.requestFocus();
				return false;
			}
		}
		return true;
	}

	/*
	 * 
	 */

	@Override
	protected AttributeValue getAttributeFromEditButtonId(String btnId) {
		String attrName = btnId.substring(EDTBTN_ID_PREFIX.length());
		if (attrName.equals(ADD_ATTR_NAME)) {
			return tempAddAttrValue;
		}
		if (updAttributeMap.containsKey(attrName)) {
			return updAttributeMap.get(attrName);
		} else {
			return getDynamoDbRecordOrg().get(attrName);
		}
	}

	@Override
	String getTitleAppendStr(String btnId) {
		return btnId.substring(EDTBTN_ID_PREFIX.length());
	}

	@Override
	protected void callBackSetNewAttribute(String btnId, AttributeValue attrVal) {
		String attrName = btnId.substring(EDTBTN_ID_PREFIX.length());
		if (attrName.equals(ADD_ATTR_NAME)) {
			tempAddAttrValue = attrVal;
		} else {
			Node valLabelNode = getDialogPane().lookup(String.format("#%s", VALLBL_ID_PREFIX + attrName));
			if (valLabelNode != null && valLabelNode instanceof Label) {
				Label valLabel = (Label) valLabelNode;
				valLabel.setText(DynamoDbUtils.getAttrString(attrVal));
			}
		}
		updAttributeMap.put(attrName, attrVal);
	}

	@Override
	protected String getAddAttrEditButtonId() {
		return EDTBTN_ID_PREFIX + ADD_ATTR_NAME;
	}

	@Override
	protected void onAddTypeComboSelChanged(String oldValue, String newValue) {
		tempAddAttrValue = null;
		updateFooter();
	}

	/*
	 * 
	 */
	protected List<Node> getOneBodyAttributeNodeList(String attrName, AttributeValue attrValue) {
		Label typelabel = getContentLabel(DynamoDbUtils.getAttrTypeString(attrValue));
		Label keylabel = getContentLabel(attrName);
		Node valueNode = getAtrributeBox(attrName, attrValue);
		CheckBox delCheck = new CheckBox();
		delCheck.setId(DEL_ID_PREFIX + attrName);

		List<Node> nodeList = new ArrayList<>();
		nodeList.add(addUnderlineStyleToNode(typelabel));
		nodeList.add(addUnderlineStyleToNode(keylabel));
		nodeList.add(addUnderlineStyleToNode(valueNode));
		nodeList.add(delCheck);
		return nodeList;
	}

	protected Node getAtrributeBox(String attrName, AttributeValue attrVal) {
		String attrStr = DynamoDbUtils.getAttrString(attrVal);
		TextField textField = new TextField(attrStr);
		if (attrVal.s() != null) {
			textField.setId(STRFLD_ID_PREFIX + attrName);
		} else if (attrVal.n() != null) {
			textField.setId(NUMFLD_ID_PREFIX + attrName);
		} else if (attrVal.b() != null) {
			textField.setId(BINFLD_ID_PREFIX + attrName);
		} else if (attrVal.bool() != null) {
			return getBooleanInput(attrVal);
		} else if (DynamoDbUtils.isNullAttr(attrVal)) {
			return getNullViewLabel();
		} else {
			return getAttrEditButton(attrName, attrStr);
		}
		return textField;
	}

	protected HBox getAttrEditButton(String attrName, String text) {
		HBox hbox = new HBox(HGAP);
		Label vallabel = getContentLabel(text);
		vallabel.setId(VALLBL_ID_PREFIX + attrName);
		Button button = new Button();
		button.setText(BTN_TITLE);
		button.setId(EDTBTN_ID_PREFIX + attrName);
		button.setOnAction((event) -> {
			Button wkBtn = (Button) event.getSource();
			actOpenEditDialog(wkBtn.getId());
		});
		hbox.getChildren().addAll(button, vallabel);
		return hbox;
	}

	/*
	 * 
	 */

	protected boolean addValidationCheck(String attrName, AttributeValue attrVal) {
		if (StringUtils.isEmpty(attrName)) {
			Alert alert = new Alert(AlertType.ERROR, VALIDATION_MSG_NO_ATTR_NAME);
			alert.showAndWait();
			getAddAttrNameTextField().requestFocus();
			return false;
		}
		if (updAttributeMap.containsKey(attrName)) {
			Alert alert = new Alert(AlertType.ERROR, VALIDATION_MSG_DUP_ATTR_NAME);
			alert.showAndWait();
			getAddAttrNameTextField().requestFocus();
			return false;
		}
		if (attrVal == null) {
			Alert alert = new Alert(AlertType.ERROR, VALIDATION_MSG_NO_ATTR_VALUE);
			alert.showAndWait();
			getAddButton().requestFocus();
			return false;
		}
		return true;
	}

	/*
	 * accessor
	 */
	protected List<String> getAttrNameList() {
		if (attrNameList == null) {
			attrNameList = new ArrayList<>();
		}
		return attrNameList;
	}

	protected TextField getAddAttrNameTextField() {
		if (addAttrNameTextField == null) {
			addAttrNameTextField = new TextField(ADD_ATTR_NAME);
		}
		return addAttrNameTextField;
	}
}
