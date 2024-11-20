module org.zakaria.webbrowserfxx {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.bootstrapfx.core;
    requires org.kordamp.ikonli.fontawesome;
    requires org.kordamp.ikonli.fontawesome5;
    requires javafx.web;
    requires com.google.gson;
    requires svg.salamander;
    requires javafx.swing;
    requires java.logging;

    opens org.zakaria.webbrowserfxx to javafx.fxml;
    opens org.zakaria.webbrowserfxx.model to com.google.gson;
    exports org.zakaria.webbrowserfxx;
    exports org.zakaria.webbrowserfxx.controller;
    opens org.zakaria.webbrowserfxx.controller to javafx.fxml;
}