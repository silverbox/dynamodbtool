package com.silverboxsoft.dynamodbtool.controller.inputdialog;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public abstract class AbsDynamoDbInputDialog<R> extends Dialog<R> {

	private final GridPane headGridPane;
	private final GridPane gridPane;
	private final GridPane footGridPane;
	private final ScrollPane scrollPane;
	private final AnchorPane gridAnchorPane;
	private final Button addButton;

	private R dynamoDbRecordOrg;
	List<List<Node>> orgBodyAttribueNodeList = new ArrayList<>();
	List<List<Node>> addBodyAttribueNodeList = new ArrayList<>();

	protected static final int HGAP = 20;
	protected static final int VGAP = 5;
	protected static final int FILELD_WIDTH = 300;
	protected static final int NAME_COL_WIDTH = 150;
	protected static final int DEL_COL_WIDTH = 50;

	protected static final String STRFLD_ID_PREFIX = "txtEdit_";
	protected static final String NUMFLD_ID_PREFIX = "numEdit_";
	protected static final String BINFLD_ID_PREFIX = "binEdit_";
	protected static final String VALLBL_ID_PREFIX = "valLabel_";
	protected static final String EDTBTN_ID_PREFIX = "btnEdit_";
	protected static final String ADDBTN_ID = "btnAddAttribute";
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
		gridAnchorPane = new AnchorPane();
		gridAnchorPane.getChildren().add(gridPane);
		AnchorPane.setLeftAnchor(gridPane, 0.);
		AnchorPane.setTopAnchor(gridPane, 5.);
		AnchorPane.setRightAnchor(gridPane, 0.);
		AnchorPane.setBottomAnchor(gridPane, 5.);

		scrollPane = new ScrollPane();
		scrollPane.setContent(gridAnchorPane);

		footGridPane = new GridPane();
		footGridPane.setHgap(HGAP);
		footGridPane.setVgap(VGAP);
		footGridPane.setMaxWidth(Double.MAX_VALUE);
		footGridPane.setAlignment(Pos.CENTER);
		// footGridPane.setStyle("-fx-padding-top: 15;-fx-spacing-top: 10;-fx-background-color:green");
		HBox footBox = new HBox();
		Insets footInset = new Insets(5., 0., 0., 0.);
		HBox.setMargin(footGridPane, footInset);
		footBox.getChildren().add(footGridPane);

		BorderPane borderPane = new BorderPane();
		borderPane.setCenter(scrollPane);
		borderPane.setTop(headGridPane);
		borderPane.setBottom(footBox);

		addButton = new Button("Add");
		addButton.setId(ADDBTN_ID);

		getDialogPane().setContent(borderPane);

		this.initialize();
		this.setColumnConstraints();

		Rectangle2D screenSize2d = Screen.getPrimary().getVisualBounds();
		double screenHeight = screenSize2d.getHeight();
		double screenWidth = screenSize2d.getWidth();

		final Window window = this.getDialogPane().getScene().getWindow();
		Stage stage = (Stage) window;
		stage.setMinHeight(200);
		stage.setMinWidth(300);
		stage.setMaxHeight(screenHeight);
		stage.setMaxWidth(screenWidth);
	}

	abstract List<Integer> getHeaderWidthList();

	abstract int getValueColIndex();

	abstract List<Node> getHeaderLabelList();

	abstract List<List<Node>> getBodyAttributeNodeList();

	abstract List<Node> getFooterNodeList();

	abstract R getCurrentDynamoDbRecord();

	abstract void actAddScalarAttribute();

	protected void initialize() {

		headGridPane.getChildren().clear();
		List<Node> labelList = getHeaderLabelList();
		for (int cIdx = 0; cIdx < labelList.size(); cIdx++) {
			headGridPane.add(labelList.get(cIdx), cIdx, 0);
		}

		getGridPane().getChildren().clear();
		orgBodyAttribueNodeList = getBodyAttributeNodeList();
		for (int rIdx = 0; rIdx < orgBodyAttribueNodeList.size(); rIdx++) {
			List<Node> nodelList = orgBodyAttribueNodeList.get(rIdx);
			for (int cIdx = 0; cIdx < nodelList.size(); cIdx++) {
				getGridPane().add(nodelList.get(cIdx), cIdx, rIdx);
			}
		}

		updateFooter();
	}

	private void setColumnConstraints() {
		int offset = 0;
		for (int idx = 0; idx < getHeaderWidthList().size(); idx++) {
			Integer wkWidth = getHeaderWidthList().get(idx);
			ColumnConstraints wkColContraint = new ColumnConstraints();
			wkColContraint.setPrefWidth(wkWidth);
			gridPane.getColumnConstraints().add(wkColContraint);
			headGridPane.getColumnConstraints().add(wkColContraint);
			footGridPane.getColumnConstraints().add(wkColContraint);
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
			footGridPane.getColumnConstraints().get(getValueColIndex()).setPrefWidth(newColWidth);
		};
		scrollPane.widthProperty().addListener(stageSizeListener);
	};

	protected void addAttributeNodeList(List<Node> newNodeList) {
		int newIdx = orgBodyAttribueNodeList.size() + addBodyAttribueNodeList.size();
		for (int cIdx = 0; cIdx < newNodeList.size(); cIdx++) {
			getGridPane().add(newNodeList.get(cIdx), cIdx, newIdx);
		}
		addBodyAttribueNodeList.add(newNodeList);
	};

	protected void updateFooter() {
		footGridPane.getChildren().clear();
		List<Node> footerNode = getFooterNodeList();
		for (int cIdx = 0; cIdx < footerNode.size(); cIdx++) {
			footGridPane.add(footerNode.get(cIdx), cIdx, 0);
		}
	}

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

	protected Node getNullViewLabel() {
		Label vallabel = getContentLabel("<null>");
		return vallabel;
	}

	protected GridPane getFootGridPane() {
		return footGridPane;
	}

	protected Button getAddButton() {
		return addButton;
	}
}
