package com.silverboxsoft.dynamodbtool.controller

import kotlin.Throws
import java.lang.Exception
import javafx.scene.layout.VBox
import com.silverboxsoft.dynamodbtool.classes.DynamoDbConnectInfo
import java.net.URISyntaxException
import com.silverboxsoft.dynamodbtool.classes.DynamoDbConnectType
import com.silverboxsoft.dynamodbtool.fxmodel.TableNameCondType
import javafx.scene.control.Alert.AlertType
import javafx.fxml.FXML
import com.silverboxsoft.dynamodbtool.classes.DynamoDbErrorInfo
import javafx.concurrent.WorkerStateEvent
import java.lang.Thread
import javafx.fxml.Initializable
import java.util.ResourceBundle
import com.silverboxsoft.dynamodbtool.dao.TableListDao
import javafx.concurrent.Task
import javafx.event.EventHandler
import javafx.scene.control.*
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import java.lang.StringBuilder
import java.net.URL

class DynamoDbToolController : Initializable {
    @FXML
    var vboxRoot: VBox? = null

    /*
	 * Connection kind
	 */
    @FXML
    var rbConnectAWS: RadioButton? = null

    @FXML
    var rbConnectLocalDynamoDB: RadioButton? = null

    @FXML
    var txtFldLocalEndpoint: TextField? = null

    @FXML
    var miAddAllKeyCond: MenuItem? = null

    @FXML
    var miAddPartitionKeyCond: MenuItem? = null

    /*
	 * Table Name Condition
	 */
    @FXML
    var txtFldTableNameCond: TextField? = null

    @FXML
    var cmbTableNameCond: ComboBox<String>? = null

    @FXML
    var lvTableList: ListView<String>? = null

    /*
	 * 
	 */
    @FXML
    var tabPaneTable: TabPane? = null

    /*
	 * event handler
	 */
    private var loadingDialog: Alert = Alert(AlertType.INFORMATION)
    override fun initialize(location: URL, resources: ResourceBundle?) {
        initCmb()
        initLoadDialog()
    }

    @FXML
    fun actTableListLoad() {
        val errInfo = DynamoDbErrorInfo()
        val task: Task<Boolean> = object : Task<Boolean>() {
            @Throws(Exception::class)
            public override fun call(): Boolean {
                try {
                    val dao = TableListDao(connectInfo)
                    val conditionType: TableNameCondType = TableNameCondType.Companion.getByName(cmbTableNameCond!!.value)
                    lvTableList!!.items.clear()
                    lvTableList!!.items.addAll(dao.getTableList(txtFldTableNameCond!!.text, conditionType))
                } catch (e: Exception) {
                    errInfo.message = e.message
                    e.printStackTrace()
                    throw e
                }
                return true
            }
        }
        task.onRunning = EventHandler { loadingDialog.show() }
        task.onSucceeded = EventHandler { loadingDialog.hide() }
        task.onFailed = EventHandler {
            loadingDialog.hide()
            val alert = Alert(AlertType.ERROR, errInfo.message)
            alert.show()
        }
        Thread(task).start()
    }

    @FXML
    @Throws(Exception::class)
    fun actLoad() {
        val activeTable = activeDynamoDbTable ?: return
        activeTable.actLoad()
    }

    @FXML
    @Throws(Exception::class)
    fun actAdd() {
        val activeTable = activeDynamoDbTable ?: return
        activeTable.actAdd()
    }

    @FXML
    @Throws(Exception::class)
    fun actDel() {
        val activeTable = activeDynamoDbTable ?: return
        activeTable.actDel()
    }

    @FXML
    @Throws(Exception::class)
    fun actBulkDel() {
        val activeTable = activeDynamoDbTable ?: return
        activeTable.actBulkDel()
    }

    @FXML
    @Throws(Exception::class)
    fun actCopyAdd() {
        val activeTable = activeDynamoDbTable ?: return
        activeTable.actCopyAdd()
    }

