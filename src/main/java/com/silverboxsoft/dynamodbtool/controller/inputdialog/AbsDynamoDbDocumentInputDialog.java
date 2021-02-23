package com.silverboxsoft.dynamodbtool.controller.inputdialog;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.silverboxsoft.dynamodbtool.utils.DynamoDbUtils;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public abstract class AbsDynamoDbDocumentInputDialog<T> extends AbsDynamoDbInputDialog<T> {

	public AbsDynamoDbDocumentInputDialog(T dynamoDbRecord) {
		super(dynamoDbRecord);
	}

	abstract AttributeValue getCurrentAttribute(String btnId);

	abstract void callBackSetNewAttribute(String btnId, AttributeValue attrVal);

	protected void actOpenEditDialog(String btnId) {
		AttributeValue attrVal = getCurrentAttribute(btnId);
		if (attrVal.hasSs()) {
			DynamoDbStringSetInputDialog dialog = new DynamoDbStringSetInputDialog(attrVal.ss());
			Optional<List<String>> newRec = dialog.showAndWait();
			if (newRec.isPresent()) {
				AttributeValue attrValue = AttributeValue.builder().ss(newRec.get()).build();
				callBackSetNewAttribute(btnId, attrValue);
			}
		} else if (attrVal.hasNs()) {
			List<BigDecimal> setList = attrVal.ns().stream()
					.map(strval -> DynamoDbUtils.getBigDecimal(strval))
					.collect(Collectors.toList());
			DynamoDbNumberSetInputDialog dialog = new DynamoDbNumberSetInputDialog(setList);
			Optional<List<BigDecimal>> newRec = dialog.showAndWait();
			if (newRec.isPresent()) {
				List<String> numStrList = newRec.get().stream()
						.map(bd -> DynamoDbUtils.getNumStr(bd))
						.collect(Collectors.toList());
				AttributeValue attrValue = AttributeValue.builder().ns(numStrList).build();
				callBackSetNewAttribute(btnId, attrValue);
			}
		} else if (attrVal.hasBs()) {
			DynamoDbBinarySetInputDialog dialog = new DynamoDbBinarySetInputDialog(attrVal.bs());
			Optional<List<SdkBytes>> newRec = dialog.showAndWait();
			if (newRec.isPresent()) {
				AttributeValue attrValue = AttributeValue.builder().bs(newRec.get()).build();
				callBackSetNewAttribute(btnId, attrValue);
			}
		} else if (attrVal.hasM()) {
			DynamoDbMapInputDialog dialog = new DynamoDbMapInputDialog(attrVal.m());
			Optional<Map<String, AttributeValue>> newRec = dialog.showAndWait();
			if (newRec.isPresent()) {
				AttributeValue attrValue = AttributeValue.builder().m(newRec.get()).build();
				callBackSetNewAttribute(btnId, attrValue);
			}
		} else if (attrVal.hasL()) {
			DynamoDbListInputDialog dialog = new DynamoDbListInputDialog(attrVal.l());
			Optional<List<AttributeValue>> newRec = dialog.showAndWait();
			if (newRec.isPresent()) {
				AttributeValue attrValue = AttributeValue.builder().l(newRec.get()).build();
				callBackSetNewAttribute(btnId, attrValue);
			}
		}
	}
}
