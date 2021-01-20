package com.silverboxsoft.dynamodbtool.classes;

import lombok.Data;

@Data
public class DynamoDbConnectInfo {

	private String endpointUrl;

	private DynamoDbConnectType connectType;
}
