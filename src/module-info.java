module IAE_Buzzergame {
    // Java Standard Module
    requires java.prefs;
    requires java.desktop;

    // Externe Bibliotheken
    requires javafx.controls;
    requires javafx.fxml;
    requires com.pi4j;
    requires com.pi4j.plugin.raspberrypi;
    requires com.pi4j.plugin.pigpio;
    requires org.slf4j;

    // Erlaubt JavaFX den Zugriff auf deine Controller
    opens application to javafx.graphics, javafx.fxml;
    opens view to javafx.fxml;
    
    exports application;
}
