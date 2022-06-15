package com.silverboxsoft.dynamodbtool.controller

import javafx.concurrent.WorkerStateEvent
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.stage.Stage
import javafx.util.Pair

class SearchResultJumpDialog(private val dynamoDbTable: DynamoDbTable): Dialog<Boolean>() {
    var posList: List<Pair<Int, Int>> = ArrayList()
        set(value) {
            field = value
            currentPos = 0
            refreshPosInfo()
        }

    private var currentPos = 0
    private val hBox = HBox()
    private val nextButton = Button()
    private val prevButton = Button()
    private val labelPosInfo = Label()
    private val labelInfo = Label()

    init {
        nextButton.text = "next"
        prevButton.text = "prev"
        labelInfo.padding = Insets(5.0,10.0,0.0,5.0)
        labelPosInfo.padding = Insets(5.0,5.0,0.0,5.0)
        nextButton.onMouseClicked = EventHandler { _ -> changePos(1) }
        prevButton.onMouseClicked = EventHandler { _ -> changePos(-1) }
        hBox.children.add(labelPosInfo)
        hBox.children.add(prevButton)
        hBox.children.add(nextButton)
        hBox.children.add(labelInfo)
        dialogPane.content = hBox
        dialogPane.buttonTypes.addAll(ButtonType.CLOSE)
        this.title = "Jump to hit cell"
        refreshPosInfo()

        val window = dialogPane.scene.window
        val stage = window as Stage
        stage.minHeight = 100.0
        stage.minWidth = 300.0
    }

    private fun changePos(diff: Int) {
        val aftVal = currentPos + diff
        if ((0 <= aftVal) && (aftVal <= posList.size - 1)) {
            currentPos = aftVal
            refreshPosInfo()
        }
    }

    private fun refreshPosInfo() {
        labelPosInfo.text = String.format("%d / %d", currentPos + 1, posList.size)
        labelInfo.text = dynamoDbTable.getSearchResultCellStr(currentPos)
        dynamoDbTable.jumpToSearchResultCell(currentPos)
    }
}