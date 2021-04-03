package com.silverboxsoft.dynamodbtool.controller;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

import com.silverboxsoft.dynamodbtool.classes.DynamoDbConnectInfo;
import com.silverboxsoft.dynamodbtool.classes.DynamoDbConnectType;
import com.silverboxsoft.dynamodbtool.classes.DynamoDbErrorInfo;
import com.silverboxsoft.dynamodbtool.dao.TableListDao;
import com.silverboxsoft.dynamodbtool.fxmodel.TableNameCondType;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;

public class DynamoDbToolController implements Initializable {

	@FXML
	VBox vboxRoot;
	/*
	 * Connection kind
	 */
	@FXML
	RadioButton rbConnectAWS;

	@FXML
	RadioButton rbConnectLocalDynamoDB;

	@FXML
	TextField txtFldLocalEndpoint;

	@FXML
	MenuItem miAddAllKeyCond;

	@FXML
	MenuItem miAddPartitionKeyCond;

	/*
	 * Table Name Condition
	 */
	@FXML
	TextField txtFldTableNameCond;

	@FXML
	ComboBox<String> cmbTableNameCond;

	@FXML
	ListView<String> lvTableList;

	/*
	 * 
	 */
	@FXML
	TabPane tabPaneTable;

	/*
	 * event handler
	 */
	private Alert dialog;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initCmb();
		initLoadDialog();
	}

	@FXML
	protected void actTableListLoad(ActionEvent ev) {
		final DynamoDbErrorInfo errInfo = new DynamoDbErrorInfo();
		Task<Boolean> task = new Task<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				try {
					TableListDao dao = new TableListDao(getConnectInfo());
					TableNameCondType conditionType = TableNameCondType.getByName(cmbTableNameCond.getValue());
					lvTableList.getItems().clear();
					lvTableList.getItems().addAll(dao.getTableList(txtFldTableNameCond.getText(), conditionType));
				} catch (Exception e) {
					errInfo.setMessage(e.getMessage());
					e.printStackTrace();
					throw e;
				}
				return true;
			}
		};
		task.setOnRunning((e) -> dialog.show());
		task.setOnSucceeded((e) -> {
			dialog.hide();
		});
		task.setOnFailed((e) -> {
			dialog.hide();
			Alert alert = new Alert(AlertType.ERROR, errInfo.getMessage());
			alert.show();
		});
		new Thread(task).start();
	}

	@FXML
	protected void actLoad(ActionEvent ev) throws Exception {
		DynamoDbTable activeTable = getActiveDynamoDbTable();
		if (activeTable == null) {
			return;
		}
		activeTable.actLoad(ev);
	}

	@FXML
	protected void actAdd(ActionEvent ev) throws Exception {
		DynamoDbTable activeTable = getActiveDynamoDbTable();
		if (activeTable == null) {
			return;
		}
		activeTable.actAdd(ev);
	}

	@FXML
	protected void actDel(ActionEvent ev) throws Exception {
		DynamoDbTable activeTable = getActiveDynamoDbTable();
		if (activeTable == null) {
			return;
		}
		activeTable.actDel(ev);
	}

	@FXML
	protected void actShowTableInfo(ActionEvent ev) throws Exception {
		DynamoDbTable activeTable = getActiveDynamoDbTable();
		if (activeTable == null) {
			return;
		}
		TableDescription desc = activeTable.getTableInfo();
		StringBuilder sb = new StringBuilder();
		sb.append("Table name = ").append(desc.tableName()).append("\r\n");
		sb.append("Partition key name = ").append(activeTable.getPartitionKeyName()).append("\r\n");
		sb.append("Sort key name = ").append(activeTable.getSortKeyName()).append("\r\n");
		sb.append("Record count = ").append(desc.itemCount()).append("\r\n");
		sb.append("Byte size = ").append(desc.tableSizeBytes()).append("\r\n");
		Alert tableInfoDialog = new Alert(AlertType.NONE);
		tableInfoDialog.setHeaderText("Table Information");
		tableInfoDialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
		tableInfoDialog.setContentText(sb.toString());
		tableInfoDialog.showAndWait();
	}

	@FXML
	protected void onLvTableListClicked(MouseEvent ev) {
		try {
			if (ev.getClickCount() >= 2) {
				actTableDecided();
			}
		} catch (Exception e) {
			Alert alert = new Alert(AlertType.ERROR, e.getMessage());
			alert.show();
		}
	}

	@FXML
	protected void actCloseActiveTab(ActionEvent ev) {
		int activeIndex = tabPaneTable.getSelectionModel().getSelectedIndex();
		if (tabPaneTable.getTabs().size() > 1) {
			tabPaneTable.getTabs().remove(activeIndex);
		}
	}

	@FXML
	protected void actCloseAllNonActiveTab(ActionEvent ev) {
		int activeIndex = tabPaneTable.getSelectionModel().getSelectedIndex();
		int tabCount = tabPaneTable.getTabs().size();
		for (int wkIdx = activeIndex + 1; wkIdx < tabCount; wkIdx++) {
			tabPaneTable.getTabs().remove(wkIdx);
		}
		for (int wkIdx = activeIndex - 1; wkIdx >= 0; wkIdx--) {
			tabPaneTable.getTabs().remove(wkIdx);
		}
	}

	/*
	 * Actions
	 */

	protected void actTableDecided() throws URISyntaxException {
		String tableName = lvTableList.getSelectionModel().getSelectedItem();
		DynamoDbTable dbtable = new DynamoDbTable();
		dbtable.initialize(getConnectInfo(), tableName, dialog);
		Tab newTab = new Tab();
		newTab.setText(tableName);
		newTab.setContent(dbtable);
		tabPaneTable.getTabs().add(newTab);
		SingleSelectionModel<Tab> selectionModel = tabPaneTable.getSelectionModel();
		selectionModel.select(newTab);
	}

	/*
	 * method
	 */

	private void initLoadDialog() {
		dialog = new Alert(AlertType.INFORMATION);
		dialog.setHeaderText(null);
		// dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
		dialog.setContentText("Now Loading...");
		Pane pane = dialog.getDialogPane();
		ObservableList<Node> nodes = pane.getChildren();
		for (Node node : nodes) {
			if (node instanceof ButtonBar) {
				node.setVisible(false);
			}
		}
	}

	private void initCmb() {
		cmbTableNameCond.getItems().addAll(TableNameCondType.getTitleList());
	}

	private DynamoDbConnectInfo getConnectInfo() {
		DynamoDbConnectType connectType = DynamoDbConnectType.LOCAL;
		if (rbConnectAWS.isSelected()) {
			connectType = DynamoDbConnectType.AWS;
		}
		return new DynamoDbConnectInfo(connectType, txtFldLocalEndpoint.getText());
	}

	private DynamoDbTable getActiveDynamoDbTable() {
		int activeIndex = tabPaneTable.getSelectionModel().getSelectedIndex();
		if (activeIndex >= tabPaneTable.getTabs().size()) {
			return null;
		}
		Tab activeTab = tabPaneTable.getTabs().get(activeIndex);
		return (DynamoDbTable) activeTab.getContent();
	}
}
