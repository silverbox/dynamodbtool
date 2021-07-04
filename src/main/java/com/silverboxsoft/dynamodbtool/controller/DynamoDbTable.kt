package com.silverboxsoft.dynamodbtool.controller

import kotlin.Throws
import java.lang.Exception
import java.io.IOException
import javafx.fxml.FXMLLoader
import javafx.scene.layout.AnchorPane
import javafx.beans.value.ObservableValue
import com.silverboxsoft.dynamodbtool.classes.DynamoDbConnectInfo
import java.net.URISyntaxException
import com.silverboxsoft.dynamodbtool.classes.DynamoDbResult
import com.silverboxsoft.dynamodbtool.classes.DynamoDbConditionJoinType
import com.silverboxsoft.dynamodbtool.classes.DynamoDbCondition
import java.util.HashMap
import com.silverboxsoft.dynamodbtool.utils.DynamoDbUtils
import com.silverboxsoft.dynamodbtool.classes.DynamoDbViewRecord
import com.silverboxsoft.dynamodbtool.classes.DynamoDbConditionType
import javafx.scene.control.Alert.AlertType
import java.util.HashSet
import com.silverboxsoft.dynamodbtool.controller.inputdialog.DynamoDbRecordInputDialog
import javafx.fxml.FXML
import com.silverboxsoft.dynamodbtool.classes.DynamoDbErrorInfo
import com.silverboxsoft.dynamodbtool.dao.PartiQLDao
import com.silverboxsoft.dynamodbtool.dao.ScanDao
import javafx.concurrent.WorkerStateEvent
import java.lang.Thread
import com.silverboxsoft.dynamodbtool.types.CopyModeType
import com.silverboxsoft.dynamodbtool.dao.TableInfoDao
import javafx.scene.control.TableView.TableViewSelectionModel
import com.silverboxsoft.dynamodbtool.dao.PutItemDao
import com.silverboxsoft.dynamodbtool.dao.DeleteItemDao
import com.silverboxsoft.dynamodbtool.dao.QueryDao
import javafx.scene.control.cell.TextFieldTableCell
import javafx.beans.property.ReadOnlyObjectWrapper
import java.lang.RuntimeException
import javafx.concurrent.Task
import javafx.event.ActionEvent
import javafx.event.Event
import javafx.event.EventHandler
import javafx.scene.control.*
import javafx.scene.input.*
import javafx.util.Pair
import software.amazon.awssdk.services.dynamodb.model.*
import software.amazon.awssdk.utils.StringUtils
import java.lang.StringBuilder
import java.util.ArrayList

class DynamoDbTable(private val connInfo: DynamoDbConnectInfo,private val tableName: String, private val dialog: Alert) : AnchorPane() {
    /*
	 * Data Condition
	 */
    @FXML
    var txtFldCondValue: TextField? = null

    @FXML
    var txtAreaPartiql: TextArea? = null

    @FXML
    var radioLoadPartiQL: RadioButton? = null

    @FXML
    var radioLoadKeyValue: RadioButton? = null

    @FXML
    var loadType: ToggleGroup? = null

    /*
	 * info area
	 */
    @FXML
    var paneTableInfo: AnchorPane? = null

    /*
	 * data area
	 */
    @FXML
    var tableResultList: TableView<DynamoDbViewRecord>? = null

    @FXML
    var contextMenuTable: ContextMenu? = null

    @FXML
    var menuItemTableResultListCopy: MenuItem? = null

    @FXML
    var menuItemTableResultListCopyWithQuotation: MenuItem? = null

    @FXML
    var menuItemTableResultListCopyInJsonFormat: MenuItem? = null

    @FXML
    var menuItemTableResultListCellSelectMode: MenuItem? = null

    private var isCellSelectMode = true

    /*
    init block
     */
    private val tableNameDao = TableInfoDao(connInfo)
    val tableInfo: TableDescription = tableNameDao.getTableDescription(tableName)
    var partitionKeyName: String = ""
    var sortKeyName: String? = null
    var hasSortKey = false
    var dynamoDbResult: DynamoDbResult = DynamoDbResult(ArrayList(), tableInfo)

    init {
        try {
            setCurrentTableInfo()
            doQueryDao(DYMMY_COND_VALUE)
            setTable(dynamoDbResult)
            loadType!!.selectedToggleProperty()
                .addListener { observ: ObservableValue<out Toggle?>?, oldVal: Toggle?, newVal: Toggle? -> onLoadTypeChange(null) }
            onLoadTypeChange(null)
        } catch (e: Exception) {
            val alert = Alert(AlertType.ERROR, e.message)
            alert.show()
            e.printStackTrace()
        }
    }

