// File: src/main/java/org/zakaria/webbrowserfxx/controller/BrowserTabController.java
package org.zakaria.webbrowserfxx.controller;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import org.zakaria.webbrowserfxx.model.HistoryManager;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BrowserTabController {

    @FXML
    private WebView webView;

    private WebEngine webEngine;
    private WebHistory history;

    private HistoryManager historyManager;
    private BrowserController mainController;
    private Tab tab;

    private Runnable onPageLoadCallback;

    private ToolbarController toolbarController;

    private static final Logger LOGGER = Logger.getLogger(BrowserTabController.class.getName());

    public void setMainController(BrowserController controller) {
        this.mainController = controller;
    }

    public void setTab(Tab tab) {
        this.tab = tab;
        tab.setOnClosed(event -> {
            // Cleanup if necessary
            dispose();
            LOGGER.info("Tab closed: " + tab.getText());
        });
    }

    @FXML
    public void initialize() {
        webEngine = webView.getEngine();
        history = webEngine.getHistory();

        // Initialize history manager
        historyManager = HistoryManager.getInstance();

        // Bind progress bar to loading progress via main controller
        webEngine.getLoadWorker().progressProperty().addListener((obs, oldProgress, newProgress) -> {
            if (mainController != null) {
                Platform.runLater(() -> mainController.progressBar.setProgress(newProgress.doubleValue()));
            }
        });

        // Update status label based on worker state via main controller
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (mainController != null) {
                Platform.runLater(() -> {
                    switch (newState) {
                        case RUNNING:
                            mainController.statusLabel.setText("Loading...");
                            break;
                        case SUCCEEDED:
                            mainController.statusLabel.setText("Finished Loading");
                            historyManager.addHistory(webEngine.getLocation());
                            tab.setText(webEngine.getTitle() != null ? webEngine.getTitle() : "New Tab");
                            break;
                        case FAILED:
                            mainController.statusLabel.setText("Failed to Load Page");
                            break;
                        default:
                            mainController.statusLabel.setText("Ready");
                    }
                    updateNavigationButtons();
                });
            }
        });

        // Update URL field when location changes via ToolbarController
        webEngine.locationProperty().addListener((observable, oldValue, newValue) -> {
            if (toolbarController != null) {
                toolbarController.updateUrlField(newValue);
            }
        });

        // Listen for load exceptions
        webEngine.getLoadWorker().exceptionProperty().addListener((obs, oldException, newException) -> {
            if (webEngine.getLoadWorker().getState() == Worker.State.FAILED && newException != null) {
                handleLoadException((Exception) newException, webEngine.getLocation());
            }
        });

        // Handle downloads
        webEngine.setCreatePopupHandler(config -> {
            // Prevent pop-ups
            LOGGER.info("Popup creation attempted and blocked.");
            return null;
        });

        // Handle download upon successful load
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                handleDownload(webEngine.getLocation());
                if (onPageLoadCallback != null) {
                    onPageLoadCallback.run();
                }
            }
            updateNavigationButtons();
        });
    }

    /**
     * This method is called by BrowserController to establish communication.
     */
    public void setToolbarController(ToolbarController controller) {
        this.toolbarController = controller;
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
            if (mainController != null) {
                String finalUrl = url;
                Platform.runLater(() -> mainController.statusLabel.setText("Loading: " + finalUrl));
            }
            LOGGER.info("Loading page: " + url);
        } catch (MalformedURLException e) {
            handleLoadException(e, url);
        }
    }

    @FXML
    public void loadPage() {
        if (toolbarController != null) {
            String url = toolbarController.getUrlFieldText().trim();
            loadPage(url);
            LOGGER.info("Load Page triggered from toolbar with URL: " + url);
        }
    }

    @FXML
    public void goBack() {
        if (history.getCurrentIndex() > 0) {
            history.go(-1);
            LOGGER.info("Navigated back in history.");
        }
    }

    @FXML
    public void goForward() {
        if (history.getCurrentIndex() < history.getEntries().size() - 1) {
            history.go(1);
            LOGGER.info("Navigated forward in history.");
        }
    }

    @FXML
    public void refreshPage() {
        webEngine.reload();
        LOGGER.info("Page refreshed.");
    }

    @FXML
    public void stopLoading() {
        webEngine.getLoadWorker().cancel();
        if (mainController != null) {
            Platform.runLater(() -> mainController.statusLabel.setText("Loading Stopped"));
        }
        LOGGER.info("Page loading stopped.");
    }

    /**
     * Updates the navigation buttons via ToolbarController.
     */
    private void updateNavigationButtons() {
        if (toolbarController != null) {
            boolean canGoBack = history.getCurrentIndex() > 0;
            boolean canGoForward = history.getCurrentIndex() < history.getEntries().size() - 1;
            toolbarController.updateNavigationButtons(canGoBack, canGoForward);
            LOGGER.info("Navigation buttons updated. Can Go Back: " + canGoBack + ", Can Go Forward: " + canGoForward);
        }
    }

    private void handleLoadException(Exception e, String url) {
        if (mainController != null) {
            Platform.runLater(() -> {
                mainController.statusLabel.setText("Error loading: " + url);
                mainController.showAlert("Load Error",
                        "Could not load the page:\n" + url + "\n\nError: " + e.getMessage(),
                        Alert.AlertType.ERROR);
            });
        }
        LOGGER.log(Level.SEVERE, "Error loading page: " + url, e);
    }

    private void handleDownload(String url) {
        // Simple check based on file extension
        if (url.matches(".*\\.(pdf|exe|zip|docx?)$")) {
            // Prompt user to download
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Download File");
                alert.setHeaderText("Do you want to download this file?");
                alert.setContentText(url);

                ButtonType yesButton = new ButtonType("Yes");
                ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(yesButton, noButton);

                alert.showAndWait().ifPresent(type -> {
                    if (type == yesButton) {
                        downloadFile(url);
                    }
                });
            });
            LOGGER.info("Download prompt shown for URL: " + url);
        }
    }

    private void downloadFile(String fileURL) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");
        String fileName = fileURL.substring(fileURL.lastIndexOf('/') + 1);
        fileChooser.setInitialFileName(fileName);
        File file = fileChooser.showSaveDialog(webView.getScene().getWindow());

        if (file != null) {
            new Thread(() -> {
                try (InputStream in = new URL(fileURL).openStream();
                     FileOutputStream out = new FileOutputStream(file)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                    // Notify user on completion
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Download Complete");
                        alert.setHeaderText(null);
                        alert.setContentText("File downloaded successfully:\n" + file.getAbsolutePath());
                        alert.showAndWait();
                    });
                    LOGGER.info("File downloaded successfully: " + file.getAbsolutePath());
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Download Failed");
                        alert.setHeaderText("Could not download the file.");
                        alert.setContentText(e.getMessage());
                        alert.showAndWait();
                    });
                    LOGGER.log(Level.SEVERE, "Error downloading file: " + fileURL, e);
                }
            }).start();
        } else {
            LOGGER.info("Download canceled by user for URL: " + fileURL);
        }
    }

    public String getCurrentUrl() {
        return webEngine.getLocation();
    }

    public String getPageTitle() {
        return webEngine.getTitle();
    }

    public void focusUrlField() {
        if (toolbarController != null) {
            toolbarController.focusUrlField();
            LOGGER.info("URL field focused.");
        }
    }

    public void setOnPageLoad(Runnable callback) {
        this.onPageLoadCallback = callback;
    }

    public Image getWebViewSnapshot() {
        try {
            return webView.snapshot(null, null);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error taking snapshot of WebView.", e);
            return null;
        }
    }

    public void dispose() {
        if (webEngine != null) {
            webEngine.load(null);
            webEngine.getLoadWorker().cancel();
            LOGGER.info("WebEngine disposed.");
        }
    }
}
