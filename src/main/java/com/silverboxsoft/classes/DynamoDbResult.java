package com.silverboxsoft.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

public class DynamoDbResult {

	private List<DynamoDbColumn> columnList = new ArrayList<>();
	private Map<String, Integer> colNameIndex = new HashMap<>();
	private List<ObservableList<String>> resItems = new ArrayList<>();

	public DynamoDbResult(QueryResponse response) {
		for (Map<String, AttributeValue> resItem : response.items()) {
			analyzeOneItem(resItem);

			ObservableList<String> record = FXCollections.observableArrayList();
			for (int colIdx = 0; colIdx < columnList.size(); colIdx++) {
				DynamoDbColumn dbCol = getDynamoDbColumn(colIdx);
				String columnName = dbCol.getColumnName();
				record.add(getAttrString(dbCol, resItem.get(columnName)));
			}
			this.resItems.add(record);
		}
	}

	public int getColumnCount() {
		return columnList.size();
	}

	public int getRecordCount() {
		return resItems.size();
	}

	public List<ObservableList<String>> getResultItems() {
		return resItems;
	}

	public DynamoDbColumn getDynamoDbColumn(int index) {
		return columnList.get(index);
	}

	private void analyzeOneItem(Map<String, AttributeValue> resItem) {
		for (String colName : resItem.keySet()) {
			AttributeValue attrVal = resItem.get(colName);
			if (!colNameIndex.containsKey(colName)) {
				DynamoDbColumn dbCol = new DynamoDbColumn();
				dbCol.setColumnName(colName);
				dbCol.setColumnType(getDynamoDbColumnType(attrVal));
				colNameIndex.put(colName, columnList.size());
				columnList.add(dbCol);
			}
		}
	}

	// https://sdk.amazonaws.com/java/api/2.0.0/software/amazon/awssdk/services/dynamodb/model/AttributeValue.html
	private DynamoDbColumnType getDynamoDbColumnType(AttributeValue attrVal) {
		if (attrVal.hasSs()) {
			return DynamoDbColumnType.STRING_SET;
		} else if (attrVal.hasNs()) {
			return DynamoDbColumnType.NUMBER_SET;
		} else if (attrVal.hasBs()) {
			return DynamoDbColumnType.BINARY_SET;
		} else if (attrVal.hasM()) {
			return DynamoDbColumnType.MAP;
		} else if (attrVal.hasL()) {
			return DynamoDbColumnType.LIST;
		}
		String attrValStr = attrVal.toString();
		if (attrValStr.startsWith("AttributeValue(N=")) {
			return DynamoDbColumnType.NUMBER;
		}
		System.out.println(attrValStr);
		return DynamoDbColumnType.STRING;
	}

	private String getAttrString(DynamoDbColumn dbCol, AttributeValue attrVal) {
		if (dbCol.getColumnType() == DynamoDbColumnType.STRING) {
			return attrVal.s();
		} else if (dbCol.getColumnType() == DynamoDbColumnType.NUMBER) {
			return attrVal.n();
		}
		return attrVal.toString();
	}
}
