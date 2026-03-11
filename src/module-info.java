module buzzer.app {

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;

    requires com.pi4j;
    requires com.pi4j.plugin.raspberrypi;
    requires com.pi4j.plugin.pigpio;

    requires org.slf4j;

    requires java.prefs;
    requires java.net.http;

    opens application to javafx.graphics, javafx.fxml;
    opens view      to javafx.graphics, javafx.fxml;

    exports application;
    exports view;
}
