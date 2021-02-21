package com.silverboxsoft.dynamodbtool.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public abstract class AbsDynamoDbInputDialog<R> extends Dialog<R> {

	private final GridPane grid;
	private R dynamoDbRecord;

	protected static final int HGAP = 20;
	protected static final int VGAP = 5;
	protected static final int FILELD_WIDTH = 300;
	protected static final String BTN_ID_PREFIX = "btnEdit";
	protected static final String BTN_TITLE = "Edit";

	public AbsDynamoDbInputDialog(R dynamoDbRecord) {
		this.setResizable(true);
		this.dynamoDbRecord = dynamoDbRecord;
		this.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

		setResultConverter((dialogButton) -> {
			ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
			return data == ButtonData.OK_DONE ? dynamoDbRecord : null;
		});

		this.grid = new GridPane();
		this.grid.setHgap(HGAP);
		this.grid.setVgap(VGAP);
		this.grid.setMaxWidth(Double.MAX_VALUE);
		this.grid.setAlignment(Pos.CENTER);
		initialize();
	}

	private void initialize() {
		getGridPane().getChildren().clear();

		List<Node> labelList = getHeaderLabelList();
		for (int cIdx = 0; cIdx < labelList.size(); cIdx++) {
			getGridPane().add(labelList.get(cIdx), cIdx, 0);
		}

		List<List<Node>> bodyAttribueNodeList = getBodyAttribueNodeList();
		for (int rIdx = 0; rIdx < bodyAttribueNodeList.size(); rIdx++) {
			List<Node> nodelList = bodyAttribueNodeList.get(rIdx);
			for (int cIdx = 0; cIdx < nodelList.size(); cIdx++) {
				getGridPane().add(nodelList.get(cIdx), cIdx, rIdx + 1);
			}
		}
		getDialogPane().setContent(getGridPane());
	}

	abstract List<Node> getHeaderLabelList();

	abstract List<List<Node>> getBodyAttribueNodeList();

	abstract AttributeValue getAttributeValue(String btnId);

	protected GridPane getGridPane() {
		return this.grid;
	}

	protected R getDynamoDbRecord() {
		return dynamoDbRecord;
	}

	protected Label getContentLabel(String text, boolean isBold) {
		Label label = getContentLabel(text);
		label.setStyle("-fx-font-weight: bold;");
		return label;
	}

	// same as DialogPane.createContentLabel
	protected Label getContentLabel(String text) {
		Label label = new Label(text);
		label.setMaxWidth(Double.MAX_VALUE);
		label.setMaxHeight(Double.MAX_VALUE);
		label.getStyleClass().add("content");
		label.setWrapText(true);
		label.setText(text);
		return label;
	}

	protected HBox getBooleanInput(AttributeValue attrVal) {
		HBox hbox = new HBox(HGAP);
		RadioButton radioButtonTrue = new RadioButton();
		RadioButton radioButtonFalse = new RadioButton();
		radioButtonTrue.setText("TRUE");
		radioButtonFalse.setText("FALSE");
		ToggleGroup toggleGroup = new ToggleGroup();
		radioButtonTrue.setToggleGroup(toggleGroup);
		radioButtonFalse.setToggleGroup(toggleGroup);
		radioButtonTrue.setSelected(attrVal.bool());
		radioButtonFalse.setSelected(!attrVal.bool());
		hbox.getChildren().addAll(radioButtonTrue, radioButtonFalse);
		return hbox;
	}

	protected HBox getNullViewLabel() {
		HBox hbox = new HBox(HGAP);
		Label vallabel = getContentLabel("<null>");
		hbox.getChildren().addAll(vallabel);
		return hbox;
	}

	protected void actOpenEditDialog(String btnId) {
		AttributeValue attrVal = getAttributeValue(btnId);
		if (attrVal.hasSs()) {
			DynamoDbStringSetInputDialog dialog = new DynamoDbStringSetInputDialog(attrVal.ss());
			Optional<List<String>> newRec = dialog.showAndWait();
			if (newRec.isPresent()) {
				// TODO
			}
		} else if (attrVal.hasNs()) {
			DynamoDbNumberSetInputDialog dialog = new DynamoDbNumberSetInputDialog(attrVal.ns());
			Optional<List<String>> newRec = dialog.showAndWait();
			if (newRec.isPresent()) {
				// TODO
			}
		} else if (attrVal.hasBs()) {
			DynamoDbBinarySetInputDialog dialog = new DynamoDbBinarySetInputDialog(attrVal.bs());
			Optional<List<SdkBytes>> newRec = dialog.showAndWait();
			if (newRec.isPresent()) {
				// TODO
			}
		} else if (attrVal.hasM()) {
			DynamoDbMapInputDialog dialog = new DynamoDbMapInputDialog(attrVal.m());
			Optional<Map<String, AttributeValue>> newRec = dialog.showAndWait();
			if (newRec.isPresent()) {
				// TODO
			}
		} else if (attrVal.hasL()) {
			DynamoDbListInputDialog dialog = new DynamoDbListInputDialog(attrVal.l());
			Optional<List<AttributeValue>> newRec = dialog.showAndWait();
			if (newRec.isPresent()) {
				// TODO
			}
		}
	}
}
