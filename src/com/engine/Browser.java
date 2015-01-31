package com.engine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.ResourceBundle;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;

public class Browser implements Initializable, Runnable {



	@FXML
	private WebView webView; //Constructing WebView

	private WebEngine webEngine; //Constructing webEngine

	@FXML
	private TextField URLBar; //Constructing TextField

	@FXML
	private ProgressBar progressBar; //Constructing ProgressBar

	@FXML
	private ProgressIndicator progressIndicator; //Constructing ProgressIndicator

	@FXML
	private Text status; //Constructing Text

	@FXML
	private Button searchButton; //Constructing Button
	
	private LinkedList<String> webHistory = new LinkedList<String>(); //Constructing the LinkedList which is being used to store temporary URLS

	private String previousSite;
	
	private String nextSite;



	/**
	 * 
	 * PENDING stands for when user is on a site
	 * LOADING stands for when the browser loads something
	 * FINISHED stands for when the browser loaded something successfully
	 * WAITING stands for when the browser is waiting for something, for instance; a response from a website
	 *
	 */
	public enum Stages {
		PENDING("Waiting for action"), 
		LOADING("Loading.. please wait"), 
		FINISHED("Finished loading"), 
		WAITING("Waiting for response"),
		ERROR("Error loading..");

		private final String status;

		private Stages(final String status) {
			this.status = status;
		}

		public String getStatus() {
			return status;
		}


	}
	/**
	 * Updates the Progress bar, Progress indicator and status text handled through TimeLine
	 */
	public void handleSearch() {
		Timeline timeline = new Timeline(new KeyFrame(Duration.millis(10), event -> {
			Stages s = Stages.LOADING;
			if(webEngine.getLoadWorker().getProgress() == -1) {
				s = Stages.ERROR;
			}
			if(webEngine.getLoadWorker().getProgress() == 0) {
				progressIndicator.setProgress(progressIndicator.getProgress() + 0.1); 
				progressBar.setProgress(progressIndicator.getProgress());
				s = Stages.LOADING;
			}
			if(webEngine.getLoadWorker().getProgress() == 1) {
				progressIndicator.setProgress(0.0);
				progressBar.setProgress(0.0);
				s = Stages.FINISHED;
			}
			status.setText(s.getStatus());
		}));
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.play();
	}
	
	/**
	 * Boolean used to check if the URL is valid
	 * If the clauses are corresponding the requirements, they'll return as TRUE
	 * If not, they'll return as FALSE
	 * @return
	 */
	public boolean URLIsValid() {
		String url = URLBar.getText();
		if(url.isEmpty()) {
			return false;
		}
		if(!url.startsWith("www.") && !url.startsWith("http")) {
			URLBar.setText("http://" + URLBar.getText());
			return true;
		}
		if(url.startsWith("www.")) {
			URLBar.setText("http://www." + URLBar.getText());
			return true;
		}
		
		return false;
	}

	/**
	 * The actual search function
	 * Checks if the URL is valid
	 * If so, it will start to search
	 * If not, it'll return
	 */
	@FXML
	public void search() {
		if(!URLIsValid()) {
			return;
		} else {
		handleSearch();
		loadURL(URLBar.getText());
		writeHistory(URLBar.getText());
		}
	}
	
	@FXML
	public void goBack() {
		loadURL(previousSite);
	}
	
	@FXML
	public void goNext() {
		loadURL(nextSite);
	}


	/**
	 * Is responsible for loading the URL
	 * Uses the WebEngine to initialize the connection
	 * @param URL
	 */
	public void loadURL(String URL) {
		webEngine.load(URL);
	}
	
	//Represents the File history.dat
	File file = new File("history.txt");
	
	/**
	 * Writes the URL to the .dat file
	 * @param URL
	 */
	public void writeHistory(String URL) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			if(!file.exists())
				return;
			writer.write(URL);
			writer.newLine();
		} catch (IOException e) {
			System.out.println("Failed to write");
		}
	}

	/**
	 * Reads the URL's stored in the file
	 * @param URL
	 */
	public void readHistory() {
		if(!file.exists()) {
			return;
		}
		try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while( (line = reader.readLine()) != null) {
				webHistory.add(line);
			}
		} catch (IOException e) {
			System.out.println("Failed to read history");
		}
	}
	
	/**
	 * Gets the webView instance
	 * @return
	 */
	public WebView getWebView() {
		return webView;
	}


	/**
	 * Used for loading
	 */
	@Override
	public void run() {
		webEngine = webView.getEngine();
		webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {

			@Override
			public void changed(ObservableValue<? extends State> arg0,State arg1, State arg2) {
				URLBar.setText(webEngine.getLocation());
			}		
		});
		
	}

	/**
	 * Also used for loading
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Platform.runLater(this);
		readHistory();
		URLBar.setOnKeyPressed(event -> {
			if(event.getCode() == KeyCode.ENTER) {
				search();
			}
		});
	}

	/**
	 * Gets the web history
	 * @return
	 */
	public LinkedList<String> getWebHistory() {
		return webHistory;
	}

	/**
	 * Sends an Interface showing an info
	 * @param title
	 * @param header
	 * @param message
	 */
	public void sendInfoDialogue(String title, String header, String message) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(message);
		alert.show();
	}
	/**
	 * Sends an interface showing a warning
	 * @param title
	 * @param header
	 * @param message
	 */
	public void sendWarningDialogue(String title, String header, String message) {
		Alert alert = new Alert(AlertType.WARNING);
		alert.getDialogPane().getScene().getWindow();
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(message);
		alert.show();
	}
	/**
	 * Sends an interface showing an error
	 * @param title
	 * @param header
	 * @param message
	 */
	public void sendErrorDialogue(String title, String header, String message) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.getDialogPane().getScene().getWindow();
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
