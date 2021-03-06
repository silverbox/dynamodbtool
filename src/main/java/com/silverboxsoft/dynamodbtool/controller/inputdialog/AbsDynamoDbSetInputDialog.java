package com.silverboxsoft.dynamodbtool.controller.inputdialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;

public abstract class AbsDynamoDbSetInputDialog<T> extends AbsDynamoDbInputDialog<List<T>> {

	protected Node addValueNode;
	private Map<String, T> addAttributeMap;

	public AbsDynamoDbSetInputDialog(List<T> dynamoDbRecord) {
		super(dynamoDbRecord);
		this.setTitle(String.format("Edit set of %s", getTypeString()));
		Button addButton = super.getAddButton();
		addButton.setOnAction((event) -> {
			actAddScalarAttribute();
		});
	}

	abstract String getTypeString();

	abstract Node getAttrubuteBox(int recIndex, T attr);

	abstract T getCurrentAttrubuteValue(Node valuebox);

	abstract T getInitAttribute();

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
	protected List<List<Node>> getBodyAttributeNodeList() {
		List<List<Node>> retList = new ArrayList<>();
		for (int recIdx = 0; recIdx < getDynamoDbRecordOrg().size(); recIdx++) {
			T attrVal = getDynamoDbRecordOrg().get(recIdx);
			retList.add(getOneBodyAttributeNodeList(recIdx, attrVal));
		}
		return retList;
	}

	protected int getNewRecIdx() {
		return getDynamoDbRecordOrg().size() + getAddAttributeMap().size();
	}

	protected Map<String, T> getAddAttributeMap() {
		if (addAttributeMap == null) {
			addAttributeMap = new HashMap<>();
		}
		return addAttributeMap;
	}

	@Override
	protected List<Node> getFooterNodeList() {
		T attrVal = getInitAttribute();
		int recIdx = getNewRecIdx();
		addValueNode = getAttrubuteBox(recIdx, attrVal);

		List<Node> retList = new ArrayList<>();
		retList.add(addValueNode);
		retList.add(getAddButton());

		return retList;
	}

	private List<Node> getOneBodyAttributeNodeList(int recIdx, T attrVal) {
		Node valueNode = getAttrubuteBox(recIdx, attrVal);
		CheckBox delCheck = new CheckBox();
		delCheck.setId(DEL_ID_PREFIX + String.valueOf(recIdx));

		List<Node> nodeList = new ArrayList<>();
		nodeList.add(valueNode);
		nodeList.add(delCheck);

		return nodeList;
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

	@Override
	protected void actAddScalarAttribute() {
		int recIdx = getNewRecIdx();
		String newIdStr = String.valueOf(recIdx);
		T attrVal = getCurrentAttrubuteValue(addValueNode);
		addAttributeMap.put(newIdStr, attrVal);
		List<Node> nodelList = getOneBodyAttributeNodeList(recIdx, attrVal);
		addAttributeNodeList(nodelList);
	}
}
