package view;

import application.IBuzzer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class FXBuzzerController implements IBuzzer {

    @FXML
    Label lblPunkte;

    private IntegerProperty answer = new SimpleIntegerProperty();

    // IBuzzer state properties – indicate which button is currently pressed
    // These are bound in EditSettingsViewController for hardware test display
    public BooleanProperty btnAState = new SimpleBooleanProperty(false);
    public BooleanProperty btnBState = new SimpleBooleanProperty(false);
    public BooleanProperty btnCState = new SimpleBooleanProperty(false);

    public void b1Pressed() {
        getAnswer().setValue(1);
        // Briefly light up the A state indicator
        btnAState.set(true);
        btnBState.set(false);
        btnCState.set(false);
        System.out.println("Button A (1) pressed");
    }

    public void b2Pressed() {
        getAnswer().setValue(2);
        btnAState.set(false);
        btnBState.set(true);
        btnCState.set(false);
        System.out.println("Button B (2) pressed");
    }

    public void b3Pressed() {
        getAnswer().setValue(3);
        btnAState.set(false);
        btnBState.set(false);
        btnCState.set(true);
        System.out.println("Button C (3) pressed");
    }

    /**
     * Update the displayed score on this buzzer window.
     */
    public void setPunkte(int punkte) {
        if (lblPunkte != null) {
            lblPunkte.setText(String.valueOf(punkte));
        }
    }

    @Override
    public IntegerProperty getAnswer() {
        if (answer == null) {
            answer = new SimpleIntegerProperty();
        }
        return answer;
    }
}
