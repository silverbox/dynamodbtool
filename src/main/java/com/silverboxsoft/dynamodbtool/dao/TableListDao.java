package com.silverboxsoft.dynamodbtool.dao;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.silverboxsoft.dynamodbtool.classes.DynamoDbConnectInfo;
import com.silverboxsoft.dynamodbtool.fxmodel.TableNameCondType;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ListTablesRequest;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;
import software.amazon.awssdk.utils.StringUtils;

/**
 * https://github.com/awsdocs/aws-doc-sdk-examples/blob/master/javav2/example_code/dynamodb/src/main/java/com/example/dynamodb/ListTables.java
 * 
 * @author tanakaeiji
 *
 */
public class TableListDao extends AbsDao {

	public TableListDao(DynamoDbConnectInfo connInfo) {
		super(connInfo);
	}

	public List<String> getTableList(String condition, TableNameCondType conditionType) throws URISyntaxException {
		DynamoDbClient ddb = getDbClient();
		try {
			return getAllTableNameLsit(ddb, condition, conditionType);
		} finally {
			ddb.close();
		}
	}

	private List<String> getAllTableNameLsit(DynamoDbClient ddb, String condition, TableNameCondType conditionType) {

		boolean moreTables = true;
		String lastName = null;
		List<String> retList = new ArrayList<>();

		while (moreTables) {
			ListTablesResponse response = null;
			if (lastName == null) {
				ListTablesRequest request = ListTablesRequest.builder().build();
				response = ddb.listTables(request);
			} else {
				ListTablesRequest request = ListTablesRequest.builder()
						.exclusiveStartTableName(lastName).build();
				response = ddb.listTables(request);
			}

			List<String> tableNames = response.tableNames();

			if (tableNames.size() > 0) {
				for (String curName : tableNames) {
					if (isMatchName(curName, condition, conditionType)) {
						retList.add(curName);
					}
				}
			}

			lastName = response.lastEvaluatedTableName();
			if (lastName == null) {
				moreTables = false;
			}
		}
		return retList;
	}

	private boolean isMatchName(String targetStr, String condition, TableNameCondType conditionType) {
		if (StringUtils.isEmpty(condition)) {
			return true;
		}
		if (conditionType == TableNameCondType.PARTIAL_MATCH) {
			return (targetStr.indexOf(condition) >= 0);
		} else if (conditionType == TableNameCondType.HEAD_MATCH) {
			return targetStr.startsWith(condition);
		}
		return targetStr.endsWith(condition);
	}
}
