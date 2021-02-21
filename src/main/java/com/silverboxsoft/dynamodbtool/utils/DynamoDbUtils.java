package com.silverboxsoft.dynamodbtool.utils;

import java.util.Base64;
import java.util.stream.Collectors;

import com.silverboxsoft.dynamodbtool.classes.DynamoDbColumnType;
import com.silverboxsoft.dynamodbtool.classes.DynamoDbConnectInfo;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

public class DynamoDbUtils {

	public DynamoDbUtils(DynamoDbConnectInfo connInfo) throws Exception {
		throw new Exception("it's util class");
	}

	public static String getAttrString(AttributeValue attrVal) {
		if (attrVal == null) {
			return "<noattr>";
		} else if (attrVal.s() != null) {
			return attrVal.s();
		} else if (attrVal.n() != null) {
			return attrVal.n();
		} else if (attrVal.b() != null) {
			return getBase64StringFromSdkBytes(attrVal.b());
		} else if (attrVal.bool() != null) {
			return attrVal.bool().toString();
		} else if (attrVal.hasSs()) {
			return attrVal.ss().toString();
		} else if (attrVal.hasNs()) {
			return attrVal.ns().toString();
		} else if (attrVal.hasBs()) {
			return attrVal.bs().stream()
					.map(attr -> getBase64StringFromSdkBytes(attr))
					.collect(Collectors.toList()).toString();
		} else if (attrVal.hasM()) {
			return attrVal.m().entrySet().stream()
					.collect(Collectors.toMap(entry -> entry.getKey(), entry -> getAttrString(entry.getValue())))
					.toString();
		} else if (attrVal.hasL()) {
			return attrVal.l().stream().map(attr -> getAttrString(attr)).collect(Collectors.toList()).toString();
		} else if (isNullAttr(attrVal)) {
			return "<null>";
		}
		return attrVal.toString();
	}

	public static String getAttrTypeString(AttributeValue attrVal) {
		if (attrVal == null) {
			return "<noattr>";
		} else if (attrVal.s() != null) {
			return "STRING";
		} else if (attrVal.n() != null) {
			return "NUMBER";
		} else if (attrVal.b() != null) {
			return "BINARY";
		} else if (attrVal.bool() != null) {
			return "BOOL";
		} else if (attrVal.hasSs()) {
			return "Set of STRING";
		} else if (attrVal.hasNs()) {
			return "Set of NUMBER";
		} else if (attrVal.hasBs()) {
			return "Set of BINARY";
		} else if (attrVal.hasM()) {
			return "MAP";
		} else if (attrVal.hasL()) {
			return "LIST";
		} else if (isNullAttr(attrVal)) {
			return "NULL";
		}
		return "UNKNOWN";
	}

	/**
	 * for item field
	 * 
	 * @param attrVal
	 * @return
	 */
	// https://sdk.amazonaws.com/java/api/2.0.0/software/amazon/awssdk/services/dynamodb/model/AttributeValue.html
	public static DynamoDbColumnType getDynamoDbColumnType(AttributeValue attrVal) {
		if (attrVal == null) {
			return DynamoDbColumnType.NULL;
		} else if (attrVal.hasSs()) {
			return DynamoDbColumnType.STRING_SET;
		} else if (attrVal.hasNs()) {
			return DynamoDbColumnType.NUMBER_SET;
		} else if (attrVal.hasBs()) {
			return DynamoDbColumnType.BINARY_SET;
		} else if (attrVal.hasM()) {
			return DynamoDbColumnType.MAP;
		} else if (attrVal.hasL()) {
			return DynamoDbColumnType.LIST;
		} else if (attrVal.s() != null) {
			return DynamoDbColumnType.STRING;
		} else if (attrVal.b() != null) {
			return DynamoDbColumnType.BINARY;
		} else if (attrVal.bool() != null) {
			return DynamoDbColumnType.BOOLEAN;
		} else if (attrVal.n() != null) {
			return DynamoDbColumnType.NUMBER;
		}
		return DynamoDbColumnType.NULL;
	}

	/**
	 * for primary key field
	 * 
	 * @param attr
	 * @return
	 */
	public static DynamoDbColumnType getDynamoDbColumnType(AttributeDefinition attr) {
		if (attr.attributeType() == ScalarAttributeType.S) {
			return DynamoDbColumnType.STRING;
		} else if (attr.attributeType() == ScalarAttributeType.N) {
			return DynamoDbColumnType.NUMBER;
		} else if (attr.attributeType() == ScalarAttributeType.B) {
			return DynamoDbColumnType.BINARY;
		}
		return DynamoDbColumnType.NULL;
	}

	public static String getBase64StringFromSdkBytes(SdkBytes sdkByte) {
		return Base64.getEncoder().encodeToString(sdkByte.asByteArray());
	}

	// work around of AttributeValue#nul()
	public static boolean isNullAttr(AttributeValue attrVal) {
		String wkStr = attrVal.toString();
		return wkStr.equals("AttributeValue(NUL=true)");
	}
}
