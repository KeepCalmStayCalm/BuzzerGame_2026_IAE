module buzzer.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    
    // Pi4J 2.x uses automatic modules - use JAR names as module names
    requires com.pi4j;
    requires com.pi4j.plugin.raspberrypi;
    requires com.pi4j.plugin.pigpio;
    requires com.pi4j.library.pigpio;
    
    // Java standard modules
    requires java.net.http;
    requires java.prefs;
    
    // SLF4J logging
    requires org.slf4j;
    
    exports application;
    opens application to javafx.fxml;
    opens view to javafx.fxml;
}
