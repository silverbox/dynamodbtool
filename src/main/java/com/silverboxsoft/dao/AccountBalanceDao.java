package com.silverboxsoft.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.silverboxsoft.model.AccountBalance;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

public class AccountBalanceDao {

	private static final String TABLE_NAME = "account_balance";
	private static final String PARTITION_KEY_ALIAS = "#tgt_date";
	private static final String PARTITION_KEY_NAME = "tgt_date";

	private DynamoDbClient ddb;

	public AccountBalanceDao() {
		Region region = Region.AP_NORTHEAST_1;
		this.ddb = DynamoDbClient.builder().region(region).build();
	}

	public List<AccountBalance> getBalanceListAtOneDay(String dateString) {
		HashMap<String, String> attrNameAlias = new HashMap<String, String>();
		attrNameAlias.put(PARTITION_KEY_ALIAS, PARTITION_KEY_NAME);

		HashMap<String, AttributeValue> attrValues = new HashMap<String, AttributeValue>();
		attrValues.put(":" + PARTITION_KEY_NAME, AttributeValue.builder().s(dateString).build());

		QueryRequest queryReq = QueryRequest.builder()
				.tableName(TABLE_NAME)
				.keyConditionExpression(PARTITION_KEY_ALIAS + " = :" + PARTITION_KEY_NAME)
				.expressionAttributeNames(attrNameAlias)
				.expressionAttributeValues(attrValues)
				.build();

		QueryResponse response = ddb.query(queryReq);
		List<Map<String, AttributeValue>> resData = response.items();

		return resData.stream().map(attmap -> convertFromMap(attmap)).collect(Collectors.toList());
	}

	private AccountBalance convertFromMap(Map<String, AttributeValue> data) {
		AccountBalance ret = new AccountBalance();
		ret.setTgtDate(data.get("tgt_date").s().toString());
		ret.setMethodCd(data.get("method_cd").s().toString());
		ret.setValue(Long.parseLong(data.get("value").n().toString()));
		return ret;
	}
}
