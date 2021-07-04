package com.silverboxsoft.dynamodbtool.classes

import javafx.collections.FXCollections

import lombok.Data
import lombok.experimental.Builder

@Data
@Builder
class DynamoDbViewRecord {
    var index = 0
    var data = FXCollections.observableArrayList<String>()
}