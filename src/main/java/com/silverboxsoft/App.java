package com.silverboxsoft;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		prepareControl(stage);
		stage.show();
	}

	private void prepareControl(Stage stage) throws IOException {

		FXMLLoader fxmlLoader = new FXMLLoader(
				getClass().getResource("controller/javafx/DynamoDbToolController.fxml"));
		VBox newPane = (VBox) fxmlLoader.load();

		Scene scene = new Scene(newPane);
		stage.setTitle("DynamoDB Tool");
		stage.setScene(scene);
	}
}
