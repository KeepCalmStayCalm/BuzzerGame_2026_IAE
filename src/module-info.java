module buzzer.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;     
    requires com.pi4j.core;
    requires java.net.http;
    requires org.slf4j;
    
    exports application;

    opens application to javafx.fxml;
    opens view to javafx.fxml;

}
