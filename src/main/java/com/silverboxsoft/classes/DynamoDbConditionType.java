package com.silverboxsoft.classes;

public enum DynamoDbConditionType {
	EQUAL(" = "), GREATER_THAN(" > "), LESSER_THAN(" < "), GREATER_THAN_EQ(" >= "), LESSER_THAN_EQ(" <= ");

	private final String condStr;

	DynamoDbConditionType(String condStr) {
		this.condStr = condStr;
	}

	public String getCondStr() {
		return condStr;
	}
}
