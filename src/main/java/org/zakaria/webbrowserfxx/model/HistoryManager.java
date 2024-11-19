// File: src/main/java/org/zakaria/webbrowserfxx/model/HistoryManager.java
package org.zakaria.webbrowserfxx.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HistoryManager {

    private static HistoryManager instance;
    private static final String HISTORY_FILE = "history.json";
    private List<String> history;
    private Gson gson;
    private static final Logger LOGGER = Logger.getLogger(HistoryManager.class.getName());

    private HistoryManager() {
        gson = new Gson();
        loadHistory();
    }

    public static synchronized HistoryManager getInstance() {
        if (instance == null) {
            instance = new HistoryManager();
        }
        return instance;
    }

    private void loadHistory() {
        try (Reader reader = new FileReader(HISTORY_FILE)) {
            Type listType = new TypeToken<ArrayList<String>>() {}.getType();
            history = gson.fromJson(reader, listType);
            if (history == null) {
                history = new ArrayList<>();
            }
            LOGGER.info("History loaded successfully.");
        } catch (FileNotFoundException e) {
            history = new ArrayList<>();
            LOGGER.log(Level.WARNING, "History file not found. Starting with an empty history.", e);
        } catch (IOException e) {
            history = new ArrayList<>();
            LOGGER.log(Level.SEVERE, "Error loading history.", e);
        }
    }

    public void addHistory(String url) {
        history.add(url);
        saveHistory();
        LOGGER.info("History added: " + url);
    }

    public List<String> getHistory() {
        return history;
    }

    public void clearHistory() {
        history.clear();
        saveHistory();
        LOGGER.info("History cleared.");
    }

    private void saveHistory() {
        try (Writer writer = new FileWriter(HISTORY_FILE)) {
            gson.toJson(history, writer);
            LOGGER.info("History saved successfully.");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error saving history.", e);
        }
    }
}
