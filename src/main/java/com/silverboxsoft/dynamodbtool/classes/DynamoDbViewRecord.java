package com.silverboxsoft.dynamodbtool.classes;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Data;
import lombok.experimental.Builder;

@Data
@Builder
public class DynamoDbViewRecord {
	int index;
	ObservableList<String> data = FXCollections.observableArrayList();
}
