package com.silverboxsoft;

import java.io.IOException;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class App extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		prepareControl(stage);
		// prepareTestControl(stage);
		stage.show();
	}

	private void prepareControl(Stage stage) throws IOException {

		FXMLLoader fxmlLoader = new FXMLLoader(
				getClass().getResource("dynamodbtool/controller/javafx/DynamoDbToolController.fxml"));
		VBox newPane = (VBox) fxmlLoader.load();

		Scene scene = new Scene(newPane);
		stage.setTitle("DynamoDB Tool");
		stage.setScene(scene);
	}

	private void prepareTestControl(Stage stage) {
		GridPane grid;
		ScrollPane scrollPane;
		AnchorPane anchorPane;

		Label typelabel = getContentLabel("label test");
		HBox hbox = new HBox();
		hbox.setStyle("-fx-background-color:yellow");
		TextField textField = new TextField("Text field test");
		textField.setMinWidth(300);
		textField.autosize();
		// AnchorPane anchorPane1 = new AnchorPane();
		// anchorPane1.getChildren().add(textField);
		// anchorPane1.setStyle("-fx-background-color:green");
		// AnchorPane.setLeftAnchor(textField, 0.);
		// AnchorPane.setTopAnchor(textField, 0.);
		// AnchorPane.setRightAnchor(textField, 0.);
		// AnchorPane.setBottomAnchor(textField, 0.);
		hbox.getChildren().addAll(textField);
		CheckBox delCheck = new CheckBox();

		Label typelabel2 = getContentLabel("label test2");
		HBox hbox2 = new HBox();
		Label vallabel = getContentLabel(
				"start"
						+ "あいうえおかきくけこさしすせそたちつてとあいうえおかきくけこさしすせそたちつてと"
						+ "あいうえおかきくけこさしすせそたちつてとあいうえおかきくけこさしすせそたちつてと"
						// + "あいうえおかきくけこさしすせそたちつてとあいうえおかきくけこさしすせそたちつてと"
						// + "あいうえおかきくけこさしすせそたちつてとあいうえおかきくけこさしすせそたちつてと"
						// + "あいうえおかきくけこさしすせそたちつてとあいうえおかきくけこさしすせそたちつてと"
						// + "あいうえおかきくけこさしすせそたちつてとあいうえおかきくけこさしすせそたちつてと"
						// + "あいうえおかきくけこさしすせそたちつてとあいうえおかきくけこさしすせそたちつてと"
						// + "あいうえおかきくけこさしすせそたちつてとあいうえおかきくけこさしすせそたちつてと"
						+ "end");
		hbox2.getChildren().addAll(vallabel);
		CheckBox delCheck2 = new CheckBox();

		grid = new GridPane();
		grid.setHgap(20);
		grid.setVgap(5);
		grid.setMaxWidth(Double.MAX_VALUE);
		grid.setAlignment(Pos.CENTER);
		// grid.setStyle("-fx-background-color:lightgray");
		grid.add(typelabel, 0, 0);
		grid.add(textField, 1, 0);
		grid.add(delCheck, 2, 0);
		grid.add(typelabel2, 0, 1);
		grid.add(hbox2, 1, 1);
		grid.add(delCheck2, 2, 1);

		for (int i = 0; i < 30; i++) {
			Label wklabel = getContentLabel(String.format("No. %1d row", i));
			grid.add(wklabel, 0, i + 2);
		}
		anchorPane = new AnchorPane();
		anchorPane.getChildren().add(grid);
		anchorPane.setStyle("-fx-background-color:green");
		AnchorPane.setLeftAnchor(grid, 0.);
		AnchorPane.setTopAnchor(grid, 0.);
		AnchorPane.setRightAnchor(grid, 0.);
		AnchorPane.setBottomAnchor(grid, 0.);
		scrollPane = new ScrollPane();
		scrollPane.setContent(anchorPane);
		scrollPane.setStyle("-fx-background-color:red");

		Rectangle2D screenSize2d = Screen.getPrimary().getVisualBounds();
		scrollPane.setMaxWidth(screenSize2d.getWidth() / 2);
		scrollPane.setMaxHeight(screenSize2d.getHeight() / 2);

		ColumnConstraints colContraint0 = new ColumnConstraints();
		ColumnConstraints colContraint1 = new ColumnConstraints();
		ColumnConstraints colContraint2 = new ColumnConstraints();
		colContraint0.setPrefWidth(100);
		colContraint1.setPrefWidth(300);
		colContraint2.setPrefWidth(100);
		grid.getColumnConstraints().add(colContraint0);
		grid.getColumnConstraints().add(colContraint1);
		grid.getColumnConstraints().add(colContraint2);

		scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
		ChangeListener<Number> stageSizeListener = (observable, oldValue, newValue) -> {
			grid.getColumnConstraints().get(1).setPrefWidth(newValue.doubleValue() - 200.0);
		};
		scrollPane.widthProperty().addListener(stageSizeListener);

		Scene scene = new Scene(scrollPane);
		stage.setTitle("Test dialog");
		stage.setScene(scene);
	}

	// same as DialogPane.createContentLabel
	private Label getContentLabel(String text) {
		Label label = new Label(text);
		label.setMaxWidth(Double.MAX_VALUE);
		label.setMaxHeight(Double.MAX_VALUE);
		label.getStyleClass().add("content");
		label.setWrapText(true);
		label.setText(text);
		return label;
	}
}
