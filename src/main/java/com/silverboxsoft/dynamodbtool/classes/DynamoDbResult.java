package com.silverboxsoft.dynamodbtool.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.silverboxsoft.dynamodbtool.utils.DynamoDbUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndexDescription;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;

public class DynamoDbResult {

	// private static final int MAX_ANALYZE_COUNT = 100;
	private List<DynamoDbColumn> columnList = new ArrayList<>();
	private Map<String, Integer> colNameIndex = new HashMap<>();
	private List<ObservableList<String>> resItems = new ArrayList<>();
	private List<Map<String, AttributeValue>> rawResItems = new ArrayList<>();
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
		Map<String, DynamoDbColumn> wkGsiColMap = new HashMap<>();
		// at first, set key column info
		for (AttributeDefinition attr : tableInfo.attributeDefinitions()) {
			String colName = attr.attributeName();
			DynamoDbColumn dbCol = new DynamoDbColumn();
			dbCol.setColumnName(colName);
			dbCol.setColumnType(DynamoDbUtils.getDynamoDbColumnType(attr));
			if (colName.equals(partitionKeyElem.attributeName())) {
				colNameIndex.put(colName, 0);
				columnList.add(0, dbCol);
				isPartitionKeySet = true;
			} else if (colName.equals(sortKeyElem.attributeName())) {
				colNameIndex.put(colName, 1);
				columnList.add(isPartitionKeySet ? 1 : 0, dbCol);
			} else {
				wkGsiColMap.put(colName, dbCol);
			}
		}

		// add global secondary index
		for (GlobalSecondaryIndexDescription gsiDesc : tableInfo.globalSecondaryIndexes()) {
			List<KeySchemaElement> wkKeyInfos = gsiDesc.keySchema();
			for (KeySchemaElement kse : wkKeyInfos) {
				String gsiColName = kse.attributeName();
				if (!colNameIndex.containsKey(gsiColName)) {
					colNameIndex.put(gsiColName, columnList.size());
					columnList.add(wkGsiColMap.get(gsiColName));
				}
			}
		}

	}

	private void initilize(List<Map<String, AttributeValue>> items) {
		this.rawResItems = items;
		for (Map<String, AttributeValue> resItem : items) {
			ObservableList<String> record = FXCollections.observableArrayList();

			// pick up data which attribute name is set.
			for (int colIdx = 0; colIdx < columnList.size(); colIdx++) {
				DynamoDbColumn dbCol = getDynamoDbColumn(colIdx);
				String columnName = dbCol.getColumnName();
				record.add(DynamoDbUtils.getAttrString(resItem.get(columnName)));
			}

			// pick up the data which attribute name is come yet.
			Set<String> unsetKeyNameSet = resItem.keySet().stream().collect(Collectors.toSet());
			unsetKeyNameSet.removeAll(colNameIndex.keySet());
			for (String newColName : unsetKeyNameSet) {
				AttributeValue attrVal = resItem.get(newColName);
				DynamoDbColumn dbCol = new DynamoDbColumn();
				dbCol.setColumnName(newColName);
				dbCol.setColumnType(DynamoDbUtils.getDynamoDbColumnType(attrVal));
				fillNewColumn();
				record.add(DynamoDbUtils.getAttrString(attrVal));
				colNameIndex.put(newColName, columnList.size());
				columnList.add(dbCol);
			}
			this.resItems.add(record);
		}
	}

	private void fillNewColumn() {
		this.resItems.stream().forEach(rec -> rec.add("<unset>"));
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

	public List<Map<String, AttributeValue>> getRawResItems() {
		return rawResItems;
	}

	public DynamoDbColumn getDynamoDbColumn(int index) {
		return columnList.get(index);
	}
}
