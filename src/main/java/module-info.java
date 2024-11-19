module org.zakaria.webbrowserfxx {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires javafx.web;

    opens org.zakaria.webbrowserfxx to javafx.fxml;
    exports org.zakaria.webbrowserfxx;
    exports org.zakaria.webbrowserfxx.controller;
    opens org.zakaria.webbrowserfxx.controller to javafx.fxml;
}