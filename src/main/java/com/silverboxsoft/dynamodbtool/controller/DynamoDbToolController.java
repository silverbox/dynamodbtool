package com.silverboxsoft.dynamodbtool.controller;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

import com.silverboxsoft.dynamodbtool.classes.DynamoDbConnectInfo;
import com.silverboxsoft.dynamodbtool.classes.DynamoDbConnectType;
import com.silverboxsoft.dynamodbtool.dao.TableListDao;
import com.silverboxsoft.dynamodbtool.fxmodel.TableNameCondType;

import javafx.application.Platform;
import javafx.collections.ObservableList;
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
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

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

	private void initLoadDialog() {
		dialog = new Alert(AlertType.NONE);
		dialog.setHeaderText(null);
		dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
		dialog.setContentText("Now Loading...");
		Pane pane = dialog.getDialogPane();
		ObservableList<Node> nodes = pane.getChildren();
		for (Node node : nodes) {
			if (node instanceof ButtonBar) {
				node.setVisible(false);
			}
		}
	}

	@FXML
	protected void actTableListLoad(ActionEvent ev) {
		startWaiting();
		try {
			TableListDao dao = new TableListDao(getConnectInfo());
			TableNameCondType conditionType = TableNameCondType.getByName(cmbTableNameCond.getValue());
			lvTableList.getItems().clear();
			lvTableList.getItems().addAll(dao.getTableList(txtFldTableNameCond.getText(), conditionType));
		} catch (Exception e) {
			Alert alert = new Alert(AlertType.ERROR, e.getMessage());
			alert.show();
		} finally {
			finishWaiting();
		}
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

	private void startWaiting() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				dialog.show();
			}
		});
	}

	private void finishWaiting() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				dialog.close();
			}
		});
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
	}

	/*
	 * method
	 */

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
}
