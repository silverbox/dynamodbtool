package com.silverboxsoft.dynamodbtool.controller.inputdialog;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public abstract class AbsDynamoDbInputDialog<R> extends Dialog<R> {

	private final GridPane headGridPane;
	private final GridPane gridPane;
	private final ScrollPane scrollPane;
	private final AnchorPane anchorPane;
	private R dynamoDbRecordOrg;
	List<List<Node>> orgBodyAttribueNodeList = new ArrayList<>();
	List<List<Node>> addBodyAttribueNodeList = new ArrayList<>();

	protected static final int HGAP = 20;
	protected static final int VGAP = 5;
	protected static final int FILELD_WIDTH = 200;
	protected static final int NAME_COL_WIDTH = 100;
	protected static final int DEL_COL_WIDTH = 50;
	protected static final int GRID_MARGIN = 70;

	protected static final String STRFLD_ID_PREFIX = "txtEdit_";
	protected static final String NUMFLD_ID_PREFIX = "numEdit_";
	protected static final String BINFLD_ID_PREFIX = "binEdit_";
	protected static final String VALLBL_ID_PREFIX = "valLabel_";
	protected static final String BTN_ID_PREFIX = "btnEdit_";
	protected static final String DEL_ID_PREFIX = "ckbDel_";
	protected static final String BTN_TITLE = "Edit";

	abstract List<Integer> getHeaderWidthList();

	abstract int getValueColIndex();

	abstract List<Node> getHeaderLabelList();

	abstract List<List<Node>> getBodyAttribueNodeList();

	abstract R getCurrentDynamoDbRecord();

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

		headGridPane = new GridPane();
		headGridPane.setHgap(HGAP);
		headGridPane.setVgap(VGAP);
		headGridPane.setMaxWidth(Double.MAX_VALUE);
		headGridPane.setAlignment(Pos.CENTER);
		gridPane = new GridPane();
		gridPane.setHgap(HGAP);
		gridPane.setVgap(VGAP);
		gridPane.setMaxWidth(Double.MAX_VALUE);
		gridPane.setAlignment(Pos.CENTER);
		// grid.setStyle("-fx-background-color:blue");
		anchorPane = new AnchorPane();
		anchorPane.getChildren().add(gridPane);
		// anchorPane.setStyle("-fx-background-color:green");
		AnchorPane.setLeftAnchor(gridPane, 0.);
		AnchorPane.setTopAnchor(gridPane, 0.);
		AnchorPane.setRightAnchor(gridPane, 0.);
		AnchorPane.setBottomAnchor(gridPane, 0.);
		scrollPane = new ScrollPane();
		scrollPane.setContent(anchorPane);

		Rectangle2D screenSize2d = Screen.getPrimary().getVisualBounds();
		scrollPane.setMaxWidth(screenSize2d.getWidth() / 2);
		scrollPane.setMaxHeight(screenSize2d.getHeight() / 2);

		VBox vBox = new VBox();
		vBox.getChildren().addAll(headGridPane, scrollPane);

		getDialogPane().setContent(vBox);

		this.initialize();
		this.setColumnConstraints();
	}

	protected void initialize() {

		headGridPane.getChildren().clear();
		List<Node> labelList = getHeaderLabelList();
		for (int cIdx = 0; cIdx < labelList.size(); cIdx++) {
			headGridPane.add(labelList.get(cIdx), cIdx, 0);
		}

		getGridPane().getChildren().clear();
		orgBodyAttribueNodeList = getBodyAttribueNodeList();
		for (int rIdx = 0; rIdx < orgBodyAttribueNodeList.size(); rIdx++) {
			List<Node> nodelList = orgBodyAttribueNodeList.get(rIdx);
			for (int cIdx = 0; cIdx < nodelList.size(); cIdx++) {
				getGridPane().add(nodelList.get(cIdx), cIdx, rIdx + 1);
			}
		}
	}

	private void setColumnConstraints() {
		int offset = 0;
		for (int idx = 0; idx < getHeaderWidthList().size(); idx++) {
			Integer wkWidth = getHeaderWidthList().get(idx);
			ColumnConstraints wkColContraint = new ColumnConstraints();
			wkColContraint.setPrefWidth(wkWidth);
			gridPane.getColumnConstraints().add(wkColContraint);
			headGridPane.getColumnConstraints().add(wkColContraint);
			if (idx != getValueColIndex()) {
				offset += wkWidth;
			}
		}

		scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
		final int finalOffset = offset;
		ChangeListener<Number> stageSizeListener = (observable, oldValue, newValue) -> {
			double newColWidth = newValue.doubleValue() - finalOffset - HGAP * (getHeaderWidthList().size() - 1);
			gridPane.getColumnConstraints().get(getValueColIndex()).setPrefWidth(newColWidth);
			headGridPane.getColumnConstraints().get(getValueColIndex()).setPrefWidth(newColWidth);
		};
		scrollPane.widthProperty().addListener(stageSizeListener);
	};

	protected void addAttributeNodeList(List<Node> newNodeList) {
		int newIdx = orgBodyAttribueNodeList.size() + addBodyAttribueNodeList.size();
		for (int cIdx = 0; cIdx < newNodeList.size(); cIdx++) {
			getGridPane().add(newNodeList.get(cIdx), cIdx, newIdx + 1);
		}
	};

	protected GridPane getGridPane() {
		return this.gridPane;
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

	protected Node getNullViewLabel() {
		Label vallabel = getContentLabel("<null>");
		return vallabel;
	}
}
