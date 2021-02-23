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

public class DynamoDbMapInputDialog extends AbsDynamoDbDocumentInputDialog<Map<String, AttributeValue>> {

	protected List<String> attrNameList = null;
	private Map<String, AttributeValue> updAttributeMap = new HashMap<>();

	public DynamoDbMapInputDialog(Map<String, AttributeValue> dynamoDbRecord) {
		super(dynamoDbRecord);
	}

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
	protected List<List<Node>> getBodyAttribueNodeList() {
		if (attrNameList == null) {
			attrNameList = new ArrayList<>();
		}
		List<List<Node>> retList = new ArrayList<>();
		for (Map.Entry<String, AttributeValue> entry : getDynamoDbRecordOrg().entrySet()) {
			retList.add(getOneNodeList(entry.getKey(), entry.getValue()));
			attrNameList.add(entry.getKey());
		}
		return retList;
	}

	protected List<Node> getOneNodeList(String attrName, AttributeValue attrValue) {
		Label typelabel = getContentLabel(DynamoDbUtils.getAttrTypeString(attrValue));
		Label keylabel = getContentLabel(attrName);
		HBox valuebox = getAtrributeBox(attrName, attrValue);
		CheckBox delCheck = new CheckBox();
		delCheck.setId(DEL_ID_PREFIX + attrName);
		List<Node> nodeList = new ArrayList<>();
		nodeList.add(typelabel);
		nodeList.add(keylabel);
		nodeList.add(valuebox);
		nodeList.add(delCheck);
		return nodeList;
	}

	protected HBox getAtrributeBox(String attrName, AttributeValue attrVal) {
		String attrStr = DynamoDbUtils.getAttrString(attrVal);
		HBox hbox = new HBox();
		TextField textField = new TextField(attrStr);
		textField.setMinWidth(FILELD_WIDTH);
		Control control = textField;
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
		hbox.getChildren().addAll(control);
		return hbox;
	}

	protected HBox getAttrEditButton(String attrName, String text) {
		HBox hbox = new HBox(HGAP);
		Label vallabel = getContentLabel(text);
		vallabel.setId(VALLBL_ID_PREFIX + attrName);
		Button button = new Button();
		button.setText(BTN_TITLE);
		button.setId(BTN_ID_PREFIX + attrName);
		button.setOnAction((event) -> {
			Button wkBtn = (Button) event.getSource();
			actOpenEditDialog(wkBtn.getId());
		});
		hbox.getChildren().addAll(button, vallabel);
		return hbox;
	}

	@Override
	protected void callBackSetNewAttribute(String btnId, AttributeValue attrVal) {
		String attrName = btnId.substring(BTN_ID_PREFIX.length());
		updAttributeMap.put(attrName, attrVal);
		Node valLabelNode = getDialogPane().lookup(String.format("#%s", VALLBL_ID_PREFIX + attrName));
		if (valLabelNode != null && valLabelNode instanceof Label) {
			Label valLabel = (Label) valLabelNode;
			valLabel.setText(DynamoDbUtils.getAttrString(attrVal));
		}
	}

	@Override
	protected AttributeValue getCurrentAttribute(String btnId) {
		String attrName = btnId.substring(BTN_ID_PREFIX.length());
		if (updAttributeMap.containsKey(attrName)) {
			return updAttributeMap.get(attrName);
		} else {
			return getDynamoDbRecordOrg().get(attrName);
		}
	}

	@Override
	protected Map<String, AttributeValue> getCurrentDynamoDbRecord() {
		Map<String, AttributeValue> retMap = new HashMap<>();
		List<List<Node>> currentBodyNodeList = getCurrentBodyNodeList();
		for (List<Node> wkNodeList : currentBodyNodeList) {
			CheckBox delCheck = (CheckBox) wkNodeList.get(3);
			if (delCheck.isSelected()) {
				continue;
			}
			HBox valuebox = (HBox) wkNodeList.get(2);
			Label keylabel = (Label) wkNodeList.get(1);
			AttributeValue newAttr = getCurrentAttributeValue(valuebox);
			retMap.put(keylabel.getText(), newAttr);
		}
		return retMap;
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
