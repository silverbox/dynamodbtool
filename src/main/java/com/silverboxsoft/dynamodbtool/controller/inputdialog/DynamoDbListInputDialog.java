package com.silverboxsoft.dynamodbtool.controller.inputdialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.silverboxsoft.dynamodbtool.utils.DynamoDbUtils;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class DynamoDbListInputDialog extends AbsDynamoDbInputDialog<List<AttributeValue>> {

	private Map<String, AttributeValue> updAttributeMap = new HashMap<>();

	public DynamoDbListInputDialog(List<AttributeValue> dynamoDbRecord) {
		super(dynamoDbRecord);
	}

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
	protected List<List<Node>> getBodyAttribueNodeList() {
		List<List<Node>> retList = new ArrayList<>();
		for (int recIdx = 0; recIdx < getDynamoDbRecordOrg().size(); recIdx++) {
			AttributeValue attrVal = getDynamoDbRecordOrg().get(recIdx);
			Label typelabel = getContentLabel(DynamoDbUtils.getAttrTypeString(attrVal));
			HBox valuebox = getAtrributeBox(recIdx, attrVal);
			CheckBox delCheck = new CheckBox();
			delCheck.setId(DEL_ID_PREFIX + String.valueOf(recIdx));
			List<Node> nodeList = new ArrayList<>();
			nodeList.add(typelabel);
			nodeList.add(valuebox);
			nodeList.add(delCheck);
			retList.add(nodeList);
		}
		return retList;
	}

	protected HBox getAtrributeBox(int recIndex, AttributeValue attrVal) {
		String attrStr = DynamoDbUtils.getAttrString(attrVal);
		HBox hbox = new HBox();
		TextField textField = new TextField(attrStr);
		textField.setMinWidth(FILELD_WIDTH);
		Control control = textField;
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
		hbox.getChildren().addAll(control);
		return hbox;
	}

	protected HBox getAttrEditButton(int recIndex, String text) {
		String idStr = String.valueOf(recIndex);
		HBox hbox = new HBox(HGAP);
		Label vallabel = getContentLabel(text);
		vallabel.setId(VALLBL_ID_PREFIX + idStr);
		Button button = new Button();
		button.setText(BTN_TITLE);
		button.setId(BTN_ID_PREFIX + idStr);
		button.setOnAction((event) -> {
			Button wkBtn = (Button) event.getSource();
			actOpenEditDialog(wkBtn.getId());
		});
		hbox.getChildren().addAll(button, vallabel);
		return hbox;
	}

	@Override
	protected AttributeValue getCurrentAttribute(String btnId) {
		String recIndexStr = btnId.substring(BTN_ID_PREFIX.length());
		if (updAttributeMap.containsKey(recIndexStr)) {
			return updAttributeMap.get(recIndexStr);
		} else {
			return getDynamoDbRecordOrg().get(Integer.valueOf(recIndexStr));
		}
	}

	@Override
	protected void callBackSetNewAttribute(String btnId, AttributeValue attrVal) {
		String idStr = btnId.substring(BTN_ID_PREFIX.length());
		updAttributeMap.put(idStr, attrVal);
		Node valLabelNode = getDialogPane().lookup(String.format("#%s", VALLBL_ID_PREFIX + idStr));
		if (valLabelNode != null && valLabelNode instanceof Label) {
			Label valLabel = (Label) valLabelNode;
			valLabel.setText(DynamoDbUtils.getAttrString(attrVal));
		}
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
			HBox valuebox = (HBox) wkNodeList.get(1);
			AttributeValue newAttr = getCurrentAttributeValue(valuebox);
			retList.add(newAttr);
		}
		return retList;
	}

	protected AttributeValue getCurrentAttributeValue(HBox valuebox) {
		Node valueNode = valuebox.getChildren().get(0);
		if (valueNode instanceof RadioButton) {
			return AttributeValue.builder().bool(getBooleanValue(valuebox)).build();
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
		} else {
			Button button = (Button) valuebox.getChildren().get(0);
			return getCurrentAttribute(button.getId());
		}
		return null;
	}
}
