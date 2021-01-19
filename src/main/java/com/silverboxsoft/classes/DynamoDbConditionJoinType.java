package com.silverboxsoft.classes;

public enum DynamoDbConditionJoinType {
	AND(" and "), OR(" or ");

	private final String joinStr;

	DynamoDbConditionJoinType(String joinStr) {
		this.joinStr = joinStr;
	}

	public String getJoinStr() {
		return joinStr;
	}
}
