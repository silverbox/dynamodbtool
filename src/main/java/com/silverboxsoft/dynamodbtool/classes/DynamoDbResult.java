package com.silverboxsoft.dynamodbtool.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;

public class DynamoDbResult {

	// private static final int MAX_ANALYZE_COUNT = 100;
	private List<DynamoDbColumn> columnList = new ArrayList<>();
	private Map<String, Integer> colNameIndex = new HashMap<>();
	private List<ObservableList<String>> resItems = new ArrayList<>();
	private KeySchemaElement partitionKeyElem = null;
	private KeySchemaElement sortKeyElem = null;

	public DynamoDbResult(QueryResponse response, TableDescription tableInfo) {
		prepareKeyInfo(tableInfo);
		initilize(response.items());
	}

	public DynamoDbResult(ScanResponse response, TableDescription tableInfo) {
		prepareKeyInfo(tableInfo);
		initilize(response.items());
	}

	private void prepareKeyInfo(TableDescription tableInfo) {
		List<KeySchemaElement> keyInfos = tableInfo.keySchema();
		// prepare Key Info
		for (KeySchemaElement k : keyInfos) {
			if (k.keyType() == KeyType.HASH) {
				partitionKeyElem = k;
			} else if (k.keyType() == KeyType.RANGE) {
				sortKeyElem = k;
			}
		}
		boolean isPartitionKeySet = false;
		// at first, set key column info
		for (AttributeDefinition attr : tableInfo.attributeDefinitions()) {
			String colName = attr.attributeName();
			// System.out.println(colName); // TODO
			DynamoDbColumn dbCol = new DynamoDbColumn();
			dbCol.setColumnName(colName);
			dbCol.setColumnType(getDynamoDbColumnType(attr));
			if (colName.equals(partitionKeyElem.attributeName())) {
				colNameIndex.put(colName, 0);
				columnList.add(0, dbCol);
				isPartitionKeySet = true;
			} else if (colName.equals(sortKeyElem.attributeName())) {
				colNameIndex.put(colName, 1);
				columnList.add(isPartitionKeySet ? 1 : 0, dbCol);
			} else {
				continue;
			}
		}
	}

	private void initilize(List<Map<String, AttributeValue>> items) {
		for (Map<String, AttributeValue> resItem : items) {
			ObservableList<String> record = FXCollections.observableArrayList();
			for (int colIdx = 0; colIdx < columnList.size(); colIdx++) {
				DynamoDbColumn dbCol = getDynamoDbColumn(colIdx);
				String columnName = dbCol.getColumnName();
				record.add(getAttrString(dbCol, resItem.get(columnName)));
			}

			Set<String> unsetKeyNameSet = resItem.keySet().stream().collect(Collectors.toSet());
			unsetKeyNameSet.removeAll(colNameIndex.keySet());
			for (String newColName : unsetKeyNameSet) {
				AttributeValue attrVal = resItem.get(newColName);
				DynamoDbColumn dbCol = new DynamoDbColumn();
				dbCol.setColumnName(newColName);
				dbCol.setColumnType(getDynamoDbColumnType(attrVal));
				record.add(getAttrString(dbCol, attrVal));
				colNameIndex.put(newColName, columnList.size());
				columnList.add(dbCol);
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
		// System.out.println(attrValStr); // TODO
		return DynamoDbColumnType.STRING;
	}

	private DynamoDbColumnType getDynamoDbColumnType(AttributeDefinition attr) {
		if (attr.attributeType() == ScalarAttributeType.S) {
			return DynamoDbColumnType.STRING;
		} else if (attr.attributeType() == ScalarAttributeType.N) {
			return DynamoDbColumnType.NUMBER;
		}
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
