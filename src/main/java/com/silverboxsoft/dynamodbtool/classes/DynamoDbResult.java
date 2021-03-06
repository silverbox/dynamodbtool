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
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;

public class DynamoDbResult {

	private List<DynamoDbColumn> columnList = new ArrayList<>();
	private Map<String, Integer> colNameIndex = new HashMap<>();
	private List<ObservableList<String>> resItems = new ArrayList<>();
	private List<Map<String, AttributeValue>> rawResItems = new ArrayList<>();

	public DynamoDbResult(QueryResponse response, TableDescription tableInfo) {
		prepareKeyInfo(tableInfo);
		initilize(response.items());
	}

	public DynamoDbResult(ScanResponse response, TableDescription tableInfo) {
		prepareKeyInfo(tableInfo);
		initilize(response.items());
	}

	private void prepareKeyInfo(TableDescription tableInfo) {
		columnList = DynamoDbUtils.getSortedSchemeAttrNameList(tableInfo);
		for (int idx = 0; idx < columnList.size(); idx++) {
			colNameIndex.put(columnList.get(idx).getColumnName(), idx);
		}
	}

	private void initilize(List<Map<String, AttributeValue>> items) {
		for (Map<String, AttributeValue> resItem : items) {
			this.rawResItems.add(resItem);

			ObservableList<String> record = getOneTableRecord(resItem);
			this.resItems.add(record);
		}
	}

	public ObservableList<String> getOneTableRecord(Map<String, AttributeValue> resItem) {
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
		return record;
	}

	private void fillNewColumn() {
		this.resItems.stream().forEach(rec -> rec.add(DynamoDbUtils.NO_VALSTR));
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

	public void updateRecord(int rowIndex, Map<String, AttributeValue> newRec) {
		rawResItems.set(rowIndex, newRec);
	}
}
