package com.silverboxsoft.dynamodbtool.fxmodel;

import java.util.ArrayList;
import java.util.List;

public enum TableNameCondType {
	PARTIAL_MATCH("部分一致"), HEAD_MATCH("前方一致"), TAIL_MATCH("後方一致");

	final private String name;

	private TableNameCondType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static List<String> getTitleList() {
		List<String> retList = new ArrayList<>();
		for (TableNameCondType type : TableNameCondType.values()) {
			retList.add(type.getName());
		}
		return retList;
	}

	public static TableNameCondType getByName(String name) {
		for (TableNameCondType type : TableNameCondType.values()) {
			if (type.getName().equals(name)) {
				return type;
			}
			;
		}
		return null;
	}
}