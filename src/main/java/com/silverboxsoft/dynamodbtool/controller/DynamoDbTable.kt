package com.silverboxsoft.dynamodbtool.controller

import com.silverboxsoft.dynamodbtool.classes.*
import kotlin.Throws
import java.lang.Exception
import java.io.IOException
import javafx.fxml.FXMLLoader
import javafx.scene.layout.AnchorPane
import javafx.beans.value.ObservableValue
import java.net.URISyntaxException
import javafx.scene.control.Alert.AlertType
import com.silverboxsoft.dynamodbtool.controller.inputdialog.DynamoDbRecordInputDialog
import javafx.fxml.FXML
import com.silverboxsoft.dynamodbtool.consts.Messages
import com.silverboxsoft.dynamodbtool.dao.*
import java.lang.Thread
import com.silverboxsoft.dynamodbtool.types.CopyModeType
import javafx.scene.control.TableView.TableViewSelectionModel
import com.silverboxsoft.dynamodbtool.utils.DynamoDbUtils.Companion.getIndexInfo
import javafx.scene.control.cell.TextFieldTableCell
import javafx.beans.property.ReadOnlyObjectWrapper
import java.lang.RuntimeException
import javafx.concurrent.Task
import javafx.event.EventHandler
import javafx.scene.control.*
import javafx.scene.input.*
import javafx.stage.Modality
import javafx.util.Pair
import software.amazon.awssdk.services.dynamodb.model.*
import software.amazon.awssdk.utils.StringUtils
import java.lang.StringBuilder
import java.util.*
import kotlin.streams.toList

class DynamoDbTable(private val connInfo: DynamoDbConnectInfo, private val tableName: String, private val loadingDialog: Alert) : AnchorPane() {
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
    var menuItemTableResultListCopyToClipBoardWhereCondition: MenuItem? = null

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
    var mainKeyInfo: DynamoDbIndex = DynamoDbIndex("", "", "")
    var gsiInfoList: List<DynamoDbIndex> = ArrayList()
    var lsiInfoList: List<DynamoDbIndex> = ArrayList()
    var dynamoDbResult: DynamoDbResult = DynamoDbResult(ArrayList(), tableInfo)
    private var columnList: List<DynamoDbColumn> = dynamoDbResult.getDynamoDbColumnList()
    private var searchCondition = SearchCondition("",
        onlySelectedColumn = false,
        caseSensitive = false,
        searchAsRegEx = false
    )
    private var matchPosList: MutableList<Pair<Int, Int>> = ArrayList()

    fun initialize() {
        try {
            setCurrentTableInfo()
            doQueryDao(DUMMY_COND_VALUE)
            setTable(dynamoDbResult)
            loadType!!.selectedToggleProperty()
                .addListener { observ: ObservableValue<out Toggle?>?, oldVal: Toggle?, newVal: Toggle? -> onLoadTypeChange() }
            onLoadTypeChange()
        } catch (e: Exception) {
            val alert = Alert(AlertType.ERROR, e.message)
            alert.show()
            e.printStackTrace()
        }
    }

    fun getTableView(): TableView<DynamoDbViewRecord> {
        return tableResultList!!
    }

    /*
	 * public action
	 */
    @Throws(Exception::class)
    fun actLoad() {
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
        task.onRunning = EventHandler { loadingDialog.show() }
        task.onSucceeded = EventHandler {
            setTable(dynamoDbResult)
            loadingDialog.hide()
        }
        task.onFailed = EventHandler {
            val alert = Alert(AlertType.ERROR, errInfo.message)
            alert.show()
            loadingDialog.hide()
        }
        Thread(task).start()
    }

    @Throws(Exception::class)
    fun actAdd() {
        val rec: MutableMap<String, AttributeValue> = HashMap()
        if (dynamoDbResult.recordCount > 0) {
            for (idx in 0 until dynamoDbResult.columnCount) {
                val dbCol = dynamoDbResult.getDynamoDbColumn(idx)
                rec[dbCol.columnName] = dbCol.columnType.initValue
            }
        } else {
            for (dbCol in columnList) {
                rec[dbCol.columnName] = dbCol.columnType.initValue
            }
        }
        doAdd(rec)
    }

