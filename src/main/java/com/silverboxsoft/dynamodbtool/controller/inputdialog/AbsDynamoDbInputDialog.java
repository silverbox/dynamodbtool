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
import javafx.scene.control.DialogEvent;
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
import javafx.scene.layout.RowConstraints;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public abstract class AbsDynamoDbInputDialog<R> extends Dialog<R> {

	protected static final String VALIDATION_MSG_INVALID_VALUE = "Invalid value.";

	private final GridPane headGridPane;
	private final GridPane gridPane;
	private final GridPane footGridPane;
	private final ScrollPane scrollPane;
	private final AnchorPane gridAnchorPane;
	private final Button addButton;

	private ButtonData buttonData;
	private boolean isValidData = false;

	private R dynamoDbRecordOrg;
	List<List<Node>> orgBodyAttribueNodeList = new ArrayList<>();
	List<List<Node>> addBodyAttribueNodeList = new ArrayList<>();

	protected static final int HGAP = 5;
	protected static final int VGAP = 5;
	protected static final int FILELD_WIDTH = 300;
	protected static final int NAME_COL_WIDTH = 150;
	protected static final int DEL_COL_WIDTH = 50;
	protected static final int GRID_MIN_HEIGHT = 25;

	protected static final String STRFLD_ID_PREFIX = "txtEdit_";
	protected static final String NUMFLD_ID_PREFIX = "numEdit_";
	protected static final String BINFLD_ID_PREFIX = "binEdit_";
	protected static final String VALLBL_ID_PREFIX = "valLabel_";
	protected static final String EDTBTN_ID_PREFIX = "btnEdit_";
	protected static final String ADDBTN_ID = "btnAddAttribute";
	protected static final String DEL_ID_PREFIX = "ckbDel_";
	protected static final String BTN_TITLE = "Edit";

	public AbsDynamoDbInputDialog(R dynamoDbRecord, String dialogTitle) {
		this.setResizable(true);
		this.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		this.setTitle(dialogTitle);
		this.setOnCloseRequest(this::doFinalConfirmation);

		dynamoDbRecordOrg = dynamoDbRecord;

		setResultConverter((dialogButton) -> {
			buttonData = dialogButton == null ? null : dialogButton.getButtonData();
			if (buttonData == ButtonData.OK_DONE) {
				isValidData = isFinalValidationOk();
				return isValidData ? getEditedDynamoDbRecord() : getEmptyAttr();
			}
			return null;
		});

		headGridPane = new GridPane();
		gridPane = new GridPane();
		gridAnchorPane = new AnchorPane();
		scrollPane = new ScrollPane();
		footGridPane = new GridPane();
		addButton = new Button("Add");
		setupComponent();

		initialize();
		setColumnConstraints();
		setupScreen();
	}

	/*
	 * for setup
	 */
	abstract List<Integer> getHeaderWidthList();

	abstract List<Node> getHeaderLabelList();

	abstract List<List<Node>> getBodyAttributeNodeList();

	abstract List<Node> getFooterNodeList();

	/*
	 * for screen resize
	 */
	abstract int getValueColIndex();

	/*
	 * for return dialog result
	 */
	abstract R getEditedDynamoDbRecord();

	abstract R getEmptyAttr();

	/*
	 * for add attribute action
	 */
	abstract void actAddNewAttribute();

	/*
	 * for final validation
	 */
	abstract boolean isFinalValidationOk();

	/*
	 * accessor for inherited class
	 */
	protected GridPane getGridPane() {
		return this.gridPane;
	}

	protected R getDynamoDbRecordOrg() {
		return dynamoDbRecordOrg;
	}

	protected GridPane getFootGridPane() {
		return footGridPane;
	}

	protected Button getAddButton() {
		return addButton;
	}

	protected ButtonData getButtonData() {
		return buttonData;
	}

	/*
	 * utility function
	 */
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

	/*
	 * static function
	 */
	// same as DialogPane.createContentLabel
	protected static Label getContentLabel(String text) {
		Label label = new Label(text);
		label.setMaxWidth(Double.MAX_VALUE);
		label.setMaxHeight(Double.MAX_VALUE);
		label.getStyleClass().add("content");
		label.setWrapText(false);
		label.setTextOverrun(OverrunStyle.ELLIPSIS);
		label.setText(text);
		return label;
	}

	protected static HBox getBooleanInput(AttributeValue attrVal) {
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

	protected static Node getNullViewLabel() {
		Label vallabel = getContentLabel("<null>");
		return vallabel;
	}

	protected static boolean getBooleanValue(HBox hbox) {
		RadioButton radioButtonTrue = (RadioButton) hbox.getChildren().get(0);
		return radioButtonTrue.isSelected();
	}

	protected static Node addUnderlineStyleToNode(Node node) {
		String wkStyle = node.getStyle();
		StringBuilder sb_style = new StringBuilder(wkStyle);
		sb_style.append("-fx-border-style: none none solid none;");
		sb_style.append("-fx-border-color: white white lightgray white;");
		sb_style.append("-fx-border-width: 0 0 1 0;");
		node.setStyle(sb_style.toString());
		return node;
	}

	protected void doFinalConfirmation(DialogEvent dialogEvent) {
		if (buttonData != ButtonData.OK_DONE) {
			return;
		}
		if (!isValidData) {
			dialogEvent.consume();
		}
	}

	/*
	 * class methods
	 */
	protected void setupComponent() {

		headGridPane.setHgap(HGAP);
		headGridPane.setVgap(VGAP);
		headGridPane.setMaxWidth(Double.MAX_VALUE);
		headGridPane.setMinHeight(GRID_MIN_HEIGHT);
		headGridPane.setAlignment(Pos.CENTER);

		gridPane.setHgap(HGAP);
		gridPane.setVgap(VGAP);
		gridPane.setMaxWidth(Double.MAX_VALUE);
		gridPane.setAlignment(Pos.CENTER);
		gridPane.setMinHeight(GRID_MIN_HEIGHT);
		// gridPane.setStyle();
		gridAnchorPane.getChildren().add(gridPane);
		AnchorPane.setLeftAnchor(gridPane, 0.);
		AnchorPane.setTopAnchor(gridPane, 5.);
		AnchorPane.setRightAnchor(gridPane, 0.);
		AnchorPane.setBottomAnchor(gridPane, 5.);
		scrollPane.setContent(gridAnchorPane);

		footGridPane.setHgap(HGAP);
		footGridPane.setVgap(VGAP);
		footGridPane.setMaxWidth(Double.MAX_VALUE);
		footGridPane.setMinHeight(GRID_MIN_HEIGHT);
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

		addButton.setId(ADDBTN_ID);
		addButton.setOnAction((event) -> {
			actAddNewAttribute();
		});
		getDialogPane().setContent(borderPane);
	}

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
			RowConstraints wkRowConstraint = new RowConstraints();
			wkRowConstraint.setMinHeight(GRID_MIN_HEIGHT);
			getGridPane().getRowConstraints().add(wkRowConstraint);
		}

		updateFooter();
	}

	protected void setupScreen() {
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

	private void setColumnConstraints() {
		int offset = 0;
		for (int idx = 0; idx < getHeaderWidthList().size(); idx++) {
			Integer wkWidth = getHeaderWidthList().get(idx);
			ColumnConstraints wkColConstraint = new ColumnConstraints();
			wkColConstraint.setPrefWidth(wkWidth);
			gridPane.getColumnConstraints().add(wkColConstraint);
			headGridPane.getColumnConstraints().add(wkColConstraint);
			footGridPane.getColumnConstraints().add(wkColConstraint);
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
}
