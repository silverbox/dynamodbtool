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
import javafx.event.ActionEvent
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
    private var dialog: Alert = Alert(AlertType.INFORMATION)
    override fun initialize(location: URL, resources: ResourceBundle?) {
        initCmb()
        initLoadDialog()
    }

    @FXML
    fun actTableListLoad(ev: ActionEvent?) {
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
        task.onRunning = EventHandler { e: WorkerStateEvent? -> dialog!!.show() }
        task.onSucceeded = EventHandler { e: WorkerStateEvent? -> dialog!!.hide() }
        task.onFailed = EventHandler { e: WorkerStateEvent? ->
            dialog!!.hide()
            val alert = Alert(AlertType.ERROR, errInfo.message)
            alert.show()
        }
        Thread(task).start()
    }

    @FXML
    @Throws(Exception::class)
    fun actLoad(ev: ActionEvent?) {
        val activeTable = activeDynamoDbTable ?: return
        activeTable.actLoad(ev)
    }

    @FXML
    @Throws(Exception::class)
    fun actAdd(ev: ActionEvent?) {
        val activeTable = activeDynamoDbTable ?: return
        activeTable.actAdd(ev)
    }

    @FXML
    @Throws(Exception::class)
    fun actDel(ev: ActionEvent?) {
        val activeTable = activeDynamoDbTable ?: return
        activeTable.actDel(ev)
    }

    @FXML
    @Throws(Exception::class)
    fun actCopyAdd(ev: ActionEvent?) {
        val activeTable = activeDynamoDbTable ?: return
        activeTable.actCopyAdd(ev)
    }

    @FXML
    @Throws(Exception::class)
    fun actShowTableInfo(ev: ActionEvent?) {
        val activeTable = activeDynamoDbTable ?: return
        val desc = activeTable.tableInfo
        val sb = StringBuilder()
        sb.append("Table name = ").append(desc!!.tableName()).append("\r\n")
        sb.append("Partition key name = ").append(activeTable.partitionKeyName).append("\r\n")
        if (activeTable.hasSortKey()) {
            sb.append("Sort key name = ").append(activeTable.sortKeyName).append("\r\n")
        }
        sb.append("Record count = ").append(desc.itemCount()).append("\r\n")
        sb.append("Byte size = ").append(desc.tableSizeBytes()).append("\r\n")
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
    fun actCloseActiveTab(ev: ActionEvent?) {
        val activeIndex = tabPaneTable!!.selectionModel.selectedIndex
        if (tabPaneTable!!.tabs.size > 1) {
            tabPaneTable!!.tabs.removeAt(activeIndex)
        }
    }

    @FXML
    fun actCloseAllNonActiveTab(ev: ActionEvent?) {
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
        val tableName = lvTableList!!.selectionModel.selectedItem
        val dbtable = DynamoDbTable(connectInfo, tableName, dialog)
        dbtable.initialize()
        val newTab = Tab()
        newTab.text = tableName
        newTab.content = dbtable
        tabPaneTable!!.tabs.add(newTab)
        val selectionModel = tabPaneTable!!.selectionModel
        selectionModel.select(newTab)
    }

    /*
	 * method
	 */
    private fun initLoadDialog() {
        dialog.headerText = null
        dialog.contentText = "Now Loading..."
        val pane: Pane = dialog.dialogPane
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