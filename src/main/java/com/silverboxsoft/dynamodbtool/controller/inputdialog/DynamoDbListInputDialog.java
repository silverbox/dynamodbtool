package com.silverboxsoft.dynamodbtool.controller.inputdialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.silverboxsoft.dynamodbtool.utils.DynamoDbUtils;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class DynamoDbListInputDialog extends AbsDynamoDbDocumentInputDialog<List<AttributeValue>> {

	private static final int ADD_INDEX = -1;

	private Node addAttrValueNode;
	private Map<String, AttributeValue> updAttributeMap = new HashMap<>();
	private Map<String, AttributeValue> addAttributeMap = new HashMap<>();

	public DynamoDbListInputDialog(List<AttributeValue> dynamoDbRecord) {
		super(dynamoDbRecord);
	}

	@Override
	protected List<Integer> getHeaderWidthList() {
		List<Integer> retList = new ArrayList<>();
		retList.add(NAME_COL_WIDTH);
		retList.add(FILELD_WIDTH);
		retList.add(DEL_COL_WIDTH);
		return retList;
	};

	@Override
	protected int getValueColIndex() {
		return 1;
	};

	@Override
	protected List<Node> getHeaderLabelList() {
		Label typeTitilelabel = getContentLabel("TYPE", true);
		Label valTtilelabel = getContentLabel("VALUE", true);
		Label delTtilelabel = getContentLabel("DEL", true);
		List<Node> retList = new ArrayList<Node>();
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

	private List<Node> getOneBodyAttributeNodeList(int recIdx, AttributeValue attrVal) {
		Label typelabel = getContentLabel(DynamoDbUtils.getAttrTypeString(attrVal));
		Node valuebox = getAtrributeBox(recIdx, attrVal);
		CheckBox delCheck = new CheckBox();
		delCheck.setId(DEL_ID_PREFIX + String.valueOf(recIdx));

		List<Node> nodeList = new ArrayList<>();
		nodeList.add(typelabel);
		nodeList.add(valuebox);
		nodeList.add(delCheck);

		return nodeList;
	}

	protected Node getAtrributeBox(int recIndex, AttributeValue attrVal) {
		String attrStr = DynamoDbUtils.getAttrString(attrVal);
		TextField textField = new TextField(attrStr);
		// textField.setMinWidth(FILELD_WIDTH);
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

	@Override
	protected AttributeValue getCurrentAttribute(String btnId) {
		String recIndexStr = btnId.substring(EDTBTN_ID_PREFIX.length());
		if (recIndexStr.equals(String.valueOf(ADD_INDEX))) {
			return getInitAttributeValue();
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
	protected void callBackSetNewAttribute(String btnId, AttributeValue attrVal) {
		String idStr = btnId.substring(EDTBTN_ID_PREFIX.length());
		if (idStr.equals(String.valueOf(ADD_INDEX))) {
			int recIdx = getNewRecIdx();
			List<Node> nodelList = getOneBodyAttributeNodeList(recIdx, attrVal);
			addAttributeNodeList(nodelList);
			String newIdStr = String.valueOf(recIdx);
			getAddAttributeMap().put(newIdStr, attrVal);
		} else {
			Node valLabelNode = getDialogPane().lookup(String.format("#%s", VALLBL_ID_PREFIX + idStr));
			if (valLabelNode != null && valLabelNode instanceof Label) {
				Label valLabel = (Label) valLabelNode;
				valLabel.setText(DynamoDbUtils.getAttrString(attrVal));
			}
		}
		updAttributeMap.put(idStr, attrVal);
	}

	@Override
	protected List<AttributeValue> getCurrentDynamoDbRecord() {
		List<AttributeValue> retList = new ArrayList<>();
		List<List<Node>> currentBodyNodeList = getCurrentBodyNodeList();
		for (List<Node> wkNodeList : currentBodyNodeList) {
			CheckBox delCheck = (CheckBox) wkNodeList.get(2);
			if (delCheck.isSelected()) {
				continue;
			}
			Node valueNode = (Node) wkNodeList.get(1);
			AttributeValue newAttr = getCurrentAttributeValue(valueNode);
			retList.add(newAttr);
		}
		return retList;
	}

	protected int getNewRecIdx() {
		return getDynamoDbRecordOrg().size() + getAddAttributeMap().size();
	}

	protected AttributeValue getCurrentAttributeValue(Node valueNode) {
		if (valueNode instanceof HBox) {
			HBox wkBox = (HBox) valueNode;
			Node wkNode = wkBox.getChildren().get(0);
			if (wkNode instanceof RadioButton) {
				return AttributeValue.builder().bool(getBooleanValue(wkBox)).build();
			} else {
				Button button = (Button) wkNode;
				return getCurrentAttribute(button.getId());
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

	@Override
	void actAddScalarAttribute() {
		int recIdx = getNewRecIdx();
		String newIdStr = String.valueOf(recIdx);
		AttributeValue attrVal = getCurrentAttributeValue(addAttrValueNode);
		getAddAttributeMap().put(newIdStr, attrVal);
		List<Node> nodelList = getOneBodyAttributeNodeList(recIdx, attrVal);
		addAttributeNodeList(nodelList);
	}

	@Override
	void onAddTypeComboSelChanged(String oldValue, String newValue) {
		updateFooter();
	}

	@Override
	List<Node> getFooterNodeList() {
		List<Node> retList = new ArrayList<>();
		addAttrValueNode = getAtrributeBox(ADD_INDEX, getSelectedAddType().getInitValue());
		retList.add(getTypeComboBox());
		retList.add(addAttrValueNode);
		retList.add(getAddButton());
		return retList;
	}

	protected Map<String, AttributeValue> getAddAttributeMap() {
		if (addAttributeMap == null) {
			addAttributeMap = new HashMap<>();
		}
		return addAttributeMap;
	}
}
