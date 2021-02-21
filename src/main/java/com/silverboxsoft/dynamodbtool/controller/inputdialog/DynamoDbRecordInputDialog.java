package com.silverboxsoft.dynamodbtool.controller.inputdialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.silverboxsoft.dynamodbtool.classes.DynamoDbColumn;
import com.silverboxsoft.dynamodbtool.utils.DynamoDbUtils;

import javafx.scene.Node;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;

public class DynamoDbRecordInputDialog extends DynamoDbMapInputDialog {
	private List<DynamoDbColumn> columnList = new ArrayList<>();

	public DynamoDbRecordInputDialog(TableDescription tableInfo, Map<String, AttributeValue> dynamoDbRecord) {
		super(dynamoDbRecord);
		columnList = DynamoDbUtils.getSortedSchemeAttrNameList(tableInfo);
		this.initialize(); // TODO work around
	}

	@Override
	protected List<List<Node>> getBodyAttribueNodeList() {
		List<List<Node>> retList = new ArrayList<>();
		if (columnList == null) {
			return retList;
		}
		if (attrNameList == null) {
			attrNameList = new ArrayList<>();
		}

		// add key info first
		for (int colIdx = 0; colIdx < columnList.size(); colIdx++) {
			String attrName = columnList.get(colIdx).getColumnName();
			retList.add(getOneNodeList(attrName, getDynamoDbRecordOrg().get(attrName)));
			attrNameList.add(attrName);
		}

		// pick up the data which attribute name is come yet.
		Set<String> unsetKeyNameSet = getDynamoDbRecordOrg().keySet();
		Set<String> keyNameSet = attrNameList.stream().collect(Collectors.toSet());
		for (String newAttrName : unsetKeyNameSet) {
			if (keyNameSet.contains(newAttrName)) {
				continue;
			}
			retList.add(getOneNodeList(newAttrName, getDynamoDbRecordOrg().get(newAttrName)));
			attrNameList.add(newAttrName);
		}

		return retList;
	}
}
