package com.silverboxsoft.dynamodbtool.classes

import javafx.collections.FXCollections

import lombok.Data
import lombok.experimental.Builder

@Data
@Builder
class DynamoDbViewRecord(private val indexPrm: Int, private val dataPrm: List<String>) {
    private val index: Int = indexPrm
    private var data: List<String> = dataPrm
    fun getIndex(): Int {
        return index
    }
    fun setData(newData: List<String>){
        this.data = newData
    }
    fun getData(): List<String> {
        return data
    }
}