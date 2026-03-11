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
    
    // JSON parsing - REQUIRED FOR API NICKNAME FETCHING
    requires com.google.gson;
    
    // Open packages for JavaFX reflection
    opens view to javafx.fxml;
    opens application to javafx.fxml;
    
    // Export packages
    exports application;
    exports view;
}
