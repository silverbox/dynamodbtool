package com.silverboxsoft;

import java.util.HashMap;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {
		final String USAGE = "\n" +
				"Usage:\n" +
				"    Query <tableName> <partitionKeyName> <partitionKeyVal>\n\n" +
				"Where:\n" +
				"    tableName - the Amazon DynamoDB table to put the item in (for example, Music3).\n" +
				"    partitionKeyName - the partition key name of the Amazon DynamoDB table (for example, Artist).\n" +
				"    partitionKeyVal - value of the partition key that should match (for example, Famous Band).\n\n" +
				"Example:\n";

		if (args.length != 3) {
			System.out.println(USAGE);
			System.exit(1);
		}

		String tableName = args[0];
		String partitionKeyName = args[1];
		String partitionKeyVal = args[2];
		String partitionAlias = "#a";

		System.out.format("Querying %s", tableName);
		System.out.println("");

		Region region = Region.AP_NORTHEAST_1;
		DynamoDbClient ddb = DynamoDbClient.builder()
				.region(region)
				.build();

		int count = queryTable(ddb, tableName, partitionKeyName, partitionKeyVal, partitionAlias);
		System.out.println("There were " + count + "record(s) returned");
		ddb.close();
	}

	// snippet-start:[dynamodb.java2.query.main]
	public static int queryTable(DynamoDbClient ddb,
			String tableName,
			String partitionKeyName,
			String partitionKeyVal,
			String partitionAlias) {

		// Set up an alias for the partition key name in case it's a reserved word
		HashMap<String, String> attrNameAlias = new HashMap<String, String>();

		attrNameAlias.put(partitionAlias, partitionKeyName);

		// Set up mapping of the partition name with the value
		HashMap<String, AttributeValue> attrValues = new HashMap<String, AttributeValue>();
		attrValues.put(":" + partitionKeyName, AttributeValue.builder().s(partitionKeyVal).build());

		QueryRequest queryReq = QueryRequest.builder()
				.tableName(tableName)
				.keyConditionExpression(partitionAlias + " = :" + partitionKeyName)
				.expressionAttributeNames(attrNameAlias)
				.expressionAttributeValues(attrValues)
				.build();

		try {
			QueryResponse response = ddb.query(queryReq);
			return response.count();
		} catch (DynamoDbException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		return -1;
	}
}
