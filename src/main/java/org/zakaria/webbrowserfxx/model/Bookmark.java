// File: src/main/java/org/zakaria/webbrowserfxx/model/Bookmark.java
package org.zakaria.webbrowserfxx.model;

import java.util.Objects;

public class Bookmark {
    private String title;
    private String url;

    public Bookmark() {
        // Default constructor for Gson
    }

    public Bookmark(String title, String url) {
        this.title = title;
        this.url = url;
    }

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    // Override equals and hashCode for proper comparison in lists
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Bookmark bookmark = (Bookmark) o;

        return Objects.equals(url, bookmark.url);
    }

    @Override
    public int hashCode() {
        return url != null ? url.hashCode() : 0;
    }
}
