package com.silverboxsoft.dynamodbtool.controller;

import java.util.ArrayList;
import java.util.List;

import com.silverboxsoft.dynamodbtool.utils.DynamoDbUtils;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class DynamoDbListInputDialog extends AbsDynamoDbInputDialog<List<AttributeValue>> {

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
		for (int recIdx = 0; recIdx < getDynamoDbRecord().size(); recIdx++) {
			AttributeValue attrVal = getDynamoDbRecord().get(recIdx);
			Label typelabel = getContentLabel(DynamoDbUtils.getAttrTypeString(attrVal));
			HBox valuebox = getAtrributeBox(recIdx, attrVal);
			CheckBox delCheck = new CheckBox();
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
		} else if (attrVal.n() != null) {
		} else if (attrVal.b() != null) {
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
		HBox hbox = new HBox(HGAP);
		Label vallabel = getContentLabel(text);
		Button button = new Button();
		button.setText(BTN_TITLE);
		button.setId(BTN_ID_PREFIX + String.valueOf(recIndex));
		button.setOnAction((event) -> {
			Button wkBtn = (Button) event.getSource();
			actOpenEditDialog(wkBtn.getId());
		});
		hbox.getChildren().addAll(button, vallabel);
		return hbox;
	}

	@Override
	AttributeValue getAttributeValue(String btnId) {
		String attrIndexStr = btnId.substring(BTN_ID_PREFIX.length());
		return getDynamoDbRecord().get(Integer.valueOf(attrIndexStr));
	}
}
