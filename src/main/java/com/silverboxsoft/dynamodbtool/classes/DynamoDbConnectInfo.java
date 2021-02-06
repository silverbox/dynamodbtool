package com.silverboxsoft.dynamodbtool.classes;

import lombok.Data;

@Data
public class DynamoDbConnectInfo {

	private final String endpointUrl;

	private final DynamoDbConnectType connectType;

	public DynamoDbConnectInfo(DynamoDbConnectType connectType, String endpointUrl) {
		this.endpointUrl = endpointUrl;
		this.connectType = connectType;
	}
}
