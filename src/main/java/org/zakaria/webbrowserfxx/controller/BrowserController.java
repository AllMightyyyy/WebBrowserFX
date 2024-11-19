// File: src/main/java/org/zakaria/webbrowserfxx/controller/BrowserController.java
package org.zakaria.webbrowserfxx.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.zakaria.webbrowserfxx.MainApp;
import org.zakaria.webbrowserfxx.model.Bookmark;
import org.zakaria.webbrowserfxx.model.BookmarksManager;
import org.zakaria.webbrowserfxx.model.HistoryManager;
import org.zakaria.webbrowserfxx.model.SettingsManager;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BrowserController {

    @FXML
    private TabPane tabPane;

    @FXML
    ProgressBar progressBar;

    @FXML
    Label statusLabel;

    @FXML
    private ListView<Bookmark> bookmarksListView;

    @FXML
    private ListView<String> historyListView;

    @FXML
    private MenuItem newTabMenuItem;
    @FXML
    private MenuItem closeTabMenuItem;
    @FXML
    private MenuItem exitMenuItem;
    @FXML
    private MenuItem addBookmarkMenuItem;
    @FXML
    private MenuItem manageBookmarksMenuItem;
    @FXML
    private MenuItem showHistoryMenuItem;
    @FXML
    private MenuItem clearHistoryMenuItem;
    @FXML
    private MenuItem preferencesMenuItem;
    @FXML
    private MenuItem aboutMenuItem;

    private BookmarksManager bookmarksManager;
    private HistoryManager historyManager;
    private SettingsManager settingsManager;

    private static final Logger LOGGER = Logger.getLogger(BrowserController.class.getName());

    @FXML
    public void initialize() {
        // Set SVG icons for menu items
        newTabMenuItem.setGraphic(loadSVGIcon("plus-circle.svg"));
        closeTabMenuItem.setGraphic(loadSVGIcon("times-circle.svg"));
        exitMenuItem.setGraphic(loadSVGIcon("sign-out.svg"));
        addBookmarkMenuItem.setGraphic(loadSVGIcon("bookmark.svg"));
        manageBookmarksMenuItem.setGraphic(loadSVGIcon("book.svg"));
        showHistoryMenuItem.setGraphic(loadSVGIcon("history.svg"));
        clearHistoryMenuItem.setGraphic(loadSVGIcon("trash.svg"));
        preferencesMenuItem.setGraphic(loadSVGIcon("cog.svg"));
        aboutMenuItem.setGraphic(loadSVGIcon("info-circle.svg"));

        // Initialize managers
        bookmarksManager = BookmarksManager.getInstance();
        historyManager = HistoryManager.getInstance();
        settingsManager = SettingsManager.getInstance();

        // Populate sidebar lists
        updateSidebar();

        // Create the first tab with the home page
        createNewTab(settingsManager.getHomePage());

        // Set TabPane drag policy for reordering
        tabPane.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);

        // Setup keyboard shortcuts
        setupKeyboardShortcuts();
    }

    @FXML
    public void createNewTab() {
        createNewTab(settingsManager.getHomePage());
    }

    private void createNewTab(String url) {
        try {
            // Load the browser_tab.fxml
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/org/zakaria/webbrowserfxx/browser_tab.fxml"));
            BorderPane content = loader.load();

            // Get the controller of the new tab
            BrowserTabController tabController = loader.getController();

            // Create a new tab and set its content
            Tab tab = new Tab("New Tab");
            tab.setContent(content);

            // Add close button with icon to the tab
            Button closeButton = new Button();
            closeButton.setGraphic(loadSVGIcon("close-circle.svg"));
            closeButton.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
            closeButton.setOnAction(e -> {
                tabPane.getTabs().remove(tab);
                LOGGER.info("Tab closed via close button.");
            });

            // Create a HBox to hold the tab title and close button
            HBox tabHeader = new HBox(5);
            Label tabLabel = new Label("New Tab");
            tabLabel.setStyle("-fx-font-size: 14px;");
            tabHeader.getChildren().addAll(tabLabel, closeButton);

            tab.setGraphic(tabHeader);

            // Associate the tab with its controller
            tabController.setTab(tab);
            tabController.setMainController(this);

            // Retrieve the ToolbarController and set the BrowserTabController
            HBox toolbarHBox = (HBox) content.getTop();
            Object userData = toolbarHBox.getUserData();
            if (userData instanceof ToolbarController) {
                ToolbarController toolbarController = (ToolbarController) userData;
                toolbarController.setTabController(tabController);
                tabController.setToolbarController(toolbarController);
                LOGGER.info("ToolbarController linked with BrowserTabController.");
            } else {
                LOGGER.warning("ToolbarController not found in toolbar HBox's userData.");
            }

            // Add the new tab to the TabPane
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);

            // Load the specified URL
            tabController.loadPage(url);

            // Set up hover event for tab preview
            tabHeader.setOnMouseEntered(event -> showTabPreview(tab));
            tabHeader.setOnMouseExited(event -> hideTabPreview());

            // Clean up resources when tab is closed
            tab.setOnClosed(event -> tabController.dispose());

            // Update sidebar when a new page is loaded
            tabController.setOnPageLoad(() -> updateSidebar());

            LOGGER.info("New tab created with URL: " + url);

        } catch (IOException e) {
            showAlert("Error", "Failed to create new tab: " + e.getMessage(), Alert.AlertType.ERROR);
            LOGGER.log(Level.SEVERE, "Failed to create new tab.", e);
        }
    }

    @FXML
    public void closeCurrentTab() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            tabPane.getTabs().remove(selectedTab);
            LOGGER.info("Current tab closed via menu.");
        }
        if (tabPane.getTabs().isEmpty()) {
            createNewTab();
        }
    }

    @FXML
    public void exitApplication() {
        Platform.exit();
        LOGGER.info("Application exited by user.");
    }

    @FXML
    public void addBookmark() {
        BrowserTabController controller = getCurrentTabController();
        if (controller != null) {
            String url = controller.getCurrentUrl();
            String title = controller.getPageTitle();
            if (url != null && !url.isEmpty()) {
                bookmarksManager.addBookmark(new Bookmark(title, url));
                updateSidebar();
                showAlert("Bookmark Added", "Added to bookmarks:\n" + title + "\n" + url, Alert.AlertType.INFORMATION);
                LOGGER.info("Bookmark added: " + title + " - " + url);
            } else {
                showAlert("Error", "No URL available to bookmark.", Alert.AlertType.ERROR);
                LOGGER.warning("Attempted to add a bookmark with no URL.");
            }
        } else {
            showAlert("Error", "No active tab found.", Alert.AlertType.ERROR);
            LOGGER.warning("Attempted to add a bookmark with no active tab.");
        }
    }

    @FXML
    public void manageBookmarks() {
        // Display bookmarks in a dialog
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Manage Bookmarks");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        ListView<Bookmark> listView = new ListView<>();
        listView.getItems().addAll(bookmarksManager.getBookmarks());

        listView.setCellFactory(param -> new BookmarkListCellFactory());

        // Allow double-click to open bookmark
        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Bookmark selectedBookmark = listView.getSelectionModel().getSelectedItem();
                if (selectedBookmark != null) {
                    createNewTab(selectedBookmark.getUrl());
                    dialog.close();
                    LOGGER.info("Bookmark opened: " + selectedBookmark.getUrl());
                }
            }
        });

        dialog.getDialogPane().setContent(listView);
        dialog.showAndWait();
        LOGGER.info("Manage Bookmarks dialog closed.");
    }

    @FXML
    public void showHistory() {
        // Display history in a dialog
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Browsing History");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        ListView<String> listView = new ListView<>();
        listView.getItems().addAll(historyManager.getHistory());

        // Allow double-click to open history item
        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedUrl = listView.getSelectionModel().getSelectedItem();
                if (selectedUrl != null) {
                    createNewTab(selectedUrl);
                    dialog.close();
                    LOGGER.info("History item opened: " + selectedUrl);
                }
            }
        });

        dialog.getDialogPane().setContent(listView);
        dialog.showAndWait();
        LOGGER.info("Show History dialog closed.");
    }

    @FXML
    public void clearHistory() {
        historyManager.clearHistory();
        updateSidebar();
        showAlert("History Cleared", "Your browsing history has been cleared.", Alert.AlertType.INFORMATION);
        LOGGER.info("Browsing history cleared by user.");
    }

    @FXML
    public void showPreferences() {
        // Show preferences dialog
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Preferences");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Create form fields
        TextField homePageField = new TextField(settingsManager.getHomePage());
        homePageField.setPromptText("Home Page URL");

        CheckBox darkModeCheckBox = new CheckBox("Enable Dark Mode");
        darkModeCheckBox.setSelected(settingsManager.isDarkMode());

        VBox content = new VBox(10, new Label("Home Page:"), homePageField, darkModeCheckBox);
        content.setPadding(new javafx.geometry.Insets(10));

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String newHomePage = homePageField.getText().trim();
                boolean darkMode = darkModeCheckBox.isSelected();

                if (!newHomePage.isEmpty()) {
                    settingsManager.setHomePage(newHomePage);
                    LOGGER.info("Home page updated to: " + newHomePage);
                }
                settingsManager.setDarkMode(darkMode);
                settingsManager.saveSettings();

                applyDarkMode(darkMode);

                showAlert("Preferences Saved", "Settings have been updated.", Alert.AlertType.INFORMATION);
                LOGGER.info("Preferences updated: Dark Mode = " + darkMode);
            }
            return null;
        });

        dialog.showAndWait();
        LOGGER.info("Preferences dialog closed.");
    }

    @FXML
    public void showAboutDialog() {
        showAlert("About", "Enhanced JavaFX Web Browser\nVersion 2.0\nDeveloped by Zakaria", Alert.AlertType.INFORMATION);
        LOGGER.info("About dialog shown.");
    }

    private BrowserTabController getCurrentTabController() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            BorderPane content = (BorderPane) selectedTab.getContent();
            return (BrowserTabController) content.getUserData();
        }
        return null;
    }

    private void updateSidebar() {
        Platform.runLater(() -> {
            bookmarksListView.getItems().setAll(bookmarksManager.getBookmarks());
            historyListView.getItems().setAll(historyManager.getHistory());
            LOGGER.info("Sidebar updated with bookmarks and history.");
        });
    }

    private void setupKeyboardShortcuts() {
        // Wait until the scene is available
        tabPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                addKeyboardShortcuts(newScene);
            }
        });
    }

    private void addKeyboardShortcuts(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown()) {
                switch (event.getCode()) {
                    case T:
                        createNewTab();
                        event.consume();
                        break;
                    case W:
                        closeCurrentTab();
                        event.consume();
                        break;
                    case L:
                        BrowserTabController currentTabController = getCurrentTabController();
                        if (currentTabController != null) {
                            currentTabController.focusUrlField();
                            LOGGER.info("URL field focused via keyboard shortcut.");
                        }
                        event.consume();
                        break;
                    case R:
                        BrowserTabController currentTabControllerForRefresh = getCurrentTabController();
                        if (currentTabControllerForRefresh != null) {
                            currentTabControllerForRefresh.refreshPage();
                            LOGGER.info("Page refreshed via keyboard shortcut.");
                        }
                        event.consume();
                        break;
                    default:
                        break;
                }
            }
        });
        LOGGER.info("Keyboard shortcuts initialized.");
    }

    private void applyDarkMode(boolean darkMode) {
        Platform.runLater(() -> {
            Scene scene = tabPane.getScene();
            if (scene != null) {
                if (darkMode) {
                    if (!scene.getRoot().getStyleClass().contains("dark")) {
                        scene.getRoot().getStyleClass().add("dark");
                        LOGGER.info("Dark mode applied.");
                    }
                } else {
                    scene.getRoot().getStyleClass().remove("dark");
                    LOGGER.info("Dark mode disabled.");
                }
            }
        });
    }

    private ImageView previewImageView;

    private void showTabPreview(Tab tab) {
        if (previewImageView != null) return; // Already showing

        BrowserTabController controller = (BrowserTabController) ((BorderPane) tab.getContent()).getUserData();
        if (controller != null) {
            Image snapshot = controller.getWebViewSnapshot();
            if (snapshot != null) {
                Platform.runLater(() -> {
                    previewImageView = new ImageView(snapshot);
                    previewImageView.setFitWidth(300);
                    previewImageView.setFitHeight(200);
                    previewImageView.setPreserveRatio(true);
                    BorderPane root = (BorderPane) tabPane.getScene().getRoot();
                    root.setRight(previewImageView);
                    LOGGER.info("Tab preview shown.");
                });
            }
        }
    }

    private void hideTabPreview() {
        if (previewImageView != null) {
            Platform.runLater(() -> {
                BorderPane root = (BorderPane) tabPane.getScene().getRoot();
                root.setRight(null);
                previewImageView = null;
                LOGGER.info("Tab preview hidden.");
            });
        }
    }

    void showAlert(String title, String message, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private Node loadSVGIcon(String path) {
        try {
            // Load the SVG using an absolute path
            URL svgUrl = MainApp.class.getResource("/org/zakaria/webbrowserfxx/icons/" + path);
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
