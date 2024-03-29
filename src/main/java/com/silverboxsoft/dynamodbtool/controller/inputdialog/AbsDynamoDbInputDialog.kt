package com.silverboxsoft.dynamodbtool.controller.inputdialog

import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.control.ScrollPane.ScrollBarPolicy
import javafx.scene.layout.*
import javafx.stage.Screen
import javafx.stage.Stage
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

abstract class AbsDynamoDbInputDialog<R>(dynamoDbRecord: R, dialogTitle: String) : Dialog<R>() {
    private val headGridPane: GridPane

    /*
	 * accessor for inherited class
	 */
    private val gridPane: GridPane
    private val footGridPane: GridPane
    private val scrollPane: ScrollPane
    private val gridAnchorPane: AnchorPane
    protected val addButton: Button
    protected var buttonData: ButtonData = ButtonData.OTHER
    private var isValidData = false
    protected val dynamoDbRecordOrg: R
    var orgBodyAttributeNodeList: List<List<Node>> = ArrayList()
    var addBodyAttributeNodeList: MutableList<List<Node>> = ArrayList()

    /*
	 * for setup
	 */
    abstract val headerWidthList: List<Int>
    abstract val headerLabelList: List<Node>
    abstract val bodyAttributeNodeList: List<List<Node>>

    /*
	 * for screen resize
	 */
    abstract val valueColIndex: Int

    /*
	 * for return dialog result
	 */
    abstract val editedDynamoDbRecord: R
    abstract val emptyAttr: R
    abstract fun isValueChanged(): Boolean
    abstract fun isAddValueRemain(): Boolean

    /*
	 * for add attribute action
	 */
    abstract fun getFooterNodeList(): List<Node>
    abstract fun actAddNewAttribute()

    /*
	 * for final validation
	 */
    abstract val isFinalValidationOk: Boolean

    /*
	 * utility function
	 */
    protected val currentBodyNodeList: List<List<Node>>
        get() {
            val curNodeList: MutableList<List<Node>> = ArrayList()
            curNodeList.addAll(orgBodyAttributeNodeList)
            curNodeList.addAll(addBodyAttributeNodeList)
            return curNodeList
        }

    protected fun getContentLabel(text: String, isBold: Boolean): Label {
        val label = getContentLabel(text)
        label.style = "-fx-font-weight: bold;"
        return label
    }

    protected open fun doFinalConfirmation(dialogEvent: DialogEvent) {
        if (buttonData == ButtonData.OK_DONE) {
            if (isAddValueRemain()){
                val dialog = Dialog<ButtonType>()
                dialog.contentText = "It's seems forgot to add the value. it's OK to close?"
                dialog.dialogPane.buttonTypes.addAll(ButtonType.YES, ButtonType.NO)
                dialog.showAndWait().ifPresent { buttonType: ButtonType ->
                    if (buttonType != ButtonType.YES) {
                        dialogEvent.consume()
                    }
                }
            }
        } else {
            if (isValueChanged() || isAddValueRemain()){
                val dialog = Dialog<ButtonType>()
                dialog.contentText = "Some value is changed. it's OK to cancel?"
                dialog.dialogPane.buttonTypes.addAll(ButtonType.YES, ButtonType.NO)
                dialog.showAndWait().ifPresent { buttonType: ButtonType ->
                    if (buttonType != ButtonType.YES) {
                        dialogEvent.consume()
                    }
                }
            }
            return // close
        }
        if (!isValidData) {
            dialogEvent.consume() // cancel
        }
    }

