package com.silverboxsoft.dynamodbtool.controller;

import java.util.Map;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;

public class DynamoDbRecordInputDialog extends DynamoDbMapInputDialog {

	public DynamoDbRecordInputDialog(TableDescription tableInfo, Map<String, AttributeValue> dynamoDbRecord) {
		super(dynamoDbRecord);
	}
}