    @Throws(Exception::class)
    fun actCopyAdd() {
        if (!checkOneRowSelected()) {
            return
        }
        val rec = dynamoDbResult.getRawResItems()[currentDataIndex]
        doAdd(rec)
    }

    @Throws(URISyntaxException::class)
    fun actUpdate() {
        val dataIndex = currentDataIndex
        if (dataIndex < 0) {
            return
        }
        val rec = dynamoDbResult.getRawResItems()[dataIndex]
        doUpdate(dataIndex, rec)
    }

    @Throws(Exception::class)
    fun actDel() {
        if (!checkOneRowSelected()) {
            return
        }
        val rec = dynamoDbResult.getRawResItems()[currentDataIndex]
        doDelete(currentDataIndex, rec)
    }

    @Throws(Exception::class)
    fun actBulkDel() {
        if (currentDataIndex < 0) {
            val alert = Alert(AlertType.ERROR, Messages.ERR_MSG_CLIPBOARD_NO_ROW_SELECTED)
            alert.show()
            return
        }
        doBulkDelete()
    }

    fun hasSortKey(): Boolean {
        return mainKeyInfo.sortKey != null
    }

    private fun checkOneRowSelected(): Boolean {
        val selectedRecords = tableResultList!!.selectionModel.selectedItems
        if (currentDataIndex < 0 || selectedRecords.size > 1) {
            val alert = Alert(AlertType.ERROR, Messages.ERR_MSG_CLIPBOARD_MULTI_ROW_SELECTED)
            alert.show()
            return false
        }
        return true
    }

    /*
	 * event handler
	 */
    @FXML
    private fun actAddPartitionKeyCond() {
        setDefaultPartiQL(PartiQLBaseCondType.PARTITION)
    }

    @FXML
    private fun actAddAllKeyCond() {
        setDefaultPartiQL(PartiQLBaseCondType.PARTITION_AND_SORT)
    }

    @FXML
    private fun actTableLineCopyToClipBoard() {
        copyToClipBoardSub(CopyModeType.TAB)
    }

    @FXML
    private fun actTableLineCopyToClipBoardWhereCondition() {
        if (tableResultList!!.selectionModel.isCellSelectionEnabled) {
            copyToClipBoardSub(CopyModeType.WHERE)
        } else {
            val alert = Alert(AlertType.ERROR, Messages.ERR_MSG_CLIPBOARD_NOT_CELL_SELECT)
            alert.show()
        }
    }

    @FXML
    private fun actTableLineCopyToClipBoardJson() {
        copyToClipBoardSub(CopyModeType.JSON)
    }

    @FXML
    private fun actTableLineSearch() {
        searchWord()
    }

    @FXML
    private fun onTableResultListKeyPressed(ev: KeyEvent) {
        if (ev.isControlDown && ev.eventType == KeyEvent.KEY_PRESSED && ev.code == KeyCode.C) {
            actTableLineCopyToClipBoard()
        }
    }