    /*
	 * public action
	 */
    @Throws(Exception::class)
    fun actLoad(ev: ActionEvent?) {
        val errInfo = DynamoDbErrorInfo()
        val task: Task<Boolean> = object : Task<Boolean>() {
            @Throws(Exception::class)
            public override fun call(): Boolean {
                try {
                    if (radioLoadPartiQL!!.isSelected) {
                        val partiQLDao = PartiQLDao(connInfo)
                        dynamoDbResult = partiQLDao.getResult(tableInfo, txtAreaPartiql!!.text)
                    } else {
                        val condValue = txtFldCondValue!!.text
                        if (!StringUtils.isEmpty(condValue)) {
                            doQueryDao(condValue)
                        } else {
                            val dao = ScanDao(connInfo)
                            dynamoDbResult = dao.getResult(tableInfo)
                        }
                    }
                } catch (e: Exception) {
                    errInfo.message = e.message
                    e.printStackTrace()
                    throw e
                }
                return true
            }
        }
        task.onRunning = EventHandler { e: WorkerStateEvent? -> dialog.show() }
        task.onSucceeded = EventHandler { e: WorkerStateEvent? ->
            setTable(dynamoDbResult)
            dialog.hide()
        }
        task.onFailed = EventHandler { e: WorkerStateEvent? ->
            val alert = Alert(AlertType.ERROR, errInfo.message)
            alert.show()
            dialog.hide()
        }
        Thread(task).start()
    }

    @Throws(Exception::class)
    fun actAdd(ev: ActionEvent?) {
        val rec: MutableMap<String, AttributeValue> = HashMap()
        if (dynamoDbResult.recordCount > 0) {
            for (idx in 0 until dynamoDbResult.columnCount) {
                val dbCol = dynamoDbResult.getDynamoDbColumn(idx)
                rec[dbCol.columnName] = dbCol.columnType.initValue
            }
        } else {
            for (dbCol in DynamoDbUtils.Companion.getSortedDynamoDbColumnList(tableInfo)) {
                rec[dbCol.columnName] = dbCol.columnType.initValue
            }
        }
        doAdd(rec)
    }

    @Throws(Exception::class)
    fun actCopyAdd(ev: ActionEvent?) {
        val dataIndex = currentDataIndex
        if (dataIndex < 0) {
            return
        }
        val rec = dynamoDbResult.getRawResItems()[dataIndex]
        doAdd(rec)
    }

    @Throws(URISyntaxException::class)
    fun actUpdate(ev: ActionEvent?) {
        val dataIndex = currentDataIndex
        if (dataIndex < 0) {
            return
        }
        val rec = dynamoDbResult.getRawResItems()[dataIndex]
        doUpdate(dataIndex, rec)
    }

    @Throws(Exception::class)
    fun actDel(ev: ActionEvent?) {
        val dataIndex = currentDataIndex
        if (dataIndex < 0) {
            return
        }
        val rec = dynamoDbResult!!.getRawResItems()[dataIndex]
        doDelete(dataIndex, rec)
    }

    fun hasSortKey(): Boolean {
        return hasSortKey
    }

    /*
	 * event handler
	 */
    @FXML
    protected fun actAddPartitionKeyCond(ev: ActionEvent?) {
        setDefaultPartiQL(PartiQLBaseCondType.PARTITION)
    }

    @FXML
    protected fun actAddAllKeyCond(ev: ActionEvent?) {
        setDefaultPartiQL(PartiQLBaseCondType.PARTITION_AND_SORT)
    }

    @FXML
    protected fun actTableLineCopyToClipBoard(ev: ActionEvent?) {
        copyToClipBoardSub(CopyModeType.TAB)
    }

    @FXML
    protected fun onTableResultListKeyPressed(ev: KeyEvent) {
        if (ev.isControlDown && ev.eventType == KeyEvent.KEY_PRESSED && ev.code == KeyCode.C) {
            actTableLineCopyToClipBoard(null)
        }
    }

    @FXML
    protected fun actTableLineCopyToClipBoardQuotation(ev: ActionEvent?) {
        copyToClipBoardSub(CopyModeType.QUOTATION)
    }

    @FXML
    protected fun actTableLineCopyToClipBoardJson(ev: ActionEvent?) {
        copyToClipBoardSub(CopyModeType.JSON)
    }

