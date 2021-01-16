package com.silverboxsoft.controller;

import java.net.URL;
import java.util.ResourceBundle;

import com.silverboxsoft.dao.AccountBalanceDao;
import com.silverboxsoft.model.AccountBalance;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class DynamoDbToolController implements Initializable {

	@FXML
	TableView<AccountBalance> tableResultList;

	@FXML
	TableColumn<AccountBalance, String> tableColTgtDate;

	@FXML
	TableColumn<AccountBalance, String> tableColTargetCd;

	@FXML
	TableColumn<AccountBalance, Long> tableColValue;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		setTableColumns();
	}

	@FXML
	protected void actLoad(ActionEvent ev) {
		AccountBalanceDao dao = new AccountBalanceDao();

		tableResultList.getItems().clear();
		tableResultList.getItems().addAll(dao.getBalanceListAtOneDay("20200208"));
	}

	private void setTableColumns() {
		tableColTgtDate.setCellValueFactory(new PropertyValueFactory<AccountBalance, String>("tgtDate"));
		tableColTargetCd.setCellValueFactory(new PropertyValueFactory<AccountBalance, String>("methodCd"));
		tableColValue.setCellValueFactory(new PropertyValueFactory<AccountBalance, Long>("value"));
	}

}
