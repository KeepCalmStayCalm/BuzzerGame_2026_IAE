module buzzer.app {
    // JavaFX modules
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    // Pi4J modules
    requires com.pi4j;
    requires com.pi4j.plugin.raspberrypi;
    requires com.pi4j.plugin.pigpio;

    // Logging
    requires org.slf4j;

    // JSON parsing (for nickname API)
    requires com.google.gson;

    // === NEW LINES THAT FIX YOUR ERROR ===
    requires java.net.http;
    requires java.prefs;

    // Open packages for JavaFX FXML
    opens view to javafx.fxml;
    opens application to javafx.fxml;

    // Export packages
    exports application;
    exports view;
}
