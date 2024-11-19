// File: src/main/java/org/zakaria/webbrowserfxx/controller/BookmarkListCellFactory.java
package org.zakaria.webbrowserfxx.controller;

import javafx.scene.control.ListCell;
import org.kordamp.ikonli.javafx.FontIcon;
import org.zakaria.webbrowserfxx.model.Bookmark;

public class BookmarkListCellFactory extends ListCell<Bookmark> {
    @Override
    protected void updateItem(Bookmark item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            setText(item.getTitle());
            FontIcon icon = new FontIcon("mdi-bookmark");
            setGraphic(icon);
        }
    }
}