    /*
	 * class methods
	 */
    private fun setupComponent() {
        headGridPane.hgap = HGAP.toDouble()
        headGridPane.vgap = VGAP.toDouble()
        headGridPane.maxWidth = Double.MAX_VALUE
        headGridPane.minHeight = GRID_MIN_HEIGHT.toDouble()
        headGridPane.alignment = Pos.CENTER
        gridPane.hgap = HGAP.toDouble()
        gridPane.vgap = VGAP.toDouble()
        gridPane.maxWidth = Double.MAX_VALUE
        gridPane.alignment = Pos.CENTER
        gridPane.minHeight = GRID_MIN_HEIGHT.toDouble()
        // gridPane.setStyle();
        gridAnchorPane.children.add(gridPane)
        AnchorPane.setLeftAnchor(gridPane, 0.0)
        AnchorPane.setTopAnchor(gridPane, 5.0)
        AnchorPane.setRightAnchor(gridPane, 0.0)
        AnchorPane.setBottomAnchor(gridPane, 5.0)
        scrollPane.content = gridAnchorPane
        footGridPane.hgap = HGAP.toDouble()
        footGridPane.vgap = VGAP.toDouble()
        footGridPane.maxWidth = Double.MAX_VALUE
        footGridPane.minHeight = GRID_MIN_HEIGHT.toDouble()
        footGridPane.alignment = Pos.CENTER
        // footGridPane.setStyle("-fx-padding-top: 15;-fx-spacing-top: 10;-fx-background-color:green");
        val footBox = HBox()
        val footInset = Insets(5.0, 0.0, 0.0, 0.0)
        HBox.setMargin(footGridPane, footInset)
        footBox.children.add(footGridPane)
        val borderPane = BorderPane()
        borderPane.center = scrollPane
        borderPane.top = headGridPane
        borderPane.bottom = footBox
        addButton.id = ADDBTN_ID
        addButton.onAction = EventHandler { event: ActionEvent? -> actAddNewAttribute() }
        dialogPane.content = borderPane
    }

