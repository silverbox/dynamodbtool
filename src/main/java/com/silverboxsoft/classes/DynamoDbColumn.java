package com.silverboxsoft.classes;

import lombok.Data;

@Data
public class DynamoDbColumn {

	private String columnName;

	private DynamoDbColumnType columnType;
}