    @FXML
    protected fun onMouseClicked(ev: MouseEvent) {
        try {
            if (ev.clickCount >= 2) {
                actUpdate(null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val alert = Alert(AlertType.ERROR, e.message)
            alert.show()
        }
    }

    @FXML
    protected fun actToggleCellSelectMode(ev: ActionEvent?) {
        isCellSelectMode = !isCellSelectMode
        if (isCellSelectMode) {
            menuItemTableResultListCellSelectMode!!.text = "Switch to row select mode"
        } else {
            menuItemTableResultListCellSelectMode!!.text = "Switch to cell select mode"
        }
        tableResultList!!.selectionModel.isCellSelectionEnabled = isCellSelectMode
    }

    @FXML
    protected fun onLoadTypeChange(ev: Event?) {
        txtAreaPartiql!!.isDisable = !radioLoadPartiQL!!.isSelected
        txtFldCondValue!!.isDisable = radioLoadPartiQL!!.isSelected
    }

    /*
	 * normal methods
	 */
    @Throws(URISyntaxException::class)
    private fun setCurrentTableInfo() {
        setCurrentTableInfoVariable()
        setDefaultPartiQL(PartiQLBaseCondType.NONE)
        tableResultList!!.selectionModel.selectionMode = SelectionMode.MULTIPLE
        tableResultList!!.selectionModel.isCellSelectionEnabled = isCellSelectMode
    }

    private fun copyToClipBoardSub(type: CopyModeType) {
        val content = ClipboardContent()
        val selectedModel = tableResultList!!.selectionModel
        if (selectedModel.selectedItems.size == 0) {
            return
        }
        var targetStr: String? = null
        targetStr = if (tableResultList!!.selectionModel.isCellSelectionEnabled) {
            getWholeTableSelectedCellString(selectedModel, type)
        } else {
            getWholeTableSelectedRowString(selectedModel, type)
        }
        if (targetStr != null) {
            content.putString(targetStr)
            Clipboard.getSystemClipboard().setContent(content)
        }
    }

    private fun getWholeTableSelectedCellString(selectedModel: TableViewSelectionModel<DynamoDbViewRecord>,
                                                type: CopyModeType): String {
        val posList = getPositionList(selectedModel)
        val selectedRowStrSb = StringBuilder()
        var oldRow = posList[0].value
        for (position in posList) {
            val wkCol = position.key
            val wkRow = position.value
            val colName = tableResultList!!.columns[wkCol].id
            val wkColIdx = dynamoDbResult!!.getColumnIndexByName(colName)!!
            val wkCellStr = tableResultList!!.items[wkRow].getData()[wkColIdx]
            val cellStr = escapedItemStr(colName, wkCellStr, type)
            if (oldRow != wkRow) {
                selectedRowStrSb.append(lineWrapStr(type, false))
                selectedRowStrSb.append(lineSepStr(type))
                selectedRowStrSb.append(lineWrapStr(type, true))
                selectedRowStrSb.append(cellStr)
            } else if (selectedRowStrSb.isEmpty()) {
                selectedRowStrSb.append(lineWrapStr(type, true))
                selectedRowStrSb.append(cellStr)
            } else {
                selectedRowStrSb.append(itemSepStr(type)).append(cellStr)
            }
            oldRow = position.value
        }
        if (selectedRowStrSb.isNotEmpty()) {
            selectedRowStrSb.append(lineWrapStr(type, false))
        }
        selectedRowStrSb.insert(0, allLineWrapStr(type, true))
        selectedRowStrSb.append(allLineWrapStr(type, false))
        return selectedRowStrSb.toString()
    }

    private fun getWholeTableSelectedRowString(selectedModel: TableViewSelectionModel<DynamoDbViewRecord>,
                                               type: CopyModeType): String {
        val selectedRecords = selectedModel.selectedItems
        if (selectedRecords.size == 0) {
            return ""
        }
        val posList = getPositionList(selectedModel)
        val dupCheckSet: MutableSet<Int> = HashSet()
        val colNameList: MutableList<String> = ArrayList()
        for (posInfo in posList) {
            val colPos = posInfo.key
            if (!dupCheckSet.contains(colPos)) {
                val colName = dynamoDbResult!!.getDynamoDbColumn(colPos.toInt()).columnName
                colNameList.add(colName)
            }
            dupCheckSet.add(colPos)
        }
        val selectedRowStrSb = StringBuilder()
        for (record in selectedRecords) {
            if (selectedRowStrSb.length > 0) {
                selectedRowStrSb.append(lineSepStr(type))
            }
            selectedRowStrSb.append(lineWrapStr(type, true))
            selectedRowStrSb.append(getOneRowString(colNameList, record, type))
            selectedRowStrSb.append(lineWrapStr(type, false))
        }
        return selectedRowStrSb.toString()
    }

    private fun getOneRowString(colNameList: List<String>, record: DynamoDbViewRecord, type: CopyModeType): String {
        val oneRowStrSb = StringBuilder()
        for (colIdx in record.getData().indices) {
            val item = record.getData()[colIdx]
            val wkItemStr = record.getData()[colIdx]
            if (oneRowStrSb.isNotEmpty()) {
                oneRowStrSb.append(itemSepStr(type))
            }
            oneRowStrSb.append(escapedItemStr(colNameList[colIdx], wkItemStr, type))
        }
        return oneRowStrSb.toString()
    }

    private fun allLineWrapStr(type: CopyModeType, isBegin: Boolean): String {
        return if (type == CopyModeType.JSON) {
            if (isBegin) {
                "["
            } else {
                "]"
            }
        } else ""
    }

    private fun lineWrapStr(type: CopyModeType, isBegin: Boolean): String {
        return if (type == CopyModeType.JSON) {
            if (isBegin) {
                "{"
            } else {
                "}"
            }
        } else ""
    }

    private fun lineSepStr(type: CopyModeType): String {
        return if (type == CopyModeType.JSON) {
            ","
        } else if (type == CopyModeType.QUOTATION) {
            "\n"
        } else {
            "\n"
        }
    }

    private fun itemSepStr(type: CopyModeType): String {
        return if (type == CopyModeType.JSON) {
            ","
        } else if (type == CopyModeType.QUOTATION) {
            ","
        } else {
            "\t"
        }
    }

    private fun escapedItemStr(colName: String, orgStr: String, type: CopyModeType): String {
        val retStrSb = StringBuilder()
        if (type == CopyModeType.JSON) {
            retStrSb.append("\"")
            retStrSb.append(colName)
            retStrSb.append("\":\"")
            retStrSb.append(orgStr.replace("\"".toRegex(), "\\\""))
            retStrSb.append("\"")
        } else if (type == CopyModeType.QUOTATION) {
            retStrSb.append("'")
            retStrSb.append(orgStr.replace("'".toRegex(), "''"))
            retStrSb.append("'")
        } else {
            retStrSb.append(orgStr)
        }
        return retStrSb.toString()
    }

    private val currentDataIndex: Int
        private get() {
            val selectedModel = tableResultList!!.selectionModel
            val selRec = selectedModel.selectedItem ?: return -1
            return selRec.getIndex()
        }

    @Throws(URISyntaxException::class)
    private fun doAdd(rec: Map<String, AttributeValue>) {
        val dialog = DynamoDbRecordInputDialog(tableInfo, rec, DynamoDbEditMode.ADD)
        val newRecWk = dialog.showAndWait()
        if (newRecWk.isPresent) {
            val newRec = newRecWk.get()
            val dao = PutItemDao(connInfo)
            dao.putItem(tableInfo, newRec)
            val viewRec = dynamoDbResult!!.addRecord(newRec)
            tableResultList!!.items.add(viewRec)
        }
    }

    @Throws(URISyntaxException::class)
    private fun doUpdate(dataIndex: Int, rec: Map<String, AttributeValue>) {
        val dialog = DynamoDbRecordInputDialog(tableInfo, rec, DynamoDbEditMode.UPD)
        val newRecWk = dialog.showAndWait()
        if (newRecWk.isPresent) {
            val newRec = newRecWk.get()
            val dao = PutItemDao(connInfo)
            dao.putItem(tableInfo, newRec)
            val tableRec = dynamoDbResult!!.updateRecord(dataIndex, newRec)
            tableResultList!!.items[dataIndex] = tableRec
        }
    }

    @Throws(URISyntaxException::class)
    private fun doDelete(dataIndex: Int, rec: Map<String, AttributeValue>) {
        val sb = StringBuilder()
        sb.append("Table name = ").append(tableInfo!!.tableName()).append("\r\n")
        sb.append("Partition key ").append(partitionKeyName).append(" = ") //
                .append(rec!![partitionKeyName].toString()).append("\r\n")
        if (hasSortKey) {
            sb.append("Sort key ").append(sortKeyName) //
                    .append(" = ").append(rec[sortKeyName].toString()).append("\r\n")
        }
        val confirmDialog = Alert(AlertType.CONFIRMATION)
        confirmDialog.headerText = "Delete Information"
        // confirmDialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        // confirmDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        confirmDialog.contentText = sb.toString()
        val retType = confirmDialog.showAndWait().get()
        if (retType == ButtonType.OK) {
            val dao = DeleteItemDao(connInfo)
            dao.deleteItem(tableInfo, rec)
            dynamoDbResult!!.removeRecord(dataIndex)
            tableResultList!!.items.removeAt(dataIndex)
        }
    }

    private fun getPositionList(
            selectedModel: TableViewSelectionModel<DynamoDbViewRecord>): List<Pair<Int, Int>> {
        val selPosList = selectedModel.selectedCells
        val posList: MutableList<Pair<Int, Int>> = ArrayList()
        for (position in selPosList) {
            posList.add(Pair(position.column, position.row))
        }
        return posList.sortedWith(java.util.Comparator { o1, o2 ->
            val rowdiff = o1.value - o2.value
            if (rowdiff != 0) {
                rowdiff
            } else o1.key - o2.key
        })
    }

    private fun setCurrentTableInfoVariable() {
        val keyInfos = tableInfo.keySchema()
        for (k in keyInfos) {
            if (k.keyType() == KeyType.HASH) {
                partitionKeyName = k.attributeName()
            } else if (k.keyType() == KeyType.RANGE) {
                sortKeyName = k.attributeName()
                hasSortKey = true
            }
        }
    }

    @Throws(URISyntaxException::class)
    private fun doQueryDao(condValue: String) {
        val dao = QueryDao(connInfo)
        val conditionList: MutableList<DynamoDbCondition> = ArrayList()
        val cond = DynamoDbCondition(partitionKeyName, DynamoDbConditionType.EQUAL, condValue)
        conditionList.add(cond)
        dynamoDbResult = dao.getResult(tableInfo, DynamoDbConditionJoinType.AND, conditionList)
    }

    private fun setDefaultPartiQL(type: PartiQLBaseCondType) {
        val defPartQL = String.format("select * from \"%1\$s\"", tableName)
        val sbPartiQL = StringBuilder(defPartQL)
        if (type != PartiQLBaseCondType.NONE) {
            sbPartiQL.append(" where ").append(partitionKeyName).append(" = ?")
            if (hasSortKey && type == PartiQLBaseCondType.PARTITION_AND_SORT) {
                sbPartiQL.append(" and ").append(sortKeyName).append(" = ?")
            }
        }
        txtAreaPartiql!!.text = sbPartiQL.toString()
    }

    private fun setTable(result: DynamoDbResult) {
        tableResultList!!.items.clear()
        tableResultList!!.columns.clear()
        for (colIdx in 0 until result.columnCount) {
            val columnName = result!!.getDynamoDbColumn(colIdx).columnName
            val dataCol = getTableColumn(columnName, colIdx)
            dataCol.setCellFactory(TextFieldTableCell.forTableColumn())
            dataCol.maxWidth = TBL_COL_MAX_WIDTH.toDouble()
            tableResultList!!.columns.add(dataCol)
        }
        tableResultList!!.items.addAll(result.resultItems)
    }

    private fun getTableColumn(columnName: String, finalColIdx: Int): TableColumn<DynamoDbViewRecord?, String> {
        val dataCol = TableColumn<DynamoDbViewRecord?, String>(columnName)
        dataCol.setCellValueFactory { param: TableColumn.CellDataFeatures<DynamoDbViewRecord?, String>
            -> ReadOnlyObjectWrapper(param.value!!.getData()[finalColIdx]) }
        dataCol.id = columnName
        return dataCol
    }

    private enum class PartiQLBaseCondType {
        NONE, PARTITION, PARTITION_AND_SORT
    }

    companion object {
        private const val TBL_COL_MAX_WIDTH = 1000
        private const val DYMMY_COND_VALUE = "\t\r\n"
    }

    init {
        val fxmlLoader = FXMLLoader(javaClass.getResource(
                "javafx/DynamoDbTable.fxml"))
        fxmlLoader.setRoot(this)
        fxmlLoader.setController(this)
        try {
            fxmlLoader.load<Any>()
        } catch (exception: IOException) {
            val alert = Alert(AlertType.ERROR, exception.message)
            alert.show()
            throw RuntimeException(exception)
        }
    }
}