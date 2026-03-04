module buzzer.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;

    requires com.pi4j.core;
    requires com.pi4j.plugin.raspberrypi;
    requires com.pi4j.plugin.pigpio;
    requires com.pi4j.library.pigpio;

    requires java.net.http;
    requires org.slf4j;

    exports application;
    opens application to javafx.fxml;
    opens view to javafx.fxml;
}
