package com.silverboxsoft.dynamodbtool.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.silverboxsoft.dynamodbtool.classes.DynamoDbCondition;
import com.silverboxsoft.dynamodbtool.classes.DynamoDbConditionJoinType;
import com.silverboxsoft.dynamodbtool.classes.DynamoDbConditionType;
import com.silverboxsoft.dynamodbtool.classes.DynamoDbConnectInfo;
import com.silverboxsoft.dynamodbtool.classes.DynamoDbResult;
import com.silverboxsoft.dynamodbtool.controller.inputdialog.DynamoDbRecordInputDialog;
import com.silverboxsoft.dynamodbtool.dao.PartiQLDao;
import com.silverboxsoft.dynamodbtool.dao.PutItemDao;
import com.silverboxsoft.dynamodbtool.dao.QueryDao;
import com.silverboxsoft.dynamodbtool.dao.ScanDao;
import com.silverboxsoft.dynamodbtool.dao.TableInfoDao;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Pair;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;
import software.amazon.awssdk.utils.StringUtils;

public class DynamoDbTable extends AnchorPane {

	/*
	 * Data Condition
	 */
	@FXML
	TextField txtFldColumnName;

	@FXML
	TextField txtFldCondValue;

	@FXML
	TextArea txtAreaPartiql;

	@FXML
	RadioButton radioLoadPartiQL;

	@FXML
	RadioButton radioLoadKeyValue;

	@FXML
	ToggleGroup loadType;

	/*
	 * info area
	 */
	@FXML
	AnchorPane paneTableInfo;

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
	 * data area
	 */
	@FXML
	TableView<ObservableList<String>> tableResultList;

	@FXML
	ContextMenu contextMenuTable;

	@FXML
	MenuItem menuItemTableResultListCopy;

	@FXML
	MenuItem menuItemTableResultListCellSelectMode;

	private static final int TBL_COL_MAX_WIDTH = 1000;

	private boolean isCellSelectMode = true;
	private TableDescription currentTableInfo = null;
	private Alert dialog;
	private DynamoDbConnectInfo connInfo;
	private String tableName;
	private DynamoDbResult dynamoDbResult;

