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
import software.amazon.awssdk.services.dynamodb.model.TableDescription;

public class DynamoDbResult {

	private List<DynamoDbColumn> columnList = new ArrayList<>();
	private Map<String, Integer> colNameIndex = new HashMap<>();
	private List<DynamoDbViewRecord> resItems = new ArrayList<>();
	private List<Map<String, AttributeValue>> rawResItems = new ArrayList<>();

	public DynamoDbResult(List<Map<String, AttributeValue>> items, TableDescription tableInfo) {
		prepareKeyInfo(tableInfo);
		initilize(items);
	}

	private void prepareKeyInfo(TableDescription tableInfo) {
		columnList = DynamoDbUtils.getSortedDynamoDbColumnList(tableInfo);
		for (int idx = 0; idx < columnList.size(); idx++) {
			colNameIndex.put(columnList.get(idx).getColumnName(), idx);
		}
	}

	private void initilize(List<Map<String, AttributeValue>> items) {
		for (Map<String, AttributeValue> resItem : items) {
			addRecord(resItem);
		}
	}

	public ObservableList<String> prepareOneTableRecord(Map<String, AttributeValue> resItem) {
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
		this.resItems.stream().forEach(rec -> rec.getData().add(DynamoDbUtils.NO_VALSTR));
	}

	public int getColumnCount() {
		return columnList.size();
	}

	public int getRecordCount() {
		return resItems.size();
	}

	public List<DynamoDbViewRecord> getResultItems() {
		return resItems;
	}

	public List<Map<String, AttributeValue>> getRawResItems() {
		return rawResItems;
	}

	public DynamoDbColumn getDynamoDbColumn(int index) {
		return columnList.get(index);
	}

	public DynamoDbViewRecord addRecord(Map<String, AttributeValue> newRec) {
		this.rawResItems.add(newRec);

		ObservableList<String> data = prepareOneTableRecord(newRec);
		DynamoDbViewRecord record = DynamoDbViewRecord.builder().index(this.resItems.size()).data(data)
				.build();
		this.resItems.add(record);
		return record;
	}

	public DynamoDbViewRecord updateRecord(int rowIndex, Map<String, AttributeValue> newRec) {
		rawResItems.set(rowIndex, newRec);
		ObservableList<String> data = prepareOneTableRecord(newRec);
		DynamoDbViewRecord record = resItems.get(rowIndex);
		record.setData(data);
		resItems.set(rowIndex, record);
		return record;
	}

	public void removeRecord(int rowIndex) {
		rawResItems.remove(rowIndex);
	}

	public Integer getColumnIndexByName(String colName) {
		if (colNameIndex.containsKey(colName)) {
			return colNameIndex.get(colName);
		}
		return -1;
	}
}
