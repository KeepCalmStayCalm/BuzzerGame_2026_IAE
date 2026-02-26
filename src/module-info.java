module IAE_Buzzergame {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.pi4j;
    requires com.pi4j.plugin.raspberrypi;
    requires com.pi4j.plugin.pigpio;
    requires org.slf4j;

    opens application to javafx.graphics, javafx.fxml;
    exports application;
}
