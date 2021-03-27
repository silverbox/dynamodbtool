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

public class DynamoDbListInputDialog extends AbsDynamoDbDocumentInputDialog<List<AttributeValue>> {

	private static final int ADD_INDEX = -1;

	private static final int COL_IDX_FIELD = 2;
	private static final int COL_IDX_DEL = 3;

	private Node addAttrValueNode;
	private Map<String, AttributeValue> updAttributeMap = new HashMap<>();
	private Map<String, AttributeValue> addAttributeMap = new HashMap<>();

	public DynamoDbListInputDialog(List<AttributeValue> dynamoDbRecord, String dialogTitle) {
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
		Label indexTitilelabel = getContentLabel("INDEX", true);
		Label typeTitilelabel = getContentLabel("TYPE", true);
		Label valTtilelabel = getContentLabel("VALUE", true);
		Label delTtilelabel = getContentLabel("DEL", true);
		List<Node> retList = new ArrayList<Node>();
		retList.add(indexTitilelabel);
		retList.add(typeTitilelabel);
		retList.add(valTtilelabel);
		retList.add(delTtilelabel);
		return retList;
	}

	@Override
	protected List<List<Node>> getBodyAttributeNodeList() {
		List<List<Node>> retList = new ArrayList<>();
		for (int recIdx = 0; recIdx < getDynamoDbRecordOrg().size(); recIdx++) {
			AttributeValue attrVal = getDynamoDbRecordOrg().get(recIdx);
			retList.add(getOneBodyAttributeNodeList(recIdx, attrVal));
		}
		return retList;
	}

	@Override
	protected List<Node> getFooterNodeList() {
		List<Node> retList = new ArrayList<>();
		addAttrValueNode = getAtrributeBox(ADD_INDEX, getSelectedAddType().getInitValue());
		retList.add(new Label("new Index"));
		retList.add(getTypeComboBox());
		retList.add(addAttrValueNode);
		retList.add(getAddButton());
		return retList;
	}

	@Override
	protected int getValueColIndex() {
		return 2;
	};

	@Override
	protected List<AttributeValue> getEditedDynamoDbRecord() {
		List<AttributeValue> retList = new ArrayList<>();
		List<List<Node>> currentBodyNodeList = getCurrentBodyNodeList();
		for (List<Node> wkNodeList : currentBodyNodeList) {
			CheckBox delCheck = (CheckBox) wkNodeList.get(COL_IDX_DEL);
			if (delCheck.isSelected()) {
				continue;
			}
			Node valueNode = (Node) wkNodeList.get(COL_IDX_FIELD);
			AttributeValue newAttr = getAttributeFromNode(valueNode);
			retList.add(newAttr);
		}
		return retList;
	}

	@Override
	protected List<AttributeValue> getEmptyAttr() {
		return new ArrayList<>();
	}

	@Override
	protected void actAddNewAttribute() {
		int recIdx = getNewRecIdx();
		String newIdStr = String.valueOf(recIdx);
		AttributeValue attrVal = getAttributeFromNode(addAttrValueNode);
		if (!addValidationCheck(attrVal)) {
			return;
		}
		getAddAttributeMap().put(newIdStr, attrVal);
		List<Node> nodelList = getOneBodyAttributeNodeList(recIdx, attrVal);
		addAttributeNodeList(nodelList);
	}

	@Override
	protected boolean isFinalValidationOk() {
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
		String recIndexStr = btnId.substring(EDTBTN_ID_PREFIX.length());
		if (recIndexStr.equals(String.valueOf(ADD_INDEX))) {
			return tempAddAttrValue;
		}
		if (updAttributeMap.containsKey(recIndexStr)) {
			return updAttributeMap.get(recIndexStr);
		} else if (addAttributeMap.containsKey(recIndexStr)) {
			return addAttributeMap.get(recIndexStr);
		} else {
			return getDynamoDbRecordOrg().get(Integer.valueOf(recIndexStr));
		}
	}

	@Override
	String getTitleAppendStr(String btnId) {
		return btnId.substring(EDTBTN_ID_PREFIX.length());
	}

