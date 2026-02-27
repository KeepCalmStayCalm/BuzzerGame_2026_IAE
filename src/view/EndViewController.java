package view;

import application.IBuzzer;
import javafx.animation.PauseTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class FXBuzzerController implements IBuzzer {

    @FXML
    Label lblPunkte;

    private IntegerProperty answer = new SimpleIntegerProperty();

    // IBuzzer state properties – indicate which button is currently pressed
    // These are bound in EditSettingsViewController for hardware test display
    public BooleanProperty btnAState = new SimpleBooleanProperty(false);
    public BooleanProperty btnBState = new SimpleBooleanProperty(false);
    public BooleanProperty btnCState = new SimpleBooleanProperty(false);

    /**
     * Button A pressed - answer 1
     */
    @FXML
    public void b1Pressed() {
        getAnswer().setValue(1);
        
        // Briefly light up the A state indicator
        btnAState.set(true);
        btnBState.set(false);
        btnCState.set(false);
        
        System.out.println("Button A (1) pressed");
        
        // Reset button state after short delay (for visual feedback)
        resetButtonStateAfterDelay();
    }

    /**
     * Button B pressed - answer 2
     */
    @FXML
    public void b2Pressed() {
        getAnswer().setValue(2);
        
        btnAState.set(false);
        btnBState.set(true);
        btnCState.set(false);
        
        System.out.println("Button B (2) pressed");
        
        resetButtonStateAfterDelay();
    }

    /**
     * Button C pressed - answer 3
     */
    @FXML
    public void b3Pressed() {
        getAnswer().setValue(3);
        
        btnAState.set(false);
        btnBState.set(false);
        btnCState.set(true);
        
        System.out.println("Button C (3) pressed");
        
        resetButtonStateAfterDelay();
    }

    /**
     * Reset button states after a brief delay for visual feedback
     */
    private void resetButtonStateAfterDelay() {
        PauseTransition pause = new PauseTransition(Duration.millis(500));
        pause.setOnFinished(event -> {
            btnAState.set(false);
            btnBState.set(false);
            btnCState.set(false);
        });
        pause.play();
    }

    /**
     * Update the displayed score on this buzzer window.
     */
    public void setPunkte(int punkte) {
        if (lblPunkte != null) {
            lblPunkte.setText("Punkte: " + punkte);
        }
    }
    
    /**
     * Reset the score display
     */
    public void resetPunkte() {
        if (lblPunkte != null) {
            lblPunkte.setText("Punkte: –");
        }
    }

    @Override
    public IntegerProperty getAnswer() {
        if (answer == null) {
            answer = new SimpleIntegerProperty(0);
        }
        return answer;
    }
    
    /**
     * Reset answer property
     */
    public void resetAnswer() {
        if (answer != null) {
            answer.setValue(0);
        }
    }
}
