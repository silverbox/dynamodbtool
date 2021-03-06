package com.silverboxsoft.dynamodbtool.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.silverboxsoft.dynamodbtool.classes.DynamoDbColumn;
import com.silverboxsoft.dynamodbtool.classes.DynamoDbColumnType;
import com.silverboxsoft.dynamodbtool.classes.DynamoDbConnectInfo;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndexDescription;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;

public class DynamoDbUtils {

	public static final String NO_VALSTR = "<unset>";

	public DynamoDbUtils(DynamoDbConnectInfo connInfo) throws Exception {
		throw new Exception("it's util class");
	}

	public static String getAttrString(AttributeValue attrVal) {
		if (attrVal == null) {
			return NO_VALSTR;
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

	public static String getAttrTypeString(AttributeValue attrVal) {
		if (attrVal == null) {
			return NO_VALSTR;
		} else if (attrVal.s() != null) {
			return DynamoDbColumnType.STRING.getDispStr();
		} else if (attrVal.n() != null) {
			return DynamoDbColumnType.NUMBER.getDispStr();
		} else if (attrVal.b() != null) {
			return DynamoDbColumnType.BINARY.getDispStr();
		} else if (attrVal.bool() != null) {
			return DynamoDbColumnType.BOOLEAN.getDispStr();
		} else if (attrVal.hasSs()) {
			return DynamoDbColumnType.STRING_SET.getDispStr();
		} else if (attrVal.hasNs()) {
			return DynamoDbColumnType.NUMBER_SET.getDispStr();
		} else if (attrVal.hasBs()) {
			return DynamoDbColumnType.BINARY_SET.getDispStr();
		} else if (attrVal.hasM()) {
			return DynamoDbColumnType.MAP.getDispStr();
		} else if (attrVal.hasL()) {
			return DynamoDbColumnType.LIST.getDispStr();
		} else if (isNullAttr(attrVal)) {
			return DynamoDbColumnType.NULL.getDispStr();
		}
		return "UNKNOWN";
	}

	/**
	 * for item field
	 * 
	 * @param attrVal
	 * @return
	 */
	// https://sdk.amazonaws.com/java/api/2.0.0/software/amazon/awssdk/services/dynamodb/model/AttributeValue.html
	public static DynamoDbColumnType getDynamoDbColumnType(AttributeValue attrVal) {
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
	public static DynamoDbColumnType getDynamoDbColumnType(AttributeDefinition attr) {
		if (attr.attributeType() == ScalarAttributeType.S) {
			return DynamoDbColumnType.STRING;
		} else if (attr.attributeType() == ScalarAttributeType.N) {
			return DynamoDbColumnType.NUMBER;
		} else if (attr.attributeType() == ScalarAttributeType.B) {
			return DynamoDbColumnType.BINARY;
		}
		return DynamoDbColumnType.NULL;
	}

	/*
	 * sdkbytes converter
	 */
	public static String getBase64StringFromSdkBytes(SdkBytes sdkByte) {
		return Base64.getEncoder().encodeToString(sdkByte.asByteArray());
	}

	public static SdkBytes getSdkBytesFromBase64String(String base64Str) {
		byte[] byteAry = Base64.getDecoder().decode(base64Str);
		return SdkBytes.fromByteArray(byteAry);
	}

	/*
	 * number converter
	 */
	public static String getNumStr(BigDecimal num) {
		return num.toString();
	}

	public static BigDecimal getBigDecimal(String str) {
		return new BigDecimal(str);
	}

	// work around of AttributeValue#nul()
	public static boolean isNullAttr(AttributeValue attrVal) {
		String wkStr = attrVal.toString();
		return wkStr.equals("AttributeValue(NUL=true)");
	}

	public static List<String> getSortedAttrNameList(Map<String, AttributeValue> dynamoDbRecord) {
		List<String> attrNameList = new ArrayList<>(dynamoDbRecord.keySet());
		attrNameList.sort(
				Comparator.comparing(attrName -> getDynamoDbColumnType(dynamoDbRecord.get(attrName)).getDispOrd()));
		return attrNameList;
	}

	public static String getKeyValueStr(TableDescription tableInfo, Map<String, AttributeValue> dynamoDbRecord) {
		StringBuilder sb = new StringBuilder();

		KeySchemaElement partitionKeyElem = null;
		KeySchemaElement sortKeyElem = null;
		List<KeySchemaElement> keyInfos = tableInfo.keySchema();
		// prepare Key Info
		for (KeySchemaElement k : keyInfos) {
			if (k.keyType() == KeyType.HASH) {
				partitionKeyElem = k;
			} else if (k.keyType() == KeyType.RANGE) {
				sortKeyElem = k;
			}
		}
		sb.append(getAttrString(dynamoDbRecord.get(partitionKeyElem.attributeName())));
		if (sortKeyElem != null) {
			sb.append(" - ");
			sb.append(getAttrString(dynamoDbRecord.get(sortKeyElem.attributeName())));
		}
		return sb.toString();
	}

	public static List<DynamoDbColumn> getSortedSchemeAttrNameList(TableDescription tableInfo) {
		KeySchemaElement partitionKeyElem = null;
		KeySchemaElement sortKeyElem = null;
		List<DynamoDbColumn> columnList = new ArrayList<>();
		Map<String, Integer> colNameIndex = new HashMap<>();
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
		return columnList;
	}

	// TODO don't use exception
	public static boolean isNumericStr(String checkStr) {
		try {
			getBigDecimal(checkStr);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static boolean isBase64Str(String checkStr) {
		try {
			getSdkBytesFromBase64String(checkStr);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
