package com.silverboxsoft.dynamodbtool.controller.inputdialog;

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
	protected List<R> getCurrentDynamoDbRecord() {
		List<R> retList = new ArrayList<R>();

		List<List<Node>> currentBodyNodeList = getCurrentBodyNodeList();
		for (List<Node> wkNodeList : currentBodyNodeList) {
			CheckBox delCheck = (CheckBox) wkNodeList.get(1);
			if (delCheck.isSelected()) {
				continue;
			}
			HBox valuebox = (HBox) wkNodeList.get(0);
			retList.add(getCurrentAttrubuteValue(valuebox));
		}
		return retList;
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
		for (int recIdx = 0; recIdx < getDynamoDbRecordOrg().size(); recIdx++) {
			R attrVal = getDynamoDbRecordOrg().get(recIdx);
			HBox valuebox = getAttrubuteBox(retList.size(), attrVal);
			CheckBox delCheck = new CheckBox();
			delCheck.setId(DEL_ID_PREFIX + String.valueOf(recIdx));
			List<Node> nodeList = new ArrayList<>();
			nodeList.add(valuebox);
			nodeList.add(delCheck);
			retList.add(nodeList);
		}
		return retList;
	}

	@Override
	protected AttributeValue getCurrentAttribute(String btnId) {
		return null;
	}

	@Override
	void callBackSetNewAttribute(String btnId, AttributeValue attrVal) {
	}

	abstract String getTypeString();

	abstract HBox getAttrubuteBox(int recIndex, R attr);

	abstract R getCurrentAttrubuteValue(HBox valuebox);
}
