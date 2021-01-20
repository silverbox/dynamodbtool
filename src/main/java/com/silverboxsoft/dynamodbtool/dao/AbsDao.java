package com.silverboxsoft.dynamodbtool.dao;

import java.net.URI;
import java.net.URISyntaxException;

import com.silverboxsoft.dynamodbtool.classes.DynamoDbConnectInfo;
import com.silverboxsoft.dynamodbtool.classes.DynamoDbConnectType;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class AbsDao {

	private static Region region = Region.AP_NORTHEAST_1;

	private final DynamoDbConnectInfo connInfo;

	public AbsDao(DynamoDbConnectInfo connInfo) {
		this.connInfo = connInfo;
	}

	protected DynamoDbClient getDbClient() throws URISyntaxException {
		if (connInfo.getConnectType() == DynamoDbConnectType.AWS) {
			return DynamoDbClient.builder().region(region).build();
		} else {
			return DynamoDbClient.builder().endpointOverride(new URI(connInfo.getEndpointUrl())).region(region).build();
		}
	}
}
