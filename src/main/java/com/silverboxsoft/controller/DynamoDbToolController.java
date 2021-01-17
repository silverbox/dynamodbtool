package com.silverboxsoft.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.silverboxsoft.classes.DynamoDbCondition;
import com.silverboxsoft.classes.DynamoDbConditionJoinType;
import com.silverboxsoft.classes.DynamoDbConditionType;
import com.silverboxsoft.classes.DynamoDbResult;
import com.silverboxsoft.dao.DynamicDao;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class DynamoDbToolController implements Initializable {

	@FXML
	TableView<ObservableList<String>> tableResultList;

	@FXML
	TextField txtFldTableName;

	@FXML
	TextField txtFldColumnName;

	@FXML
	TextField txtFldCondValue;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	@FXML
	protected void actLoad(ActionEvent ev) {
		String tableName = txtFldTableName.getText();
		DynamicDao dao = new DynamicDao();
		List<DynamoDbCondition> conditionList = new ArrayList<>();
		DynamoDbCondition cond = new DynamoDbCondition();
		cond.setColumnName(txtFldColumnName.getText());
		cond.setConditionType(DynamoDbConditionType.EQUAL);
		cond.setValue(txtFldCondValue.getText());
		conditionList.add(cond);

		DynamoDbResult result = dao.getResult(tableName, DynamoDbConditionJoinType.AND, conditionList);
		setTable(result);
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
