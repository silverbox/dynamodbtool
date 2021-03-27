package com.silverboxsoft.dynamodbtool.controller.inputdialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;

public abstract class AbsDynamoDbSetInputDialog<T> extends AbsDynamoDbInputDialog<List<T>> {

	protected static final String VALIDATION_MSG_DUP_VALUE = "Same value exists. please change the value";
	protected Node addValueNode;
	private Map<String, T> addAttributeMap;

	public AbsDynamoDbSetInputDialog(List<T> dynamoDbRecord, String dialogTitle) {
		super(dynamoDbRecord, dialogTitle);
		this.setTitle(this.getTitle().concat(String.format("[%s Set]", getTypeString())));
	}

	@Override
	protected List<Integer> getHeaderWidthList() {
		List<Integer> retList = new ArrayList<>();
		retList.add(FILELD_WIDTH);
		retList.add(DEL_COL_WIDTH);
		return retList;
	};

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

	@Override
	protected int getValueColIndex() {
		return 0;
	};

	@Override
	protected List<T> getEditedDynamoDbRecord() {
		List<T> retList = new ArrayList<T>();

		List<List<Node>> currentBodyNodeList = getCurrentBodyNodeList();
		for (List<Node> wkNodeList : currentBodyNodeList) {
			CheckBox delCheck = (CheckBox) wkNodeList.get(1);
			if (delCheck.isSelected()) {
				continue;
			}
			Node valueNode = wkNodeList.get(0);
			retList.add(getCurrentAttrubuteValue(valueNode));
		}
		return retList;
	}

	@Override
	protected List<T> getEmptyAttr() {
		return new ArrayList<>();
	}

	@Override
	protected void actAddNewAttribute() {
		int recIdx = getNewRecIdx();
		String newIdStr = String.valueOf(recIdx);
		T attrVal = getCurrentAttrubuteValue(addValueNode);
		if (!validationCheck(attrVal)) {
			return;
		}
		addAttributeMap.put(newIdStr, attrVal);
		List<Node> nodelList = getOneBodyAttributeNodeList(recIdx, attrVal);
		addAttributeNodeList(nodelList);
	}

	@Override
	final boolean isFinalValidationOk() {
		Set<T> checkSet = new HashSet<T>();
		List<List<Node>> currentBodyNodeList = getCurrentBodyNodeList();
		for (List<Node> wkNodeList : currentBodyNodeList) {
			CheckBox delCheck = (CheckBox) wkNodeList.get(1);
			if (delCheck.isSelected()) {
				continue;
			}
			Node valueNode = wkNodeList.get(0);
			T wkVal = getCurrentAttrubuteValue(valueNode);
			if (wkVal == null) {
				Alert alert = new Alert(AlertType.ERROR, VALIDATION_MSG_INVALID_VALUE);
				alert.showAndWait();
				valueNode.requestFocus();
				return false;
			}
			if (checkSet.contains(wkVal)) {
				Alert alert = new Alert(AlertType.ERROR, VALIDATION_MSG_DUP_VALUE);
				alert.showAndWait();
				valueNode.requestFocus();
				return false;
			}
			checkSet.add(wkVal);
		}
		return true;
	}

	/*
	 * 
	 */
	abstract String getTypeString();

	abstract Node getAttrubuteBox(int recIndex, T attr);

	abstract T getCurrentAttrubuteValue(Node valueNode);

	abstract T getInitAttribute();

	/*
	 * for validation
	 */
	abstract boolean isSameValue(T valA, T valB);

	/*
	 * accessor
	 */
	protected Node getAddValueNode() {
		return addValueNode;
	}

	/*
	 * 
	 */
	protected int getNewRecIdx() {
		return getDynamoDbRecordOrg().size() + getAddAttributeMap().size();
	}

	protected Map<String, T> getAddAttributeMap() {
		if (addAttributeMap == null) {
			addAttributeMap = new HashMap<>();
		}
		return addAttributeMap;
	}

	protected boolean validationCheck(T attrVal) {
		List<List<Node>> currentBodyNodeList = getCurrentBodyNodeList();
		for (List<Node> wkNodeList : currentBodyNodeList) {
			Node valuebox = wkNodeList.get(0);
			T wkAttr = getCurrentAttrubuteValue(valuebox);
			if (isSameValue(wkAttr, attrVal)) {
				Alert alert = new Alert(AlertType.ERROR, VALIDATION_MSG_DUP_VALUE);
				alert.showAndWait();
				addValueNode.requestFocus();
				return false;
			}
		}
		return true;
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
}