    private fun innerInitialize() {
        headGridPane.children.clear()
        val labelList = headerLabelList
        for (cIdx in labelList.indices) {
            headGridPane.add(labelList[cIdx], cIdx, 0)
        }
        gridPane.children.clear()
        orgBodyAttributeNodeList = bodyAttributeNodeList
        for (rIdx in orgBodyAttributeNodeList.indices) {
            val nodeList = orgBodyAttributeNodeList[rIdx]
            for (cIdx in nodeList.indices) {
                gridPane.add(nodeList[cIdx], cIdx, rIdx)
            }
            val wkRowConstraint = RowConstraints()
            wkRowConstraint.minHeight = GRID_MIN_HEIGHT.toDouble()
            gridPane.rowConstraints.add(wkRowConstraint)
        }
        updateFooter()
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

    private fun setColumnConstraints() {
        var offset = 0
        for (idx in headerWidthList.indices) {
            val wkWidth = headerWidthList[idx]
            val wkColConstraint = ColumnConstraints()
            wkColConstraint.prefWidth = wkWidth.toDouble()
            gridPane.columnConstraints.add(wkColConstraint)
            headGridPane.columnConstraints.add(wkColConstraint)
            footGridPane.columnConstraints.add(wkColConstraint)
            if (idx != valueColIndex) {
                offset += wkWidth
            }
        }
        scrollPane.hbarPolicy = ScrollBarPolicy.NEVER
        val finalOffset = offset
        val stageSizeListener = ChangeListener { _: ObservableValue<out Number>?, _: Number?, newValue: Number ->
            val newColWidth = newValue.toDouble() - finalOffset - HGAP * (headerWidthList.size - 1)
            gridPane.columnConstraints[valueColIndex].prefWidth = newColWidth
            headGridPane.columnConstraints[valueColIndex].prefWidth = newColWidth
            footGridPane.columnConstraints[valueColIndex].prefWidth = newColWidth
        }
        scrollPane.widthProperty().addListener(stageSizeListener)
    }

    protected fun addAttributeNodeList(newNodeList: List<Node>) {
        val newIdx = orgBodyAttributeNodeList.size + addBodyAttributeNodeList.size
        for (cIdx in newNodeList.indices) {
            gridPane.add(newNodeList[cIdx], cIdx, newIdx)
        }
//        dialogPane.setMaxSize(dialogPane.maxWidth, dialogPane.height + GRID_MIN_HEIGHT.toDouble())
//        gridAnchorPane.maxHeight = gridAnchorPane.maxHeight + GRID_MIN_HEIGHT.toDouble()
//        dialogPane.maxHeight = dialogPane.height + GRID_MIN_HEIGHT.toDouble()
//        val additionalHeight = 30.0
//        dialogPane.resize(dialogPane.width, dialogPane.height  + additionalHeight)
//        gridAnchorPane.resize(gridAnchorPane.width, gridAnchorPane.height + additionalHeight)
        addBodyAttributeNodeList.add(newNodeList)
        updateFooter()
    }

    protected fun updateFooter() {
        footGridPane.children.clear()
        val footerNode = getFooterNodeList()
        for (cIdx in footerNode.indices) {
            footGridPane.add(footerNode[cIdx], cIdx, 0)
        }
    }

    open fun initialize(){
        innerInitialize()
        setColumnConstraints()
        setupScreen()
    }

    companion object {
        const val VALIDATION_MSG_INVALID_VALUE = "Invalid value."
        const val HGAP = 5.0
        const val VGAP = 5.0
        const val FILELD_WIDTH = 300
        const val NAME_COL_WIDTH = 150
        const val DEL_COL_WIDTH = 50
        const val GRID_MIN_HEIGHT = 25
        const val STRFLD_ID_PREFIX = "txtEdit_"
        const val NUMFLD_ID_PREFIX = "numEdit_"
        const val BINFLD_ID_PREFIX = "binEdit_"
        const val VALLBL_ID_PREFIX = "valLabel_"
        const val EDTBTN_ID_PREFIX = "btnEdit_"
        const val ADDBTN_ID = "btnAddAttribute"
        const val DEL_ID_PREFIX = "ckbDel_"
        const val BTN_TITLE = "Edit"
        val NULL_ATTRIBUTE = AttributeValue.builder().nul(true).build()

        /*
        * static function
        */
        // same as DialogPane.createContentLabel
        fun getContentLabel(text: String): Label {
            val label = Label(text)
            label.maxWidth = Double.MAX_VALUE
            label.maxHeight = Double.MAX_VALUE
            label.styleClass.add("content")
            label.isWrapText = false
            label.textOverrun = OverrunStyle.ELLIPSIS
            label.text = text
            return label
        }

        fun getBooleanInput(attrVal: AttributeValue): HBox {
            val hBox = HBox(HGAP)
            val radioButtonTrue = RadioButton()
            val radioButtonFalse = RadioButton()
            radioButtonTrue.text = "TRUE"
            radioButtonFalse.text = "FALSE"
            val toggleGroup = ToggleGroup()
            radioButtonTrue.toggleGroup = toggleGroup
            radioButtonFalse.toggleGroup = toggleGroup
            radioButtonTrue.isSelected = attrVal.bool()
            radioButtonFalse.isSelected = !attrVal.bool()
            hBox.children.addAll(radioButtonTrue, radioButtonFalse)
            return hBox
        }

        val nullViewLabel: Node
            get() = getContentLabel("<null>")

        fun getBooleanValue(hBox: HBox): Boolean {
            val radioButtonTrue = hBox.children[0] as RadioButton
            return radioButtonTrue.isSelected
        }

        fun addUnderlineStyleToNode(node: Node): Node {
            val wkStyle = node.style
            val sbStyle = StringBuilder(wkStyle)
            sbStyle.append("-fx-border-style: none none solid none;")
            sbStyle.append("-fx-border-color: white white lightgray white;")
            sbStyle.append("-fx-border-width: 0 0 1 0;")
            node.style = sbStyle.toString()
            return node
        }
    }

    init {
        this.isResizable = true
        dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)
        this.title = dialogTitle
        onCloseRequest = EventHandler { dialogEvent: DialogEvent -> doFinalConfirmation(dialogEvent) }
        dynamoDbRecordOrg = dynamoDbRecord
        setResultConverter { dialogButton: ButtonType ->
            buttonData = dialogButton.buttonData
            if (buttonData == ButtonData.OK_DONE) {
                isValidData = isFinalValidationOk
                return@setResultConverter if (isValidData) editedDynamoDbRecord else emptyAttr
            }
            null
        }
        headGridPane = GridPane()
        gridPane = GridPane()
        gridAnchorPane = AnchorPane()
        scrollPane = ScrollPane()
        footGridPane = GridPane()
        addButton = Button("Add")
        setupComponent()
        // initialize()
    }
}