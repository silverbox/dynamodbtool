package com.silverboxsoft.dynamodbtool.classes;

import lombok.Data;

@Data
public class DynamoDbColumn {

	private String columnName;

	private DynamoDbColumnType columnType;
}
