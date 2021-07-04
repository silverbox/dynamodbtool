package com.silverboxsoft.dynamodbtool.classes

import javafx.collections.FXCollections

import lombok.Data
import lombok.experimental.Builder

@Data
@Builder
class DynamoDbViewRecord(private val index: Int, private var data: MutableList<String>) {
    fun getIndex(): Int {
        return index
    }
    fun setData(newData: MutableList<String>){
        this.data = newData
    }
    fun getData(): MutableList<String> {
        return data
    }
}