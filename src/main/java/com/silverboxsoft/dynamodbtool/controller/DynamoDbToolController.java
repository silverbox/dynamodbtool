package com.silverboxsoft.dynamodbtool.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.silverboxsoft.dynamodbtool.classes.DynamoDbCondition;
import com.silverboxsoft.dynamodbtool.classes.DynamoDbConditionJoinType;
import com.silverboxsoft.dynamodbtool.classes.DynamoDbConditionType;
import com.silverboxsoft.dynamodbtool.classes.DynamoDbResult;
import com.silverboxsoft.dynamodbtool.dao.QueryDao;
import com.silverboxsoft.dynamodbtool.dao.TableListDao;
import com.silverboxsoft.dynamodbtool.fxmodel.TableNameCondType;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class DynamoDbToolController implements Initializable {

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

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initCmb();
	}

	@FXML
	protected void actLoad(ActionEvent ev) {
		String tableName = txtFldTableName.getText();
		QueryDao dao = new QueryDao();
		List<DynamoDbCondition> conditionList = new ArrayList<>();
		DynamoDbCondition cond = new DynamoDbCondition();
		cond.setColumnName(txtFldColumnName.getText());
		cond.setConditionType(DynamoDbConditionType.EQUAL);
		cond.setValue(txtFldCondValue.getText());
		conditionList.add(cond);

		DynamoDbResult result = dao.getResult(tableName, DynamoDbConditionJoinType.AND, conditionList);
		setTable(result);
	}

	@FXML
	protected void actTableListLoad(ActionEvent ev) {
		TableListDao dao = new TableListDao();
		TableNameCondType conditionType = TableNameCondType.getByName(cmbTableNameCond.getValue());
		lvTableList.getItems().clear();
		lvTableList.getItems().addAll(dao.getTableList(txtFldTableNameCond.getText(), conditionType));
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

}