    @FXML
    private fun onMouseClicked(ev: MouseEvent) {
        try {
            if (ev.clickCount >= 2) {
                actUpdate()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val alert = Alert(AlertType.ERROR, e.message)
            alert.show()
        }
    }

    @FXML
    private fun actToggleCellSelectMode() {
        isCellSelectMode = !isCellSelectMode
        if (isCellSelectMode) {
            menuItemTableResultListCellSelectMode!!.text = "Switch to row select mode"
        } else {
            menuItemTableResultListCellSelectMode!!.text = "Switch to cell select mode"
        }
        tableResultList!!.selectionModel.isCellSelectionEnabled = isCellSelectMode
    }

    @FXML
    private fun onLoadTypeChange() {
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
        if (type == CopyModeType.WHERE) {
            if  (!tableResultList!!.selectionModel.isCellSelectionEnabled) {
                val alert = Alert(AlertType.ERROR, Messages.ERR_MSG_CLIPBOARD_NOT_CELL_SELECT)
                alert.show()
                return
            }
            if (getSelectedColumnList().size > 1){
                val alert = Alert(AlertType.ERROR, Messages.ERR_MSG_CLIPBOARD_MULTI_CELL_SELECTED)
                alert.show()
                return
            }
        }
        var targetStr = if (isCellSelectMode) {
            getWholeTableSelectedCellString(selectedModel, type)
        } else {
            getWholeTableSelectedRowString(selectedModel, type)
        }
        if (type == CopyModeType.WHERE) {
            val sb = StringBuilder(" where ")
            getSelectedColumnList().forEach {
                colIdx ->
                run {
                    val colName = tableResultList!!.columns[colIdx].id
                    sb.append(colName)
                }
            }
            sb.append(" in (").append(targetStr).append(")")
            targetStr = sb.toString()
        }
        content.putString(targetStr)
        Clipboard.getSystemClipboard().setContent(content)
    }

    private fun searchWord() {
        val dialog = SearchConditionDialog(searchCondition, "Table cell condition")
        val newConditionWk = dialog.showAndWait()
        if (!newConditionWk.isPresent) {
            return
        }
        val newCondition = newConditionWk.get()
        val targetColIdxSet: Set<Int> = getSearchTargetColIdxList(newCondition.onlySelectedColumn)
        val caredSearchWord = if (newCondition.caseSensitive) newCondition.searchWord else newCondition.searchWord.lowercase(
            Locale.getDefault()
        )
        val regex = Regex(caredSearchWord)

        tableResultList!!.selectionModel.clearSelection()
        matchPosList.clear()
        for (visibleColIdx in targetColIdxSet) {
            for (rIdx in (0 until dynamoDbResult.recordCount)) {
                val colName = tableResultList!!.columns[visibleColIdx].id
                val dbColIdx = dynamoDbResult.getColumnIndexByName(colName)
                val wkCellStr = tableResultList!!.items[rIdx].getData()[dbColIdx]
                val chkStr = if (newCondition.caseSensitive) wkCellStr else wkCellStr.lowercase(Locale.getDefault())
                val isMatch =
                    if (newCondition.searchAsRegEx) regex.containsMatchIn(chkStr)
                    else wkCellStr.contains(newCondition.searchWord, !newCondition.caseSensitive)
                if (isMatch) {
                    tableResultList!!.selectionModel.select(rIdx, tableResultList!!.columns[visibleColIdx])
                    matchPosList.add(Pair(visibleColIdx, rIdx))
                }
            }
        }
        searchCondition = newCondition
        if (matchPosList.size > 0) {
            showSearchResultDialog()
        }
    }

    private fun showSearchResultDialog() {
        val srDialog = SearchResultJumpDialog(this)
        srDialog.initModality(Modality.NONE)
        srDialog.posList = matchPosList
        srDialog.show()
    }

    fun jumpToSearchResultCell(index: Int) {
        val pos: Pair<Int, Int> = matchPosList[index]
        val fPos = TablePosition(tableResultList, pos.value, tableResultList!!.columns[pos.key])
        tableResultList!!.focusModel.focus(fPos)
        tableResultList!!.scrollTo(pos.value)
        tableResultList!!.scrollToColumnIndex(pos.key)
    }

    fun getSearchResultCellStr(index: Int): String {
        val pos: Pair<Int, Int> = matchPosList[index]
        return tableResultList!!.items[pos.value].getData()[pos.key]
    }

    /**
     * return visible column index(not db column index)
     */
    private fun getSearchTargetColIdxList(onlySelectedCol: Boolean): Set<Int> {
        val targetColIdxSet: MutableSet<Int> = HashSet()
        val selectedModel = tableResultList!!.selectionModel
        if (onlySelectedCol && selectedModel.selectedItems.size > 0) {
            val posList = getPositionList(selectedModel)
            for (position in posList) {
                val wkCol = position.key
//                val colName = tableResultList!!.columns[wkCol].id
//                val wkColIdx = dynamoDbResult!!.getColumnIndexByName(colName)!!
                targetColIdxSet.add(wkCol)
            }
        } else {
            val allColIdxList = (0 until tableResultList!!.columns.size - 1)
            targetColIdxSet.addAll(allColIdxList)
        }
        return targetColIdxSet
    }

    private fun getWholeTableSelectedCellString(selectedModel: TableViewSelectionModel<DynamoDbViewRecord>,
                                                copyMode: CopyModeType): String {
        val posList = getPositionList(selectedModel)
        val selectedRowStrSb = StringBuilder()
        var oldRow = posList[0].value
        for (position in posList) {
            val wkCol = position.key
            val wkRow = position.value
            val colName = tableResultList!!.columns[wkCol].id
            val wkColIdx = dynamoDbResult.getColumnIndexByName(colName)
            val colType = dynamoDbResult.getDynamoDbColumn(wkColIdx).columnType
            val wkCellStr = tableResultList!!.items[wkRow].getData()[wkColIdx]
            val cellStr = escapedItemStr(colName, colType, wkCellStr, copyMode)
            when {
                oldRow != wkRow -> {
                    selectedRowStrSb.append(lineWrapStr(copyMode, false))
                    selectedRowStrSb.append(lineSepStr(copyMode))
                    selectedRowStrSb.append(lineWrapStr(copyMode, true))
                    selectedRowStrSb.append(cellStr)
                }
                selectedRowStrSb.isEmpty() -> {
                    selectedRowStrSb.append(lineWrapStr(copyMode, true))
                    selectedRowStrSb.append(cellStr)
                }
                else -> {
                    selectedRowStrSb.append(itemSepStr(copyMode)).append(cellStr)
                }
            }
            oldRow = position.value
        }
        if (selectedRowStrSb.isNotEmpty()) {
            selectedRowStrSb.append(lineWrapStr(copyMode, false))
        }
        selectedRowStrSb.insert(0, allLineWrapStr(copyMode, true))
        selectedRowStrSb.append(allLineWrapStr(copyMode, false))
        return selectedRowStrSb.toString()
    }

    private fun getWholeTableSelectedRowString(selectedModel: TableViewSelectionModel<DynamoDbViewRecord>,
                                               copyMode: CopyModeType): String {
        val selectedRecords = selectedModel.selectedItems
        if (selectedRecords.size == 0) {
            return ""
        }
        val colNameList: MutableList<String> = dynamoDbResult.getDynamoDbColumnList().stream().map { column: DynamoDbColumn ->
            column.columnName
        }.toList() as MutableList<String>

        val selectedRowStrSb = StringBuilder()
        for (record in selectedRecords) {
            if (selectedRowStrSb.isNotEmpty()) {
                selectedRowStrSb.append(lineSepStr(copyMode))
            }
            selectedRowStrSb.append(lineWrapStr(copyMode, true))
            selectedRowStrSb.append(getOneRowString(colNameList, record, copyMode))
            selectedRowStrSb.append(lineWrapStr(copyMode, false))
        }
        selectedRowStrSb.insert(0, allLineWrapStr(copyMode, true))
        selectedRowStrSb.append(allLineWrapStr(copyMode, false))
        return selectedRowStrSb.toString()
    }

    private fun getOneRowString(colNameList: List<String>, record: DynamoDbViewRecord, copyMode: CopyModeType): String {
        val oneRowStrSb = StringBuilder()
        for (colIdx in record.getData().indices) {
            val wkItemStr = record.getData()[colIdx]
            if (oneRowStrSb.isNotEmpty()) {
                oneRowStrSb.append(itemSepStr(copyMode))
            }
            val colName = colNameList[colIdx]
            val wkColIdx = dynamoDbResult.getColumnIndexByName(colName)
            val colType = dynamoDbResult.getDynamoDbColumn(wkColIdx).columnType
            oneRowStrSb.append(escapedItemStr(colName, colType, wkItemStr, copyMode))
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

    private fun lineSepStr(copyMode: CopyModeType): String {
        return when(copyMode){
            CopyModeType.JSON -> ","
            CopyModeType.WHERE -> ","
            else -> "\n"
        }
    }

    private fun itemSepStr(copyMode: CopyModeType): String {
        return when(copyMode){
            CopyModeType.JSON -> ","
            CopyModeType.WHERE -> ","
            else -> "\t"
        }
    }

    private fun escapedItemStr(colName: String, columnType: DynamoDbColumnType, orgStr: String, copyMode: CopyModeType): String {
        val retStrSb = StringBuilder()
        when(copyMode){
            CopyModeType.JSON -> {
                retStrSb.append("\"")
                retStrSb.append(colName)
                retStrSb.append("\":\"")
                retStrSb.append(orgStr.replace("\"".toRegex(), "\\\""))
                retStrSb.append("\"")
            }
            CopyModeType.WHERE -> {
                val quotation = if (columnType == DynamoDbColumnType.STRING) "'" else ""
                retStrSb.append(quotation)
                retStrSb.append(orgStr.replace("'".toRegex(), "''"))
                retStrSb.append(quotation)
            }
            else -> retStrSb.append(orgStr)
        }
        return retStrSb.toString()
    }

    private val currentDataIndex: Int
        get() {
            val selectedModel = tableResultList!!.selectionModel
            val selRec = selectedModel.selectedItem?: return -1
            return selRec.getIndex()
        }

    @Throws(URISyntaxException::class)
    private fun doAdd(rec: Map<String, AttributeValue>) {
        val inputDialog = DynamoDbRecordInputDialog(tableInfo, rec, DynamoDbEditMode.ADD)
        inputDialog.initialize()
        val newRecWk = inputDialog.showAndWait()
        if (!newRecWk.isPresent) {
            return
        }
        val errInfo = DynamoDbErrorInfo()
        val task: Task<Boolean> = object : Task<Boolean>() {
            @Throws(Exception::class)
            public override fun call(): Boolean {
                try {
                    val newRec = newRecWk.get()
                    val dao = PutItemDao(connInfo)
                    dao.putItem(tableInfo, newRec)
                    val viewRec = dynamoDbResult.addRecord(newRec)
                    tableResultList!!.items.add(viewRec)
                } catch (e: Exception) {
                    errInfo.message = e.message
                    e.printStackTrace()
                    throw e
                }
                return true
            }
        }
        task.onRunning = EventHandler {
            loadingDialog.show()
        }
        task.onSucceeded = EventHandler {
            setTable(dynamoDbResult)
            loadingDialog.hide()
        }
        task.onFailed = EventHandler {
            val alert = Alert(AlertType.ERROR, errInfo.message)
            alert.show()
            loadingDialog.hide()
        }
        Thread(task).start()
    }

    @Throws(URISyntaxException::class)
    private fun doUpdate(dataIndex: Int, rec: Map<String, AttributeValue>) {
        val inputDialog = DynamoDbRecordInputDialog(tableInfo, rec, DynamoDbEditMode.UPD)
        inputDialog.initialize()
        val newRecWk = inputDialog.showAndWait()
        if (!newRecWk.isPresent) {
            return
        }

        val errInfo = DynamoDbErrorInfo()
        val task: Task<Boolean> = object : Task<Boolean>() {
            @Throws(Exception::class)
            public override fun call(): Boolean {
                try {
                    val newRec = newRecWk.get()
                    val dao = UpdateItemDao(connInfo)
                    dao.updateItem(tableInfo, newRec)
                    val tableRec = dynamoDbResult.updateRecord(dataIndex, newRec)
                    tableResultList!!.items[dataIndex] = tableRec
                } catch (e: Exception) {
                    errInfo.message = e.message
                    e.printStackTrace()
                    throw e
                }
                return true
            }
        }
        task.onRunning = EventHandler {
            loadingDialog.show()
        }
        task.onSucceeded = EventHandler {
            setTable(dynamoDbResult)
            loadingDialog.hide()
        }
        task.onFailed = EventHandler {
            val alert = Alert(AlertType.ERROR, errInfo.message)
            alert.show()
            loadingDialog.hide()
        }
        Thread(task).start()
    }

    @Throws(URISyntaxException::class)
    private fun doDelete(dataIndex: Int, rec: Map<String, AttributeValue>) {
        val sb = StringBuilder()
        sb.append("Table name = ").append(tableInfo.tableName()).append("\r\n")
        sb.append("Partition key ").append(mainKeyInfo.hashKey).append(" = ") //
                .append(rec[mainKeyInfo.hashKey].toString()).append("\r\n")
        if (hasSortKey()) {
            sb.append("Sort key ").append(mainKeyInfo.sortKey) //
                    .append(" = ").append(rec[mainKeyInfo.sortKey].toString()).append("\r\n")
        }
        val confirmDialog = Alert(AlertType.CONFIRMATION)
        confirmDialog.headerText = "Delete Information"
        confirmDialog.contentText = sb.toString()
        val retType = confirmDialog.showAndWait().get()
        if (retType != ButtonType.OK) {
            return
        }

        val errInfo = DynamoDbErrorInfo()
        val task: Task<Boolean> = object : Task<Boolean>() {
            @Throws(Exception::class)
            public override fun call(): Boolean {
                try {
                    val dao = DeleteItemDao(connInfo)
                    dao.deleteItem(tableInfo, rec)
                    dynamoDbResult.removeRecord(dataIndex)
                    tableResultList!!.items.removeAt(dataIndex)
                } catch (e: Exception) {
                    errInfo.message = e.message
                    e.printStackTrace()
                    throw e
                }
                return true
            }
        }
        task.onRunning = EventHandler {
            loadingDialog.show()
        }
        task.onSucceeded = EventHandler {
            setTable(dynamoDbResult)
            loadingDialog.hide()
        }
        task.onFailed = EventHandler {
            val alert = Alert(AlertType.ERROR, errInfo.message)
            alert.show()
            loadingDialog.hide()
        }
        Thread(task).start()
    }

    private fun doBulkDelete() {
        val selectedModel = tableResultList!!.selectionModel
        val posList = getPositionList(selectedModel)

        val confirmDialog = Alert(AlertType.CONFIRMATION)
        confirmDialog.headerText = "Delete Information"
        confirmDialog.contentText = getSelectedKeyInformationString(posList)
        val retType = confirmDialog.showAndWait().get()
        if (retType != ButtonType.OK) {
            return
        }

        val errInfo = DynamoDbErrorInfo()
        val task: Task<Boolean> = object : Task<Boolean>() {
            @Throws(Exception::class)
            public override fun call(): Boolean {
                try {
                    val dao = DeleteItemDao(connInfo)
                    val dataIndexList = posList.stream().map{ pos ->  tableResultList!!.items[pos.value].getIndex() }.toList().sortedDescending()
                    for (dataIndex in dataIndexList) {
                        val rec = dynamoDbResult.getRawResItems()[dataIndex]
                        dao.deleteItem(tableInfo, rec)
                        dynamoDbResult.removeRecord(dataIndex)
                        tableResultList!!.items.removeAt(dataIndex)
                    }
                } catch (e: Exception) {
                    errInfo.message = e.message
                    e.printStackTrace()
                    throw e
                }
                return true
            }
        }
        task.onRunning = EventHandler {
            loadingDialog.show()
        }
        task.onSucceeded = EventHandler {
            setTable(dynamoDbResult)
            loadingDialog.hide()
        }
        task.onFailed = EventHandler {
            val alert = Alert(AlertType.ERROR, errInfo.message)
            alert.show()
            loadingDialog.hide()
        }
        Thread(task).start()
    }

    private fun getSelectedKeyInformationString(posList: List<Pair<Int, Int>>): String {
        val sb = StringBuilder()
        sb.append("Table name = ").append(tableInfo.tableName()).append("\r\n")
        sb.append("Target data key information (").append(mainKeyInfo.hashKey)
        if (mainKeyInfo.sortKey != null) {
            sb.append(", ").append(mainKeyInfo.sortKey)
        }
        sb.append(") = [").append("\r\n")
        var count = 0
        val limitSize = 10
        for (pos in posList) {
            val wkRow = pos.value
            val priKeyColIdx = dynamoDbResult.getColumnIndexByName(mainKeyInfo.hashKey)
            val rec = tableResultList!!.items[wkRow].getData()
            val priKeyStr = rec[priKeyColIdx]
            sb.append(priKeyStr)
            if (mainKeyInfo.sortKey != null) {
                val sortKeyColIdx = dynamoDbResult.getColumnIndexByName(mainKeyInfo.sortKey!!)
                val sortKeyStr = rec[sortKeyColIdx]
                sb.append(", ").append(sortKeyStr)
            }
            sb.append("\r\n")

            count += 1
            if (count >= limitSize) {
                break
            }
        }
        sb.append("]")
        if (posList.size > limitSize) {
            sb.append("\r\n").append("... and more ").append(posList.size - limitSize).append(" record(s).")
        }
        return sb.toString()
    }

    private fun getPositionList(
            selectedModel: TableViewSelectionModel<DynamoDbViewRecord>): List<Pair<Int, Int>> {
        val selPosList = selectedModel.selectedCells
        val posList: MutableList<Pair<Int, Int>> = ArrayList()
        for (position in selPosList) {
            posList.add(Pair(position.column, position.row))
        }
        return posList.sortedWith { o1, o2 ->
            val rowDiff = o1.value - o2.value
            if (rowDiff != 0) {
                rowDiff
            } else o1.key - o2.key
        }
    }

    private fun getSelectedColumnList(): HashSet<Int> {
        val selectedModel = tableResultList!!.selectionModel
        val colIdxSet = HashSet<Int>()
        val selPosList = selectedModel.selectedCells
        for (position in selPosList) {
            colIdxSet.add(position.column)
        }
        return colIdxSet
    }

    private fun setCurrentTableInfoVariable() {
        val keyInfo = tableInfo.keySchema()

        mainKeyInfo = getIndexInfo(tableInfo.tableName(), keyInfo)

        gsiInfoList = ArrayList()
        for (gsiDesc in tableInfo.globalSecondaryIndexes()) {
            val indexName = gsiDesc.indexName()
            val gsiKeyInfo = gsiDesc.keySchema()
            gsiInfoList = gsiInfoList + getIndexInfo(indexName, gsiKeyInfo)
        }

        lsiInfoList = ArrayList()
        for (lsiDesc in tableInfo.localSecondaryIndexes()) {
            val indexName = lsiDesc.indexName()
            val gsiKeyInfo = lsiDesc.keySchema()
            lsiInfoList = lsiInfoList + getIndexInfo(indexName, gsiKeyInfo)
        }
    }

    @Throws(URISyntaxException::class)
    private fun doQueryDao(condValue: String) {
        val dao = QueryDao(connInfo)
        val conditionList: MutableList<DynamoDbCondition> = ArrayList()
        val cond = DynamoDbCondition(mainKeyInfo.hashKey, DynamoDbConditionType.EQUAL, condValue)
        conditionList.add(cond)
        dynamoDbResult = dao.getResult(tableInfo, DynamoDbConditionJoinType.AND, conditionList)
    }

    private fun setDefaultPartiQL(type: PartiQLBaseCondType) {
        val defPartQL = String.format("select * from \"%1\$s\"", tableName)
        val sbPartiQL = StringBuilder(defPartQL)
        if (type != PartiQLBaseCondType.NONE) {
            sbPartiQL.append(" where ").append(mainKeyInfo.hashKey).append(" = ?")
            if (hasSortKey() && type == PartiQLBaseCondType.PARTITION_AND_SORT && mainKeyInfo.sortKey != null) {
                sbPartiQL.append(" and ").append(mainKeyInfo.sortKey).append(" = ?")
            }
        }
        txtAreaPartiql!!.text = sbPartiQL.toString()
    }

    private fun setTable(result: DynamoDbResult) {
        tableResultList!!.items.clear()
        tableResultList!!.columns.clear()
        for (colIdx in 0 until result.columnCount) {
            val columnName = result.getDynamoDbColumn(colIdx).columnName
            val dataCol = getTableColumn(columnName, colIdx)
            dataCol.cellFactory = TextFieldTableCell.forTableColumn()
            dataCol.maxWidth = TBL_COL_MAX_WIDTH.toDouble()
            tableResultList!!.columns.add(dataCol)
        }
        tableResultList!!.items.addAll(result.resultItems)
    }

    private fun getTableColumn(columnName: String, finalColIdx: Int): TableColumn<DynamoDbViewRecord, String> {
        val dataCol = TableColumn<DynamoDbViewRecord, String>(columnName)
        dataCol.setCellValueFactory { param: TableColumn.CellDataFeatures<DynamoDbViewRecord, String>
            -> ReadOnlyObjectWrapper(param.value!!.getData()[finalColIdx]) }
        dataCol.id = columnName
        return dataCol
    }

    private enum class PartiQLBaseCondType {
        NONE, PARTITION, PARTITION_AND_SORT
    }

    companion object {
        private const val TBL_COL_MAX_WIDTH = 1000
        private const val DUMMY_COND_VALUE = "\t\r\n"
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