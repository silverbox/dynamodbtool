package com.silverboxsoft.dynamodbtool.classes;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndexDescription;
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
		Map<String, DynamoDbColumn> wkGsiColMap = new HashMap<>();
		// at first, set key column info
		for (AttributeDefinition attr : tableInfo.attributeDefinitions()) {
			String colName = attr.attributeName();
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
		for (Map<String, AttributeValue> resItem : items) {
			ObservableList<String> record = FXCollections.observableArrayList();

			// pick up data which attribute name is set.
			for (int colIdx = 0; colIdx < columnList.size(); colIdx++) {
				DynamoDbColumn dbCol = getDynamoDbColumn(colIdx);
				String columnName = dbCol.getColumnName();
				record.add(getAttrString(resItem.get(columnName)));
			}

			// pick up the data which attribute name is come yet.
			Set<String> unsetKeyNameSet = resItem.keySet().stream().collect(Collectors.toSet());
			unsetKeyNameSet.removeAll(colNameIndex.keySet());
			for (String newColName : unsetKeyNameSet) {
				AttributeValue attrVal = resItem.get(newColName);
				DynamoDbColumn dbCol = new DynamoDbColumn();
				dbCol.setColumnName(newColName);
				dbCol.setColumnType(getDynamoDbColumnType(attrVal));
				fillNewColumn();
				record.add(getAttrString(attrVal));
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

	public DynamoDbColumn getDynamoDbColumn(int index) {
		return columnList.get(index);
	}

	/**
	 * for item field
	 * 
	 * @param attrVal
	 * @return
	 */
	// https://sdk.amazonaws.com/java/api/2.0.0/software/amazon/awssdk/services/dynamodb/model/AttributeValue.html
	private DynamoDbColumnType getDynamoDbColumnType(AttributeValue attrVal) {
		if (attrVal == null) {
			return DynamoDbColumnType.NULL;
		} else if (attrVal.hasSs()) {
			return DynamoDbColumnType.STRING_SET;
		} else if (attrVal.hasNs()) {
			return DynamoDbColumnType.NUMBER_SET;
		} else if (attrVal.hasBs()) {
			return DynamoDbColumnType.BINARY_SET;
		} else if (attrVal.hasM()) {
			return DynamoDbColumnType.MAP;
		} else if (attrVal.hasL()) {
			return DynamoDbColumnType.LIST;
		} else if (attrVal.s() != null) {
			return DynamoDbColumnType.STRING;
		} else if (attrVal.b() != null) {
			return DynamoDbColumnType.BINARY;
		} else if (attrVal.bool() != null) {
			return DynamoDbColumnType.BOOLEAN;
		} else if (attrVal.n() != null) {
			return DynamoDbColumnType.NUMBER;
		}
		return DynamoDbColumnType.NULL;
	}

	/**
	 * for primary key field
	 * 
	 * @param attr
	 * @return
	 */
	private DynamoDbColumnType getDynamoDbColumnType(AttributeDefinition attr) {
		if (attr.attributeType() == ScalarAttributeType.S) {
			return DynamoDbColumnType.STRING;
		} else if (attr.attributeType() == ScalarAttributeType.N) {
			return DynamoDbColumnType.NUMBER;
		} else if (attr.attributeType() == ScalarAttributeType.B) {
			return DynamoDbColumnType.BINARY;
		}
		return DynamoDbColumnType.NULL;
	}

	private String getAttrString(AttributeValue attrVal) {
		if (attrVal == null) {
			return "<noattr>";
		} else if (attrVal.s() != null) {
			return attrVal.s();
		} else if (attrVal.n() != null) {
			return attrVal.n();
		} else if (attrVal.b() != null) {
			return getBase64StringFromSdkBytes(attrVal.b());
		} else if (attrVal.bool() != null) {
			return attrVal.bool().toString();
		} else if (attrVal.hasSs()) {
			return attrVal.ss().toString();
		} else if (attrVal.hasNs()) {
			return attrVal.ns().toString();
		} else if (attrVal.hasBs()) {
			return attrVal.bs().stream()
					.map(attr -> getBase64StringFromSdkBytes(attr))
					.collect(Collectors.toList()).toString();
		} else if (attrVal.hasM()) {
			return attrVal.m().entrySet().stream()
					.collect(Collectors.toMap(entry -> entry.getKey(), entry -> getAttrString(entry.getValue())))
					.toString();
		} else if (attrVal.hasL()) {
			return attrVal.l().stream().map(attr -> getAttrString(attr)).collect(Collectors.toList()).toString();
		} else if (isNullAttr(attrVal)) {
			return "<null>";
		}
		return attrVal.toString();
	}

	private String getBase64StringFromSdkBytes(SdkBytes sdkByte) {
		return Base64.getEncoder().encodeToString(sdkByte.asByteArray());
	}

	// work around of AttributeValue#nul()
	private boolean isNullAttr(AttributeValue attrVal) {
		String wkStr = attrVal.toString();
		return wkStr.equals("AttributeValue(NUL=true)");
	}
}