	public DynamoDbTable() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
				"javafx/DynamoDbTable.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			Alert alert = new Alert(AlertType.ERROR, exception.getMessage());
			alert.show();
			throw new RuntimeException(exception);
		}
	}

	public void initialize(DynamoDbConnectInfo connInfo, String tableName, Alert dialog) {

		this.connInfo = connInfo;
		this.tableName = tableName;
		this.dialog = dialog;
		try {
			setCurrentTableInfo();
			loadType.selectedToggleProperty()
					.addListener((ObservableValue<? extends Toggle> observ, Toggle oldVal, Toggle newVal) -> {
						onLoadTypeChange(null);
					});
			onLoadTypeChange(null);
		} catch (Exception e) {
			Alert alert = new Alert(AlertType.ERROR, e.getMessage());
			alert.show();
			e.printStackTrace();
		}
	}

	private void setCurrentTableInfo() throws URISyntaxException {
		lblTableName.setText(tableName);
		TableInfoDao tableNameDao = new TableInfoDao(connInfo);
		TableDescription tableInfo = tableNameDao.getTableDescription(tableName);
		lblRecordCount.setText(String.valueOf(tableInfo.itemCount().longValue()));
		lblTableSize.setText(String.valueOf(tableInfo.tableSizeBytes().longValue()));
		currentTableInfo = tableInfo;
		setCurrentTableInfoToComponent();
		tableResultList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		tableResultList.getSelectionModel().setCellSelectionEnabled(isCellSelectMode);
	}

	/*
	 * event handler
	 */

	@FXML
	protected void actLoad(ActionEvent ev) throws Exception {
		Task<Boolean> task = new Task<Boolean>() {
			@Override
			public Boolean call() {
				try {
					DynamoDbResult result = null;
					if (radioLoadPartiQL.isSelected()) {
						PartiQLDao partqlDao = new PartiQLDao(connInfo);
						result = partqlDao.getResult(currentTableInfo, txtAreaPartiql.getText());
					} else {
						String condColumn = txtFldColumnName.getText();
						String condValue = txtFldCondValue.getText();
						if (!StringUtils.isEmpty(condColumn) && !StringUtils.isEmpty(condValue)) {
							QueryDao dao = new QueryDao(connInfo);
							List<DynamoDbCondition> conditionList = new ArrayList<>();
							DynamoDbCondition cond = new DynamoDbCondition();
							cond.setColumnName(condColumn);
							cond.setConditionType(DynamoDbConditionType.EQUAL);
							cond.setValue(condValue);
							conditionList.add(cond);
							result = dao.getResult(currentTableInfo, DynamoDbConditionJoinType.AND, conditionList);
						} else {
							ScanDao dao = new ScanDao(connInfo);
							result = dao.getResult(currentTableInfo);
						}
					}
					dynamoDbResult = result;
				} catch (Exception e) {
					Alert alert = new Alert(AlertType.ERROR, e.getMessage());
					alert.show();
				}
				return true;
			}
		};
		task.setOnRunning((e) -> dialog.show());
		task.setOnSucceeded((e) -> {
			setTable(dynamoDbResult);
			dialog.hide();
		});
		task.setOnFailed((e) -> {
			dialog.hide();
		});
		new Thread(task).start();
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
	protected void onMouseClicked(MouseEvent ev) {
		try {
			if (ev.getClickCount() >= 2) {
				actShowInputDialog();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Alert alert = new Alert(AlertType.ERROR, e.getMessage());
			alert.show();
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

	@FXML
	protected void onLoadTypeChange(Event ev) {
		txtAreaPartiql.setDisable(!radioLoadPartiQL.isSelected());
		txtFldColumnName.setDisable(radioLoadPartiQL.isSelected());
		txtFldCondValue.setDisable(radioLoadPartiQL.isSelected());
	}
	/*
	 * normal methods
	 */

	private void actShowInputDialog() throws URISyntaxException {
		TableViewSelectionModel<ObservableList<String>> selectedModel = tableResultList.getSelectionModel();
		List<Pair<Integer, Integer>> posList = getPositionList(selectedModel);
		if (posList.size() == 0) {
			return;
		}
		int row = posList.get(0).getValue();
		Map<String, AttributeValue> rec = dynamoDbResult.getRawResItems().get(row);

		DynamoDbRecordInputDialog dialog = new DynamoDbRecordInputDialog(currentTableInfo, rec);
		Optional<Map<String, AttributeValue>> newRecWk = dialog.showAndWait();
		if (newRecWk.isPresent()) {
			Map<String, AttributeValue> newRec = newRecWk.get();
			PutItemDao dao = new PutItemDao(connInfo);
			dao.putItem(currentTableInfo, newRec);

			dynamoDbResult.updateRecord(row, newRec);
			ObservableList<String> tableRec = dynamoDbResult.getOneTableRecord(newRec);
			tableResultList.getItems().set(row, tableRec);
		}
	}

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
		String defPartQL = String.format("select * from %1$s where ", tableName);
		StringBuilder sbPartiQL = new StringBuilder(defPartQL);
		boolean hasSortKey = false;
		for (KeySchemaElement k : keyInfos) {
			if (k.keyType() == KeyType.HASH) {
				txtFldColumnName.setText(k.attributeName());
				lblPartitionKey.setText(k.attributeName());
			} else if (k.keyType() == KeyType.RANGE) {
				lblSortKey.setText(k.attributeName());
				hasSortKey = true;
			}
		}
		sbPartiQL.append(lblPartitionKey.getText()).append(" = ?");
		if (hasSortKey) {
			sbPartiQL.append(" and ").append(lblSortKey.getText()).append(" = ?");
		}
		txtAreaPartiql.setText(sbPartiQL.toString());
	}

	private void setTable(DynamoDbResult result) {
		tableResultList.getItems().clear();

		tableResultList.getColumns().clear();
		for (int colIdx = 0; colIdx < result.getColumnCount(); colIdx++) {
			String columnName = result.getDynamoDbColumn(colIdx).getColumnName();
			TableColumn<ObservableList<String>, String> dataCol = getTableColumn(columnName, colIdx);
			dataCol.setCellFactory(TextFieldTableCell.forTableColumn());
			dataCol.setMaxWidth(TBL_COL_MAX_WIDTH);
			tableResultList.getColumns().add(dataCol);
		}

		tableResultList.getItems().addAll(result.getResultItems());
	}

	private TableColumn<ObservableList<String>, String> getTableColumn(String columnName, final int finalColIdx) {
		TableColumn<ObservableList<String>, String> dataCol = new TableColumn<>(columnName);
		dataCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(finalColIdx)));
		return dataCol;
	}
}