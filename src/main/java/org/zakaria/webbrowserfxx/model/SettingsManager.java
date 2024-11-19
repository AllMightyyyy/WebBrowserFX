// File: src/main/java/org/zakaria/webbrowserfxx/model/SettingsManager.java
package org.zakaria.webbrowserfxx.model;

import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SettingsManager {

    private static SettingsManager instance;
    private static final String SETTINGS_FILE = "settings.json";
    private Settings settings;
    private Gson gson;
    private static final Logger LOGGER = Logger.getLogger(SettingsManager.class.getName());

    private SettingsManager() {
        gson = new Gson();
        loadSettings();
    }

    public static synchronized SettingsManager getInstance() {
        if (instance == null) {
            instance = new SettingsManager();
        }
        return instance;
    }

    private void loadSettings() {
        try (FileReader reader = new FileReader(SETTINGS_FILE)) {
            settings = gson.fromJson(reader, Settings.class);
            if (settings == null) {
                settings = new Settings();
            }
            LOGGER.info("Settings loaded successfully.");
        } catch (FileNotFoundException e) {
            settings = new Settings();
            LOGGER.log(Level.WARNING, "Settings file not found. Using default settings.", e);
        } catch (IOException e) {
            settings = new Settings();
            LOGGER.log(Level.SEVERE, "Error loading settings.", e);
        }
    }

    public void saveSettings() {
        try (FileWriter writer = new FileWriter(SETTINGS_FILE)) {
            gson.toJson(settings, writer);
            LOGGER.info("Settings saved successfully.");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error saving settings.", e);
        }
    }

    // Getters and setters
    public String getHomePage() {
        return settings.homePage;
    }

    public void setHomePage(String homePage) {
        settings.homePage = homePage;
    }

    public boolean isDarkMode() {
        return settings.darkMode;
    }

    public void setDarkMode(boolean darkMode) {
        settings.darkMode = darkMode;
    }

    // Additional settings can be added here

    private static class Settings {
        String homePage = "https://www.google.com";
        boolean darkMode = false;
        // Add more settings fields as needed
    }
}