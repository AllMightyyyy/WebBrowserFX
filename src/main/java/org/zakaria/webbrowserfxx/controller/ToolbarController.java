// File: src/main/java/org/zakaria/webbrowserfxx/controller/ToolbarController.java
package org.zakaria.webbrowserfxx.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.Node;
import org.zakaria.webbrowserfxx.controller.BrowserTabController;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ToolbarController {

    @FXML
    private Button backButton;

    @FXML
    private Button forwardButton;

    @FXML
    private TextField urlField;

    @FXML
    private Button refreshPageButton;

    @FXML
    private Button stopLoadingButton;

    @FXML
    private Button goButton;

    private BrowserTabController tabController;

    private static final Logger LOGGER = Logger.getLogger(ToolbarController.class.getName());

    /**
     * This method is called by BrowserController to establish communication.
     */
    public void setTabController(BrowserTabController controller) {
        this.tabController = controller;
    }

    @FXML
    public void initialize() {
        // Register this controller in the parent HBox's userData
        Node parent = backButton.getParent();
        if (parent instanceof javafx.scene.layout.HBox) {
            ((javafx.scene.layout.HBox) parent).setUserData(this);
        }

        // Set SVG icons for the buttons
        backButton.setGraphic(loadSVGIcon("arrow-left-bold.svg"));
        forwardButton.setGraphic(loadSVGIcon("arrow-right-bold.svg"));
        refreshPageButton.setGraphic(loadSVGIcon("refresh.svg"));
        stopLoadingButton.setGraphic(loadSVGIcon("stop.svg"));
        goButton.setGraphic(loadSVGIcon("send.svg"));
    }

    @FXML
    public void goBack() {
        if (tabController != null) {
            tabController.goBack();
            LOGGER.info("Go Back action triggered.");
        }
    }

    @FXML
    public void goForward() {
        if (tabController != null) {
            tabController.goForward();
            LOGGER.info("Go Forward action triggered.");
        }
    }

    @FXML
    public void refreshPage() {
        if (tabController != null) {
            tabController.refreshPage();
            LOGGER.info("Refresh Page action triggered.");
        }
    }

    @FXML
    public void stopLoading() {
        if (tabController != null) {
            tabController.stopLoading();
            LOGGER.info("Stop Loading action triggered.");
        }
    }

    @FXML
    public void loadPage() {
        if (tabController != null) {
            tabController.loadPage();
            LOGGER.info("Load Page action triggered.");
        }
    }

    /**
     * Updates the URL field in the toolbar.
     */
    public void updateUrlField(String url) {
        Platform.runLater(() -> urlField.setText(url));
    }

    /**
     * Updates the navigation buttons' disable state.
     */
    public void updateNavigationButtons(boolean canGoBack, boolean canGoForward) {
        Platform.runLater(() -> {
            backButton.setDisable(!canGoBack);
            forwardButton.setDisable(!canGoForward);
        });
    }

    /**
     * Focuses the URL field.
     */
    public void focusUrlField() {
        Platform.runLater(() -> urlField.requestFocus());
    }

    /**
     * Retrieves the text from the URL field.
     */
    public String getUrlFieldText() {
        return urlField.getText();
    }

    private Node loadSVGIcon(String path) {
        try {
            // Load the SVG using an absolute path
            URL svgUrl = getClass().getResource("/org/zakaria/webbrowserfxx/icons/" + path);
            if (svgUrl == null) {
                throw new IOException("SVG file not found: " + path);
            }

            // Use a WebView to render the SVG
            javafx.scene.web.WebView webView = new javafx.scene.web.WebView();
            webView.getEngine().load(svgUrl.toExternalForm());
            webView.setPrefSize(24, 24); // Set preferred size for the icon
            webView.setZoom(2.0); // Adjust zoom to scale appropriately
            webView.setMaxSize(24, 24); // Ensure WebView doesn't expand

            return webView;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading SVG icon: " + path, e);
            return null;
        }
    }
}
