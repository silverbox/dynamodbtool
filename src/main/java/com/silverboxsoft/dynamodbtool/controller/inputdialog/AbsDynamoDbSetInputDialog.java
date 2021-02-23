package com.silverboxsoft.dynamodbtool.controller.inputdialog;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public abstract class AbsDynamoDbSetInputDialog<T> extends AbsDynamoDbInputDialog<List<T>> {

	public AbsDynamoDbSetInputDialog(List<T> dynamoDbRecord) {
		super(dynamoDbRecord);
		this.setTitle(String.format("Edit set of %s", getTypeString()));
	}

	abstract String getTypeString();

	abstract HBox getAttrubuteBox(int recIndex, T attr);

	abstract T getCurrentAttrubuteValue(HBox valuebox);

	@Override
	protected List<T> getCurrentDynamoDbRecord() {
		List<T> retList = new ArrayList<T>();

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
			T attrVal = getDynamoDbRecordOrg().get(recIdx);
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
}
