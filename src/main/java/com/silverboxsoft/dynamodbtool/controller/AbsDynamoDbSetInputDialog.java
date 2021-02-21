package com.silverboxsoft.dynamodbtool.controller;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public abstract class AbsDynamoDbSetInputDialog<R> extends AbsDynamoDbInputDialog<List<R>> {

	public AbsDynamoDbSetInputDialog(List<R> dynamoDbRecord) {
		super(dynamoDbRecord);
		this.setTitle(String.format("Edit set of %s", getTypeString()));
	}

	@Override
	protected List<Node> getHeaderLabelList() {
		Label valTtilelabel = getContentLabel("VALUE", true);
		Label delTtilelabel = getContentLabel("DEL", true);
		List<Node> retList = new ArrayList<Node>();
		retList.add(valTtilelabel);
		retList.add(delTtilelabel);
		return retList;
	}

	@Override
	protected List<List<Node>> getBodyAttribueNodeList() {
		List<List<Node>> retList = new ArrayList<>();
		for (int recIdx = 0; recIdx < getDynamoDbRecord().size(); recIdx++) {
			R attrVal = getDynamoDbRecord().get(recIdx);
			HBox valuebox = getAttrubuteBox(retList.size(), attrVal);
			CheckBox delCheck = new CheckBox();
			List<Node> nodeList = new ArrayList<>();
			nodeList.add(valuebox);
			nodeList.add(delCheck);
			retList.add(nodeList);
		}
		return retList;
	}

	@Override
	protected AttributeValue getAttributeValue(String btnId) {
		return null;
	}

	abstract String getTypeString();

	abstract HBox getAttrubuteBox(int recIndex, R attr);
}