    @FXML
    @Throws(Exception::class)
    fun actShowTableInfo() {
        val activeTable = activeDynamoDbTable ?: return
        val desc = activeTable.tableInfo
        val sb = StringBuilder()
        sb.append("Table name = ").append(desc.tableName()).append("\r\n")
        sb.append("Record count = ").append(desc.itemCount()).append("\r\n")
        sb.append("Byte size = ").append(desc.tableSizeBytes()).append("\r\n")
        sb.append("---- Partition key ----").append("\r\n")
        sb.append("Hash key name = ").append(activeTable.mainKeyInfo.hashKey).append("\r\n")
        if (activeTable.hasSortKey()) {
            sb.append("Sort key name = ").append(activeTable.mainKeyInfo.sortKey).append("\r\n")
        }
        for (gsi in activeTable.gsiInfoList){
            sb.append("---- GSI [").append(gsi.indexName).append("] key info ----").append("\r\n")
            sb.append("Hash key name = ").append(gsi.hashKey).append("\r\n")
            if (gsi.sortKey != null) {
                sb.append("Sort key name = ").append(gsi.sortKey).append("\r\n")
            }
        }
        for (lsi in activeTable.lsiInfoList){
            sb.append("---- LSI [").append(lsi.indexName).append("] key info ----").append("\r\n")
            sb.append("Hash key name = ").append(lsi.hashKey).append("\r\n")
            if (lsi.sortKey != null) {
                sb.append("Sort key name = ").append(lsi.sortKey).append("\r\n")
            }
        }

        val tableInfoDialog = Alert(AlertType.NONE)
        tableInfoDialog.headerText = "Table Information"
        tableInfoDialog.dialogPane.buttonTypes.add(ButtonType.OK)
        tableInfoDialog.contentText = sb.toString()
        tableInfoDialog.showAndWait()
    }

    @FXML
    fun onLvTableListClicked(ev: MouseEvent) {
        try {
            if (ev.clickCount >= 2) {
                actTableDecided()
            }
        } catch (e: Exception) {
            val alert = Alert(AlertType.ERROR, e.message)
            alert.show()
        }
    }

    @FXML
    fun actCloseActiveTab() {
        val activeIndex = tabPaneTable!!.selectionModel.selectedIndex
        if (tabPaneTable!!.tabs.size > 1) {
            tabPaneTable!!.tabs.removeAt(activeIndex)
        }
    }

    @FXML
    fun actCloseAllNonActiveTab() {
        val activeIndex = tabPaneTable!!.selectionModel.selectedIndex
        val tabCount = tabPaneTable!!.tabs.size
        for (wkIdx in activeIndex + 1 until tabCount) {
            tabPaneTable!!.tabs.removeAt(wkIdx)
        }
        for (wkIdx in activeIndex - 1 downTo 0) {
            tabPaneTable!!.tabs.removeAt(wkIdx)
        }
    }

    /*
	 * Actions
	 */
    @Throws(URISyntaxException::class)
    fun actTableDecided() {
        try {
            val tableName = lvTableList!!.selectionModel.selectedItem
            val dbTable = DynamoDbTable(connectInfo, tableName, loadingDialog)
            dbTable.initialize()
            val newTab = Tab()
            newTab.text = tableName
            newTab.content = dbTable
            tabPaneTable!!.tabs.add(newTab)
            val selectionModel = tabPaneTable!!.selectionModel
            selectionModel.select(newTab)
        } catch (e: Exception) {
            val alert = Alert(AlertType.ERROR, e.message)
            alert.show()
            e.printStackTrace()
        }
    }

    /*
	 * method
	 */
    private fun initLoadDialog() {
        loadingDialog.headerText = null
        loadingDialog.contentText = "Now Loading..."
        val pane: Pane = loadingDialog.dialogPane
        val nodes = pane.children
        for (node in nodes) {
            (node as? ButtonBar)?.isVisible = false
        }
    }

    private fun initCmb() {
        cmbTableNameCond!!.items.addAll(TableNameCondType.Companion.titleList)
        cmbTableNameCond!!.selectionModel.select(0)
    }

    private val connectInfo: DynamoDbConnectInfo
        get() {
            var connectType = DynamoDbConnectType.LOCAL
            if (rbConnectAWS!!.isSelected) {
                connectType = DynamoDbConnectType.AWS
            }
            return DynamoDbConnectInfo(connectType, txtFldLocalEndpoint!!.text)
        }

    private val activeDynamoDbTable: DynamoDbTable?
        get() {
            val activeIndex = tabPaneTable!!.selectionModel.selectedIndex
            if (activeIndex >= tabPaneTable!!.tabs.size || activeIndex < 0) {
                return null
            }
            val activeTab = tabPaneTable!!.tabs[activeIndex]
            return activeTab.content as DynamoDbTable
        }
}