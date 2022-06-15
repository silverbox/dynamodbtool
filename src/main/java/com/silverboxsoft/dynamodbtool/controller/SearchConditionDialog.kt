package com.silverboxsoft.dynamodbtool.controller

import javafx.scene.control.*

import com.silverboxsoft.dynamodbtool.classes.SearchCondition
import com.silverboxsoft.dynamodbtool.controller.inputdialog.AbsDynamoDbInputDialog
import javafx.event.EventHandler
import javafx.scene.layout.GridPane
import javafx.scene.layout.RowConstraints
import javafx.stage.Screen
import javafx.stage.Stage
import software.amazon.awssdk.utils.StringUtils

class SearchConditionDialog(condition: SearchCondition, dialogTitle: String) : Dialog<SearchCondition>() {
    private val gridPane: GridPane = GridPane()

    private val textFieldSearchText: TextField = TextField()
    private val checkBoxSelectedColumn: CheckBox = CheckBox("Only selected column")
    private val checkBoxCase: CheckBox = CheckBox("Case sensitive")
    private val checkBoxRegex: CheckBox = CheckBox("Search as regular expression")

    private val emptyCond = SearchCondition("")

    private var buttonData: ButtonBar.ButtonData = ButtonBar.ButtonData.OTHER
    private var isValidData = false

    companion object {
        const val GAP_H = 5.0
        const val GAP_V = 5.0
        const val GRID_MIN_HEIGHT = 25.0
    }

    init {
        textFieldSearchText.text = condition.searchWord
        checkBoxSelectedColumn.isSelected = condition.onlySelectedColumn
        checkBoxCase.isSelected = condition.caseSensitive
        checkBoxRegex.isSelected = condition.searchAsRegEx

        setResultConverter { dialogButton: ButtonType ->
            buttonData = dialogButton.buttonData
            if (buttonData == ButtonBar.ButtonData.OK_DONE) {
                isValidData = isFinalValidationOk()
                return@setResultConverter if (isValidData) searchCond() else emptyCond
            }
            null
        }

        setupScreen()
        setupComponent()
    }

    private fun setupScreen() {
        val screenSize2d = Screen.getPrimary().visualBounds
        val screenHeight = screenSize2d.height
        val screenWidth = screenSize2d.width
        val window = dialogPane.scene.window
        val stage = window as Stage
        stage.minHeight = 200.0
        stage.minWidth = 300.0
        stage.maxHeight = screenHeight
        stage.maxWidth = screenWidth
    }

    private fun setupComponent() {
        this.title = "Table cell search condition"

        onCloseRequest = EventHandler { dialogEvent: DialogEvent -> doFinalConfirmation(dialogEvent) }

        gridPane.hgap = GAP_H
        gridPane.vgap = GAP_V
        gridPane.children.clear()
        gridPane.add(textFieldSearchText,0,0)
        gridPane.add(checkBoxSelectedColumn, 0,1)
        gridPane.add(checkBoxCase, 0,2)
        gridPane.add(checkBoxRegex, 0,3)
        val wkRowConstraint = RowConstraints()
        wkRowConstraint.minHeight = GRID_MIN_HEIGHT
        gridPane.rowConstraints.add(wkRowConstraint)

        dialogPane.content = gridPane
        dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)

        textFieldSearchText.requestFocus()
    }

    private fun doFinalConfirmation(dialogEvent: DialogEvent) {
        if (buttonData != ButtonBar.ButtonData.OK_DONE) {
            return
        }
        if (!isValidData) {
            dialogEvent.consume()
        }
    }

    private fun isFinalValidationOk(): Boolean {
        val result = !StringUtils.isEmpty(textFieldSearchText.text)
        if (!result) {
            val alert = Alert(Alert.AlertType.ERROR, "Please input search word")
            alert.showAndWait()
        }
        return result
    }

    private fun searchCond(): SearchCondition {
        return SearchCondition(textFieldSearchText.text,
            onlySelectedColumn = checkBoxSelectedColumn.isSelected,
            caseSensitive = checkBoxCase.isSelected,
            searchAsRegEx = checkBoxRegex.isSelected
        )
    }
}
