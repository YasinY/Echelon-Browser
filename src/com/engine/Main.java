package com.engine;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

	public static void main(String[] args) {
		//new Browser();
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("browser.fxml"));
			Scene scene = new Scene(root, 810, 560);
			//scene.getStylesheets().add("stylesheet.css");

			root.setId("pane");
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.setTitle("Browser..");
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
