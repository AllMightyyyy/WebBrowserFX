// File: src/main/java/org/zakaria/webbrowserfxx/model/BookmarksManager.java
package org.zakaria.webbrowserfxx.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookmarksManager {

    private static BookmarksManager instance;
    private static final String BOOKMARKS_FILE = "bookmarks.json";
    private List<Bookmark> bookmarks;
    private Gson gson;
    private static final Logger LOGGER = Logger.getLogger(BookmarksManager.class.getName());

    private BookmarksManager() {
        gson = new Gson();
        loadBookmarks();
    }

    public static synchronized BookmarksManager getInstance() {
        if (instance == null) {
            instance = new BookmarksManager();
        }
        return instance;
    }

    private void loadBookmarks() {
        try (Reader reader = new FileReader(BOOKMARKS_FILE)) {
            Type listType = new TypeToken<ArrayList<Bookmark>>() {}.getType();
            bookmarks = gson.fromJson(reader, listType);
            if (bookmarks == null) {
                bookmarks = new ArrayList<>();
            }
            LOGGER.info("Bookmarks loaded successfully.");
        } catch (FileNotFoundException e) {
            bookmarks = new ArrayList<>();
            LOGGER.log(Level.WARNING, "Bookmarks file not found. Starting with an empty list.", e);
        } catch (IOException e) {
            bookmarks = new ArrayList<>();
            LOGGER.log(Level.SEVERE, "Error loading bookmarks.", e);
        }
    }

    public void addBookmark(Bookmark bookmark) {
        if (!bookmarks.contains(bookmark)) {
            bookmarks.add(bookmark);
            saveBookmarks();
            LOGGER.info("Bookmark added: " + bookmark.getTitle() + " - " + bookmark.getUrl());
        } else {
            LOGGER.info("Bookmark already exists: " + bookmark.getTitle() + " - " + bookmark.getUrl());
        }
    }

    public void removeBookmark(String url) {
        bookmarks.removeIf(bookmark -> bookmark.getUrl().equals(url));
        saveBookmarks();
        LOGGER.info("Bookmark removed for URL: " + url);
    }

    public List<Bookmark> getBookmarks() {
        return bookmarks;
    }

    private void saveBookmarks() {
        try (Writer writer = new FileWriter(BOOKMARKS_FILE)) {
            gson.toJson(bookmarks, writer);
            LOGGER.info("Bookmarks saved successfully.");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error saving bookmarks.", e);
        }
    }
}
