package com.silverboxsoft.classes;

import lombok.Data;

@Data
public class DynamoDbCondition {
	private String columnName;
	private DynamoDbConditionType conditionType;
	private String value;

	public String getConditionExpression() {
		return getAlias().concat(conditionType.getCondStr()).concat(":").concat(columnName);
	}

	public String getAlias() {
		return "#".concat(columnName);
	}
}
