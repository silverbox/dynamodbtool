package com.silverboxsoft.dynamodbtool.controller.inputdialog;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;

public abstract class AbsDynamoDbSetInputDialog<T> extends AbsDynamoDbInputDialog<List<T>> {

	public AbsDynamoDbSetInputDialog(List<T> dynamoDbRecord) {
		super(dynamoDbRecord);
		this.setTitle(String.format("Edit set of %s", getTypeString()));
	}

	abstract String getTypeString();

	abstract Node getAttrubuteBox(int recIndex, T attr);

	abstract T getCurrentAttrubuteValue(Node valuebox);

	@Override
	protected List<T> getCurrentDynamoDbRecord() {
		List<T> retList = new ArrayList<T>();

		List<List<Node>> currentBodyNodeList = getCurrentBodyNodeList();
		for (List<Node> wkNodeList : currentBodyNodeList) {
			CheckBox delCheck = (CheckBox) wkNodeList.get(1);
			if (delCheck.isSelected()) {
				continue;
			}
			Node valuebox = wkNodeList.get(0);
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
			Node valueNode = getAttrubuteBox(retList.size(), attrVal);
			CheckBox delCheck = new CheckBox();
			delCheck.setId(DEL_ID_PREFIX + String.valueOf(recIdx));
			List<Node> nodeList = new ArrayList<>();
			nodeList.add(valueNode);
			nodeList.add(delCheck);
			retList.add(nodeList);
		}
		return retList;
	}

	@Override
	protected List<Integer> getHeaderWidthList() {
		List<Integer> retList = new ArrayList<>();
		retList.add(FILELD_WIDTH);
		retList.add(DEL_COL_WIDTH);
		return retList;
	};

	@Override
	protected int getValueColIndex() {
		return 0;
	};
}