	@Override
	protected void callBackSetNewAttribute(String btnId, AttributeValue attrVal) {
		String idStr = btnId.substring(EDTBTN_ID_PREFIX.length());
		Node valLabelNode = getDialogPane().lookup(String.format("#%s", VALLBL_ID_PREFIX + idStr));
		if (valLabelNode != null && valLabelNode instanceof Label) {
			Label valLabel = (Label) valLabelNode;
			valLabel.setText(DynamoDbUtils.getAttrString(attrVal));
		}

		if (idStr.equals(String.valueOf(ADD_INDEX))) {
			tempAddAttrValue = attrVal;
		}
		updAttributeMap.put(idStr, attrVal);
	}

	@Override
	protected String getAddAttrEditButtonId() {
		return EDTBTN_ID_PREFIX + String.valueOf(ADD_INDEX);
	}

	@Override
	protected void onAddTypeComboSelChanged(String oldValue, String newValue) {
		tempAddAttrValue = null;
		updateFooter();
	}

	/*
	 * 
	 */
	private List<Node> getOneBodyAttributeNodeList(int recIdx, AttributeValue attrVal) {
		Label indexLabel = getContentLabel(String.valueOf(recIdx));
		Label typeLabel = getContentLabel(DynamoDbUtils.getAttrTypeString(attrVal));
		Node valueBox = getAtrributeBox(recIdx, attrVal);
		CheckBox delCheck = new CheckBox();
		delCheck.setId(DEL_ID_PREFIX + String.valueOf(recIdx));

		List<Node> nodeList = new ArrayList<>();
		nodeList.add(addUnderlineStyleToNode(indexLabel));
		nodeList.add(addUnderlineStyleToNode(typeLabel));
		nodeList.add(addUnderlineStyleToNode(valueBox));
		nodeList.add(delCheck);

		return nodeList;
	}

	protected Node getAtrributeBox(int recIndex, AttributeValue attrVal) {
		String attrStr = DynamoDbUtils.getAttrString(attrVal);
		TextField textField = new TextField(attrStr);
		if (attrVal.s() != null) {
			textField.setId(STRFLD_ID_PREFIX + String.valueOf(recIndex));
		} else if (attrVal.n() != null) {
			textField.setId(NUMFLD_ID_PREFIX + String.valueOf(recIndex));
		} else if (attrVal.b() != null) {
			textField.setId(BINFLD_ID_PREFIX + String.valueOf(recIndex));
		} else if (attrVal.bool() != null) {
			return getBooleanInput(attrVal);
		} else if (DynamoDbUtils.isNullAttr(attrVal)) {
			return getNullViewLabel();
		} else {
			return getAttrEditButton(recIndex, attrStr);
		}
		return textField;
	}

	protected Node getAttrEditButton(int recIndex, String text) {
		String idStr = String.valueOf(recIndex);
		HBox hbox = new HBox(HGAP);
		Label vallabel = getContentLabel(text);
		vallabel.setId(VALLBL_ID_PREFIX + idStr);
		Button button = new Button();
		button.setText(BTN_TITLE);
		button.setId(EDTBTN_ID_PREFIX + idStr);
		button.setOnAction((event) -> {
			Button wkBtn = (Button) event.getSource();
			actOpenEditDialog(wkBtn.getId());
		});
		hbox.getChildren().addAll(button, vallabel);
		return hbox;
	}

	protected Map<String, AttributeValue> getAddAttributeMap() {
		if (addAttributeMap == null) {
			addAttributeMap = new HashMap<>();
		}
		return addAttributeMap;
	}

	protected int getNewRecIdx() {
		return getDynamoDbRecordOrg().size() + getAddAttributeMap().size();
	}

	protected boolean addValidationCheck(AttributeValue attrVal) {
		if (attrVal == null) {
			Alert alert = new Alert(AlertType.ERROR, VALIDATION_MSG_NO_ATTR_VALUE);
			alert.showAndWait();
			getAddButton().requestFocus();
			return false;
		}
		return true;
	}
}
