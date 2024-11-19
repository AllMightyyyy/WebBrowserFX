package org.zakaria.webbrowserfxx.controller;

import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;

import java.net.MalformedURLException;
import java.net.URL;

public class BrowserController {

    @FXML
    private WebView webView;

    @FXML
    private TextField urlField;

    @FXML
    private Button backButton;

    @FXML
    private Button forwardButton;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label statusLabel;

    private WebEngine webEngine;
    private WebHistory history;

    @FXML
    public void initialize() {
        webEngine = webView.getEngine();
        history = webEngine.getHistory();

        // Bind progress bar to loading progress
        webEngine.getLoadWorker().progressProperty().addListener((obs, oldProgress, newProgress) -> {
            progressBar.setProgress(newProgress.doubleValue());
        });

        // Update status label based on worker state
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            switch (newState) {
                case RUNNING:
                    statusLabel.setText("Loading...");
                    break;
                case SUCCEEDED:
                    statusLabel.setText("Finished Loading");
                    break;
                case FAILED:
                    statusLabel.setText("Failed to Load Page");
                    break;
                default:
                    statusLabel.setText("Ready");
            }
            updateNavigationButtons();
        });

        // Update URL field when location changes
        webEngine.locationProperty().addListener((observable, oldValue, newValue) -> {
            urlField.setText(newValue);
        });

        // Listen for load exceptions
        webEngine.getLoadWorker().exceptionProperty().addListener((obs, oldException, newException) -> {
            if (webEngine.getLoadWorker().getState() == Worker.State.FAILED && newException != null) {
                handleLoadException((Exception) newException, webEngine.getLocation());
            }
        });

        // Initialize with a default page
        loadPage("https://www.google.com");
    }

    @FXML
    public void loadPage() {
        String url = urlField.getText().trim();
        loadPage(url);
    }

    public void loadPage(String url) {
        // Validate and correct the URL if necessary
        if (!url.matches("^(http|https)://.*$")) {
            url = "http://" + url;
        }

        try {
            // Parse URL for validation
            new URL(url); // Throws MalformedURLException if invalid
            webEngine.load(url);
            statusLabel.setText("Loading: " + url);
        } catch (MalformedURLException e) {
            handleLoadException(e, url);
        }
    }

    @FXML
    public void goBack() {
        if (history.getCurrentIndex() > 0) {
            history.go(-1);
        }
    }

    @FXML
    public void goForward() {
        if (history.getCurrentIndex() < history.getEntries().size() - 1) {
            history.go(1);
        }
    }

    @FXML
    public void refreshPage() {
        webEngine.reload();
    }

    @FXML
    public void stopLoading() {
        webEngine.getLoadWorker().cancel();
        statusLabel.setText("Loading Stopped");
    }

    private void updateNavigationButtons() {
        backButton.setDisable(history.getCurrentIndex() <= 0);
        forwardButton.setDisable(history.getCurrentIndex() >= history.getEntries().size() - 1);
    }

    private void handleLoadException(Exception e, String url) {
        statusLabel.setText("Error loading: " + url);
        showAlert("Load Error", "Could not load the page:\n" + url + "\n\nError: " + e.getMessage(), Alert.AlertType.ERROR);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
