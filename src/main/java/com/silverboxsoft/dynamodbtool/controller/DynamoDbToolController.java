package com.silverboxsoft.dynamodbtool.controller;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import com.silverboxsoft.dynamodbtool.classes.DynamoDbCondition;
import com.silverboxsoft.dynamodbtool.classes.DynamoDbConditionJoinType;
import com.silverboxsoft.dynamodbtool.classes.DynamoDbConditionType;
import com.silverboxsoft.dynamodbtool.classes.DynamoDbConnectInfo;
import com.silverboxsoft.dynamodbtool.classes.DynamoDbConnectType;
import com.silverboxsoft.dynamodbtool.classes.DynamoDbResult;
import com.silverboxsoft.dynamodbtool.dao.QueryDao;
import com.silverboxsoft.dynamodbtool.dao.ScanDao;
import com.silverboxsoft.dynamodbtool.dao.TableInfoDao;
import com.silverboxsoft.dynamodbtool.dao.TableListDao;
import com.silverboxsoft.dynamodbtool.fxmodel.TableNameCondType;

import javafx.beans.property.ReadOnlyObjectWrapper;
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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;
import software.amazon.awssdk.utils.StringUtils;

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
	TableView<ObservableList<String>> tableResultList;

	@FXML
	ContextMenu contextMenuTable;

	@FXML
	MenuItem menuItemTableResultListCopy;

	@FXML
	MenuItem menuItemTableResultListCellSelectMode;

	/*
	 * Data Condition
	 */
	@FXML
	TextField txtFldColumnName;

	@FXML
	TextField txtFldCondValue;

	/*
	 * Table Name Condition
	 */
	@FXML
	TextField txtFldTableNameCond;

	@FXML
	ComboBox<String> cmbTableNameCond;

	@FXML
	ListView<String> lvTableList;

	@FXML
	Label lblTableName;

	@FXML
	Label lblRecordCount;

	@FXML
	Label lblTableSize;

	@FXML
	Label lblPartitionKey;

	@FXML
	Label lblSortKey;

	/*
	 * event handler
	 */

	private TableDescription currentTableInfo = null;
	private boolean isCellSelectMode = true;
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
	protected void actLoad(ActionEvent ev) throws URISyntaxException {
		startWaiting();
		try {
			String condColumn = txtFldColumnName.getText();
			String condValue = txtFldCondValue.getText();
			DynamoDbResult result = null;
			if (!StringUtils.isEmpty(condColumn) && !StringUtils.isEmpty(condValue)) {
				QueryDao dao = new QueryDao(getConnectInfo());
				List<DynamoDbCondition> conditionList = new ArrayList<>();
				DynamoDbCondition cond = new DynamoDbCondition();
				cond.setColumnName(condColumn);
				cond.setConditionType(DynamoDbConditionType.EQUAL);
				cond.setValue(condValue);
				conditionList.add(cond);
				result = dao.getResult(currentTableInfo, DynamoDbConditionJoinType.AND, conditionList);
			} else {
				ScanDao dao = new ScanDao(getConnectInfo());
				result = dao.getResult(currentTableInfo);
			}
			setTable(result);
		} finally {
			finishWaiting();
		}
	}

	@FXML
	protected void actTableListLoad(ActionEvent ev) throws URISyntaxException {
		startWaiting();
		try {
			TableListDao dao = new TableListDao(getConnectInfo());
			TableNameCondType conditionType = TableNameCondType.getByName(cmbTableNameCond.getValue());
			lvTableList.getItems().clear();
			lvTableList.getItems().addAll(dao.getTableList(txtFldTableNameCond.getText(), conditionType));
		} finally {
			finishWaiting();
		}
	}

	private void startWaiting() {
		dialog.show();
	}

	private void finishWaiting() {
		dialog.close();
	}

	@FXML
	protected void onLvTableListClicked(MouseEvent ev) throws URISyntaxException {
		if (ev.getClickCount() >= 2) {
			actTableDecided();
		}
	}

	@FXML
	protected void actTableLineCopyToClipBoard(ActionEvent ev) {
		final ClipboardContent content = new ClipboardContent();
		TableViewSelectionModel<ObservableList<String>> selectedModel = tableResultList.getSelectionModel();
		if (selectedModel.getSelectedItems().size() == 0) {
			return;
		}
		String targetStr = null;
		if (tableResultList.getSelectionModel().isCellSelectionEnabled()) {
			targetStr = getWholeTableSelectedCellString(selectedModel);
		} else {
			targetStr = getWholeTableSelectedRowString(selectedModel);
		}
		if (targetStr != null) {
			content.putString(targetStr);
			Clipboard.getSystemClipboard().setContent(content);
		}
	}

	@FXML
	protected void onTableResultListKeyPressed(KeyEvent ev) {
		if (ev.isControlDown() && ev.getEventType() == KeyEvent.KEY_PRESSED && ev.getCode() == KeyCode.C) {
			actTableLineCopyToClipBoard(null);
		}
	}

	@FXML
	protected void actToggleCellSelectMode(ActionEvent ev) {
		isCellSelectMode = !isCellSelectMode;
		if (isCellSelectMode) {
			menuItemTableResultListCellSelectMode.setText("Switch to row select mode");
		} else {
			menuItemTableResultListCellSelectMode.setText("Switch to cell select mode");
		}
		tableResultList.getSelectionModel().setCellSelectionEnabled(isCellSelectMode);
	}

	/*
	 * Actions
	 */

	protected void actTableDecided() throws URISyntaxException {
		String tableName = lvTableList.getSelectionModel().getSelectedItem();
		setCurrentTableInfo(tableName);
	}

	private void setCurrentTableInfo(String tableName) throws URISyntaxException {
		lblTableName.setText(tableName);
		TableInfoDao tableNameDao = new TableInfoDao(getConnectInfo());
		TableDescription tableInfo = tableNameDao.getTableDescription(tableName);
		lblRecordCount.setText(String.valueOf(tableInfo.itemCount().longValue()));
		lblTableSize.setText(String.valueOf(tableInfo.tableSizeBytes().longValue()));
		currentTableInfo = tableInfo;
		setCurrentTableInfoToComponent();
		tableResultList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		tableResultList.getSelectionModel().setCellSelectionEnabled(isCellSelectMode);
	}

	/*
	 * method
	 */

	private String getWholeTableSelectedCellString(TableViewSelectionModel<ObservableList<String>> selectedModel) {
		List<Pair<Integer, Integer>> posList = getPositionList(selectedModel);

		StringBuilder selectedRowStrSb = new StringBuilder();
		int oldRow = posList.get(0).getValue();
		for (Pair<Integer, Integer> position : posList) {
			int newRow = position.getValue();
			String cellStr = tableResultList.getItems().get(newRow).get(position.getKey());
			cellStr = cellStr == null ? "" : cellStr;
			if (oldRow != newRow) {
				selectedRowStrSb.append("\n").append(cellStr);
			} else if (selectedRowStrSb.length() == 0) {
				selectedRowStrSb.append(cellStr);
			} else {
				selectedRowStrSb.append("\t").append(cellStr);
			}
			oldRow = position.getValue();
		}
		return selectedRowStrSb.toString();
	}

	@SuppressWarnings("unchecked")
	private List<Pair<Integer, Integer>> getPositionList(
			TableViewSelectionModel<ObservableList<String>> selectedModel) {
		@SuppressWarnings("rawtypes")
		ObservableList<TablePosition> selPosList = selectedModel.getSelectedCells();

		List<Pair<Integer, Integer>> posList = new ArrayList<>();
		for (TablePosition<ObservableList<String>, String> position : selPosList) {
			posList.add(new Pair<Integer, Integer>(position.getColumn(), position.getRow()));
		}
		posList.sort(new Comparator<Pair<Integer, Integer>>() {
			@Override
			public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
				int rowdiff = o1.getValue() - o2.getValue();
				if (rowdiff != 0) {
					return rowdiff;
				}
				return o1.getKey() - o2.getKey();
			}
		});
		return posList;
	}

	private String getWholeTableSelectedRowString(TableViewSelectionModel<ObservableList<String>> selectedModel) {
		ObservableList<ObservableList<String>> selectedRecords = selectedModel.getSelectedItems();
		if (selectedRecords.size() == 0) {
			return null;
		}

		StringBuilder selectedRowStrSb = new StringBuilder();
		for (ObservableList<String> record : selectedRecords) {
			if (selectedRowStrSb.length() > 0) {
				selectedRowStrSb.append("\n");
			}
			selectedRowStrSb.append(getOneRowString(record));
		}
		return selectedRowStrSb.toString();
	}

	private String getOneRowString(ObservableList<String> record) {
		StringBuilder oneRowStrSb = new StringBuilder();
		for (String item : record) {
			if (oneRowStrSb.length() > 0) {
				oneRowStrSb.append("\t");
			}
			oneRowStrSb.append(item == null ? "" : item);
		}
		return oneRowStrSb.toString();
	}

	private void setCurrentTableInfoToComponent() {
		txtFldColumnName.setText("");
		lblPartitionKey.setText("-");
		lblSortKey.setText("-");

		List<KeySchemaElement> keyInfos = currentTableInfo.keySchema();
		for (KeySchemaElement k : keyInfos) {
			if (k.keyType() == KeyType.HASH) {
				txtFldColumnName.setText(k.attributeName());
				lblPartitionKey.setText(k.attributeName());
			} else if (k.keyType() == KeyType.RANGE) {
				lblSortKey.setText(k.attributeName());
			}
		}
	}

	private void initCmb() {
		cmbTableNameCond.getItems().addAll(TableNameCondType.getTitleList());
	}

	private void setTable(DynamoDbResult result) {
		tableResultList.getItems().clear();

		tableResultList.getColumns().clear();
		for (int colIdx = 0; colIdx < result.getColumnCount(); colIdx++) {
			String columnName = result.getDynamoDbColumn(colIdx).getColumnName();
			TableColumn<ObservableList<String>, String> dataCol = getTableColumn(columnName, colIdx);
			dataCol.setCellFactory(TextFieldTableCell.forTableColumn());
			tableResultList.getColumns().add(dataCol);
		}

		tableResultList.getItems().addAll(result.getResultItems());
	}

	private TableColumn<ObservableList<String>, String> getTableColumn(String columnName, final int finalColIdx) {
		TableColumn<ObservableList<String>, String> dataCol = new TableColumn<>(columnName);
		dataCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(finalColIdx)));
		return dataCol;
	}

	private DynamoDbConnectInfo getConnectInfo() {
		DynamoDbConnectInfo connInfo = new DynamoDbConnectInfo();
		if (rbConnectAWS.isSelected()) {
			connInfo.setConnectType(DynamoDbConnectType.AWS);
		} else {
			connInfo.setConnectType(DynamoDbConnectType.LOCAL);
		}
		connInfo.setEndpointUrl(txtFldLocalEndpoint.getText());

		return connInfo;
	}
}
