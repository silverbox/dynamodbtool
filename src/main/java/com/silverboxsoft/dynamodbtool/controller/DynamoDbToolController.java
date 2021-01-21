package com.silverboxsoft.dynamodbtool.controller;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;
import software.amazon.awssdk.utils.StringUtils;

public class DynamoDbToolController implements Initializable {

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

	/*
	 * Data Condition
	 */
	@FXML
	TextField txtFldTableName;

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
	Label lblRecordCount;

	@FXML
	Label lblTableSize;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initCmb();
	}

	@FXML
	protected void actLoad(ActionEvent ev) throws URISyntaxException {
		String tableName = txtFldTableName.getText();
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
			result = dao.getResult(tableName, DynamoDbConditionJoinType.AND, conditionList);
		} else {
			ScanDao dao = new ScanDao(getConnectInfo());
			result = dao.getResult(tableName);
		}
		setTable(result);
	}

	@FXML
	protected void actTableListLoad(ActionEvent ev) throws URISyntaxException {
		TableListDao dao = new TableListDao(getConnectInfo());
		TableNameCondType conditionType = TableNameCondType.getByName(cmbTableNameCond.getValue());
		lvTableList.getItems().clear();
		lvTableList.getItems().addAll(dao.getTableList(txtFldTableNameCond.getText(), conditionType));
	}

	@FXML
	protected void onLvTableListClicked(MouseEvent ev) throws URISyntaxException {
		if (ev.getClickCount() >= 2) {
			actTableDecided();
		}
	}

	protected void actTableDecided() throws URISyntaxException {
		String tableName = lvTableList.getSelectionModel().getSelectedItem();
		txtFldTableName.setText(tableName);
		TableInfoDao tableNameDao = new TableInfoDao(getConnectInfo());
		TableDescription tableInfo = tableNameDao.getTableDescription(tableName);
		lblRecordCount.setText(String.valueOf(tableInfo.itemCount().longValue()));
		lblTableSize.setText(String.valueOf(tableInfo.tableSizeBytes().longValue()));
		// List<AttributeDefinition> attributes = tableInfo.attributeDefinitions();
		// System.out.println("Attributes");
		// for (AttributeDefinition a : attributes) {
		// System.out.format(" %s (%s)\n", a.attributeName(), a.attributeType());
		// }

		System.out.println("keyInfos");
		List<KeySchemaElement> keyInfos = tableInfo.keySchema();
		for (KeySchemaElement k : keyInfos) {
			if (k.keyType() == KeyType.HASH) {
				txtFldColumnName.setText(k.attributeName());
			}
			System.out.format("  %s (%s)\n", k.attributeName(), k.keyType().toString());
		}
	}

	// @FXML
	// protected void onTxtFldTableNameCondKeyPress(KeyEvent e) {
	// System.out.println("onTxtFldTableNameCondKeyPress");
	// }

	private void initCmb() {
		cmbTableNameCond.getItems().addAll(TableNameCondType.getTitleList());
	}

	private void setTable(DynamoDbResult result) {
		tableResultList.getItems().clear();

		tableResultList.getColumns().clear();
		for (int colIdx = 0; colIdx < result.getColumnCount(); colIdx++) {
			String columnName = result.getDynamoDbColumn(colIdx).getColumnName();
			TableColumn<ObservableList<String>, String> dataCol = new TableColumn<>(columnName);
			final int finalColIdx = colIdx;
			dataCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(finalColIdx)));
			tableResultList.getColumns().add(dataCol);
		}

		tableResultList.getItems().addAll(result.getResultItems());
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
