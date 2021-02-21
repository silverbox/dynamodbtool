package com.silverboxsoft.dynamodbtool.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.silverboxsoft.dynamodbtool.utils.DynamoDbUtils;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class DynamoDbMapInputDialog extends AbsDynamoDbInputDialog<Map<String, AttributeValue>> {

	private List<String> attrNameList = null;

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
		for (Map.Entry<String, AttributeValue> entry : getDynamoDbRecord().entrySet()) {
			Label typelabel = getContentLabel(DynamoDbUtils.getAttrTypeString(entry.getValue()));
			Label keylabel = getContentLabel(entry.getKey());
			HBox valuebox = getAtrributeBox(entry.getKey(), entry.getValue());
			CheckBox delCheck = new CheckBox();
			List<Node> nodeList = new ArrayList<>();
			nodeList.add(typelabel);
			nodeList.add(keylabel);
			nodeList.add(valuebox);
			nodeList.add(delCheck);
			attrNameList.add(entry.getKey());
			retList.add(nodeList);
		}
		return retList;
	}

	protected HBox getAtrributeBox(String attrName, AttributeValue attrVal) {
		String attrStr = DynamoDbUtils.getAttrString(attrVal);
		HBox hbox = new HBox();
		TextField textField = new TextField(attrStr);
		textField.setMinWidth(FILELD_WIDTH);
		Control control = textField;
		if (attrVal.s() != null) {
		} else if (attrVal.n() != null) {
		} else if (attrVal.b() != null) {
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
	AttributeValue getAttributeValue(String btnId) {
		String attrName = btnId.substring(BTN_ID_PREFIX.length());
		return getDynamoDbRecord().get(attrName);
	}
}
