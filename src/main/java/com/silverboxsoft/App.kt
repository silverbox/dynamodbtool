package com.silverboxsoft

import kotlin.Throws
import java.lang.Exception
import javafx.stage.Stage
import java.io.IOException
import javafx.fxml.FXMLLoader
import javafx.scene.layout.VBox
import javafx.scene.Scene
import javafx.scene.layout.GridPane
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.scene.layout.ColumnConstraints
import javafx.scene.control.ScrollPane.ScrollBarPolicy
import javafx.beans.value.ObservableValue
import javafx.scene.layout.BorderPane
import kotlin.jvm.JvmStatic
import com.silverboxsoft.dynamodbtool.classes.DynamoDbConnectInfo
import java.net.URISyntaxException
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import com.silverboxsoft.dynamodbtool.classes.DynamoDbConnectType
import com.silverboxsoft.dynamodbtool.dao.AbsDao
import software.amazon.awssdk.services.dynamodb.model.TableDescription
import com.silverboxsoft.dynamodbtool.classes.DynamoDbResult
import software.amazon.awssdk.services.dynamodb.model.ScanRequest
import com.silverboxsoft.dynamodbtool.classes.DynamoDbConditionJoinType
import com.silverboxsoft.dynamodbtool.classes.DynamoDbCondition
import java.util.HashMap
import software.amazon.awssdk.services.dynamodb.model.QueryRequest
import software.amazon.awssdk.services.dynamodb.model.QueryResponse
import software.amazon.awssdk.services.dynamodb.model.ExecuteStatementRequest
import software.amazon.awssdk.services.dynamodb.model.ExecuteStatementResponse
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest
import software.amazon.awssdk.services.dynamodb.model.DeleteItemResponse
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest
import com.silverboxsoft.dynamodbtool.utils.DynamoDbUtils
import software.amazon.awssdk.core.SdkBytes
import java.util.stream.Collectors
import com.silverboxsoft.dynamodbtool.classes.DynamoDbColumnType
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType
import java.math.BigDecimal
import com.silverboxsoft.dynamodbtool.classes.DynamoDbColumn
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndexDescription
import java.lang.NumberFormatException
import com.silverboxsoft.dynamodbtool.classes.DynamoDbViewRecord
import javafx.collections.ObservableList
import javafx.collections.FXCollections
import com.silverboxsoft.dynamodbtool.classes.DynamoDbConditionType
import com.silverboxsoft.dynamodbtool.classes.DynamoDbColumnTypeCategory
import com.silverboxsoft.dynamodbtool.fxmodel.TableNameCondType
import javafx.scene.control.ButtonBar.ButtonData
import com.silverboxsoft.dynamodbtool.controller.inputdialog.AbsDynamoDbInputDialog
import javafx.scene.layout.RowConstraints
import com.silverboxsoft.dynamodbtool.controller.inputdialog.AbsDynamoDbDocumentInputDialog
import com.silverboxsoft.dynamodbtool.controller.inputdialog.DynamoDbMapInputDialog
import javafx.scene.control.Alert.AlertType
import com.silverboxsoft.dynamodbtool.controller.inputdialog.DynamoDbListInputDialog
import java.util.HashSet
import com.silverboxsoft.dynamodbtool.controller.inputdialog.AbsDynamoDbSetInputDialog
import com.silverboxsoft.dynamodbtool.controller.DynamoDbEditMode
import com.silverboxsoft.dynamodbtool.controller.inputdialog.DynamoDbRecordInputDialog
import com.silverboxsoft.dynamodbtool.controller.inputdialog.DynamoDbNumberSetInputDialog
import com.silverboxsoft.dynamodbtool.controller.inputdialog.DynamoDbStringSetInputDialog
import com.silverboxsoft.dynamodbtool.controller.inputdialog.DynamoDbBinarySetInputDialog
import javafx.fxml.FXML
import com.silverboxsoft.dynamodbtool.controller.DynamoDbTable
import com.silverboxsoft.dynamodbtool.classes.DynamoDbErrorInfo
import com.silverboxsoft.dynamodbtool.dao.PartiQLDao
import com.silverboxsoft.dynamodbtool.dao.ScanDao
import javafx.concurrent.WorkerStateEvent
import java.lang.Thread
import com.silverboxsoft.dynamodbtool.types.CopyModeType
import javafx.scene.input.KeyCode
import com.silverboxsoft.dynamodbtool.dao.TableInfoDao
import javafx.scene.input.ClipboardContent
import javafx.scene.control.TableView.TableViewSelectionModel
import com.silverboxsoft.dynamodbtool.dao.PutItemDao
import com.silverboxsoft.dynamodbtool.dao.DeleteItemDao
import com.silverboxsoft.dynamodbtool.dao.QueryDao
import javafx.scene.control.cell.TextFieldTableCell
import javafx.beans.property.ReadOnlyObjectWrapper
import java.lang.RuntimeException
import javafx.fxml.Initializable
import java.util.ResourceBundle
import com.silverboxsoft.dynamodbtool.dao.TableListDao
import javafx.application.Application
import javafx.beans.value.ChangeListener
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.Pane
import javafx.stage.Screen

class App : Application() {
    @Throws(Exception::class)
    override fun start(stage: Stage) {
        prepareControl(stage)
        // prepareTestControl(stage);
        stage.show()
    }

    @Throws(IOException::class)
    private fun prepareControl(stage: Stage) {
        val fxmlLoader = FXMLLoader(
                javaClass.getResource("dynamodbtool/controller/javafx/DynamoDbToolController.fxml"))
        val newPane = fxmlLoader.load<Any>() as VBox
        val scene = Scene(newPane)
        stage.title = "DynamoDB Tool"
        stage.scene = scene
    }

