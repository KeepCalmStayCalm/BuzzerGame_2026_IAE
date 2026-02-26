module buzzer.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.pi4j;
    requires java.prefs;

    opens application to javafx.fxml;
    opens view to javafx.fxml;

    exports application;
}
