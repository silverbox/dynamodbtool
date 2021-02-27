package com.silverboxsoft.dynamodbtool.controller.inputdialog;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public abstract class AbsDynamoDbInputDialog<R> extends Dialog<R> {

	private final GridPane grid;
	private R dynamoDbRecordOrg;
	List<List<Node>> orgBodyAttribueNodeList = new ArrayList<>();
	List<List<Node>> addBodyAttribueNodeList = new ArrayList<>();

	protected static final int HGAP = 20;
	protected static final int VGAP = 5;
	protected static final int FILELD_WIDTH = 300;
	protected static final String STRFLD_ID_PREFIX = "txtEdit_";
	protected static final String NUMFLD_ID_PREFIX = "numEdit_";
	protected static final String BINFLD_ID_PREFIX = "binEdit_";
	protected static final String VALLBL_ID_PREFIX = "valLabel_";
	protected static final String BTN_ID_PREFIX = "btnEdit_";
	protected static final String DEL_ID_PREFIX = "ckbDel_";
	protected static final String BTN_TITLE = "Edit";

	public AbsDynamoDbInputDialog(R dynamoDbRecord) {
		this.setResizable(true);
		this.dynamoDbRecordOrg = dynamoDbRecord;
		this.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

		setResultConverter((dialogButton) -> {
			ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
			return data == ButtonData.OK_DONE
					? getCurrentDynamoDbRecord()
					: null;
		});

		this.grid = new GridPane();
		this.grid.setHgap(HGAP);
		this.grid.setVgap(VGAP);
		this.grid.setMaxWidth(Double.MAX_VALUE);
		this.grid.setAlignment(Pos.CENTER);
		this.initialize();
	}

	abstract R getCurrentDynamoDbRecord();

	protected void initialize() {
		getGridPane().getChildren().clear();

		List<Node> labelList = getHeaderLabelList();
		for (int cIdx = 0; cIdx < labelList.size(); cIdx++) {
			getGridPane().add(labelList.get(cIdx), cIdx, 0);
		}

		orgBodyAttribueNodeList = getBodyAttribueNodeList();
		for (int rIdx = 0; rIdx < orgBodyAttribueNodeList.size(); rIdx++) {
			List<Node> nodelList = orgBodyAttribueNodeList.get(rIdx);
			for (int cIdx = 0; cIdx < nodelList.size(); cIdx++) {
				getGridPane().add(nodelList.get(cIdx), cIdx, rIdx + 1);
			}
		}
		getDialogPane().setContent(getGridPane());
	}

	abstract List<Node> getHeaderLabelList();

	abstract List<List<Node>> getBodyAttribueNodeList();

	protected void addAttributeNodeList(List<Node> newNodeList) {
		int newIdx = orgBodyAttribueNodeList.size() + addBodyAttribueNodeList.size();
		for (int cIdx = 0; cIdx < newNodeList.size(); cIdx++) {
			getGridPane().add(newNodeList.get(cIdx), cIdx, newIdx + 1);
		}
	};

	protected GridPane getGridPane() {
		return this.grid;
	}

	protected R getDynamoDbRecordOrg() {
		return dynamoDbRecordOrg;
	}

	protected List<List<Node>> getCurrentBodyNodeList() {
		List<List<Node>> curNodeList = new ArrayList<>();
		curNodeList.addAll(orgBodyAttribueNodeList);
		curNodeList.addAll(addBodyAttribueNodeList);
		return curNodeList;
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
		label.setWrapText(false);
		label.setTextOverrun(OverrunStyle.ELLIPSIS);
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

	protected boolean getBooleanValue(HBox hbox) {
		RadioButton radioButtonTrue = (RadioButton) hbox.getChildren().get(0);
		return radioButtonTrue.isSelected();
	}

	protected HBox getNullViewLabel() {
		HBox hbox = new HBox(HGAP);
		Label vallabel = getContentLabel("<null>");
		hbox.getChildren().addAll(vallabel);
		return hbox;
	}
}
