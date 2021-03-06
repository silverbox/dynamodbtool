package com.silverboxsoft.dynamodbtool.classes;

import java.util.ArrayList;
import java.util.HashMap;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public enum DynamoDbColumnType {
	STRING("STRING", 0, DynamoDbColumnTypeCategory.SCALAR), //
	NUMBER("NUMBER", 1, DynamoDbColumnTypeCategory.SCALAR), //
	BOOLEAN("BOOL", 2, DynamoDbColumnTypeCategory.SCALAR), //
	BINARY("BINARY", 3, DynamoDbColumnTypeCategory.SCALAR), //
	NULL("NULL", 4, DynamoDbColumnTypeCategory.SCALAR), //
	STRING_SET("Set of STRING", 5, DynamoDbColumnTypeCategory.SET), //
	NUMBER_SET("Set of NUMBER", 6, DynamoDbColumnTypeCategory.SET), //
	BINARY_SET("Set of BINARY", 7, DynamoDbColumnTypeCategory.SET), //
	MAP("MAP", 8, DynamoDbColumnTypeCategory.DOCUMENT), //
	LIST("LIST", 9, DynamoDbColumnTypeCategory.DOCUMENT); //

	private final String dispStr;
	private final int dispOrd;
	private final DynamoDbColumnTypeCategory category;

	DynamoDbColumnType(String dispStr, int dispOrd, DynamoDbColumnTypeCategory category) {
		this.dispStr = dispStr;
		this.dispOrd = dispOrd;
		this.category = category;
	}

	public String getDispStr() {
		return dispStr;
	}

	public int getDispOrd() {
		return dispOrd;
	}

	public DynamoDbColumnTypeCategory getCategory() {
		return category;
	}

	public static DynamoDbColumnType getColumnType(String dispStr) {
		for (DynamoDbColumnType type : DynamoDbColumnType.values()) {
			if (type.getDispStr().equals(dispStr)) {
				return type;
			}
		}
		return null;
	}

	public AttributeValue getInitValue() {
		if (this == DynamoDbColumnType.STRING) {
			return AttributeValue.builder().s("").build();
		} else if (this == DynamoDbColumnType.NUMBER) {
			return AttributeValue.builder().n("0").build();
		} else if (this == DynamoDbColumnType.BOOLEAN) {
			return AttributeValue.builder().bool(true).build();
		} else if (this == DynamoDbColumnType.BINARY) {
			return AttributeValue.builder().b(SdkBytes.fromByteArray(new byte[0])).build();
		} else if (this == DynamoDbColumnType.NULL) {
			return AttributeValue.builder().nul(true).build();
		} else if (this == DynamoDbColumnType.STRING_SET) {
			return AttributeValue.builder().ss().build();
		} else if (this == DynamoDbColumnType.NUMBER_SET) {
			return AttributeValue.builder().ns().build();
		} else if (this == DynamoDbColumnType.BINARY_SET) {
			return AttributeValue.builder().bs().build();
		} else if (this == DynamoDbColumnType.LIST) {
			return AttributeValue.builder().l(new ArrayList<AttributeValue>()).build();
		} else if (this == DynamoDbColumnType.MAP) {
			return AttributeValue.builder().m(new HashMap<String, AttributeValue>()).build();
		}
		return null;
	}
}