    private fun prepareTestControl(stage: Stage) {
        val grid: GridPane
        val scrollPane: ScrollPane
        val anchorPane: AnchorPane
        val typelabel = getContentLabel("label test")
        val hbox = HBox()
        hbox.style = "-fx-background-color:yellow"
        val textField = TextField("Text field test")
        textField.minWidth = 300.0
        textField.autosize()
        // AnchorPane anchorPane1 = new AnchorPane();
        // anchorPane1.getChildren().add(textField);
        // anchorPane1.setStyle("-fx-background-color:green");
        // AnchorPane.setLeftAnchor(textField, 0.);
        // AnchorPane.setTopAnchor(textField, 0.);
        // AnchorPane.setRightAnchor(textField, 0.);
        // AnchorPane.setBottomAnchor(textField, 0.);
        hbox.children.addAll(textField)
        val delCheck = CheckBox()
        val typelabel2 = getContentLabel("label test2")
        val hbox2 = HBox()
        val vallabel = getContentLabel(
                "start"
                        + "あいうえおかきくけこさしすせそたちつてとあいうえおかきくけこさしすせそたちつてと"
                        + "あいうえおかきくけこさしすせそたちつてとあいうえおかきくけこさしすせそたちつてと" // + "あいうえおかきくけこさしすせそたちつてとあいうえおかきくけこさしすせそたちつてと"
                        // + "あいうえおかきくけこさしすせそたちつてとあいうえおかきくけこさしすせそたちつてと"
                        // + "あいうえおかきくけこさしすせそたちつてとあいうえおかきくけこさしすせそたちつてと"
                        // + "あいうえおかきくけこさしすせそたちつてとあいうえおかきくけこさしすせそたちつてと"
                        // + "あいうえおかきくけこさしすせそたちつてとあいうえおかきくけこさしすせそたちつてと"
                        // + "あいうえおかきくけこさしすせそたちつてとあいうえおかきくけこさしすせそたちつてと"
                        + "end")
        hbox2.children.addAll(vallabel)
        val delCheck2 = CheckBox()
        grid = GridPane()
        grid.hgap = 20.0
        grid.vgap = 5.0
        grid.maxWidth = Double.MAX_VALUE
        grid.alignment = Pos.CENTER
        // grid.setStyle("-fx-background-color:lightgray");
        grid.add(typelabel, 0, 0)
        grid.add(textField, 1, 0)
        grid.add(delCheck, 2, 0)
        grid.add(typelabel2, 0, 1)
        grid.add(hbox2, 1, 1)
        grid.add(delCheck2, 2, 1)
        for (i in 0..99) {
            val wklabel = getContentLabel(String.format("No. %1d row", i))
            grid.add(wklabel, 0, i + 2)
        }
        anchorPane = AnchorPane()
        anchorPane.children.add(grid)
        anchorPane.style = "-fx-background-color:green"
        AnchorPane.setLeftAnchor(grid, 0.0)
        AnchorPane.setTopAnchor(grid, 0.0)
        AnchorPane.setRightAnchor(grid, 0.0)
        AnchorPane.setBottomAnchor(grid, 0.0)
        scrollPane = ScrollPane()
        scrollPane.content = anchorPane
        scrollPane.style = "-fx-background-color:red"
        val colContraint0 = ColumnConstraints()
        val colContraint1 = ColumnConstraints()
        val colContraint2 = ColumnConstraints()
        colContraint0.prefWidth = 100.0
        colContraint1.prefWidth = 300.0
        colContraint2.prefWidth = 100.0
        grid.columnConstraints.add(colContraint0)
        grid.columnConstraints.add(colContraint1)
        grid.columnConstraints.add(colContraint2)
        scrollPane.hbarPolicy = ScrollBarPolicy.NEVER
        val stageSizeListener = ChangeListener { observable: ObservableValue<out Number>?, oldValue: Number?, newValue: Number -> grid.columnConstraints[1].prefWidth = newValue.toDouble() - 200.0 }
        scrollPane.widthProperty().addListener(stageSizeListener)
        val borderPane = BorderPane()
        borderPane.center = scrollPane
        borderPane.top = Label("test")
        borderPane.bottom = Button("add")
        val scene = Scene(borderPane)
        stage.title = "Test dialog"
        stage.scene = scene
        val screenSize2d = Screen.getPrimary().visualBounds
        stage.minHeight = 300.0
        stage.minWidth = 500.0
        stage.maxHeight = screenSize2d.height
        stage.maxWidth = screenSize2d.width
    }

    // same as DialogPane.createContentLabel
    private fun getContentLabel(text: String): Label {
        val label = Label(text)
        label.maxWidth = Double.MAX_VALUE
        label.maxHeight = Double.MAX_VALUE
        label.styleClass.add("content")
        label.isWrapText = true
        label.text = text
        return label
    } // private void setWinsize(ScrollPane pane, Bounds newValue) {

    // double dialogWidth = newValue.getWidth(); // thisDialog.getPrefWidth();
    // double dialogHeight = newValue.getHeight(); // thisDialog.getPrefHeight();
    // // scrollPane.setMaxWidth();
    // // scrollPane.setMaxHeight();
    // Rectangle2D screenSize2d = Screen.getPrimary().getVisualBounds();
    // double width = screenSize2d.getWidth();
    // double height = screenSize2d.getHeight();
    // double newWidth = dialogWidth > width ? width : dialogWidth;
    // double newHeight = dialogHeight > height ? height : dialogHeight;
    // System.out.println(String.format("width=%1$f, height=%2$f, win-wid=%3$f, win-hei=%4$f",
    // dialogWidth, dialogHeight, width, height));
    // pane.setPrefSize(newWidth, newHeight);
    // }
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(*args)
        }
    }
}