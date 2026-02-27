package application;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Buzzer für Maus/Tastatur-Steuerung im Entwicklungsmodus.
 */
public class MouseBuzzer implements IBuzzer {

    private final IntegerProperty answer   = new SimpleIntegerProperty(0);
    private final BooleanProperty btnAState = new SimpleBooleanProperty(false);
    private final BooleanProperty btnBState = new SimpleBooleanProperty(false);
    private final BooleanProperty btnCState = new SimpleBooleanProperty(false);

    public MouseBuzzer() { }

    public void setAnswer(int answerIndex) {
        answer.setValue(answerIndex);
        btnAState.set(answerIndex == 1);
        btnBState.set(answerIndex == 2);
        btnCState.set(answerIndex == 3);
    }

    @Override public IntegerProperty getAnswer()          { return answer; }
    @Override public BooleanProperty btnAStateProperty()  { return btnAState; }
    @Override public BooleanProperty btnBStateProperty()  { return btnBState; }
    @Override public BooleanProperty btnCStateProperty()  { return btnCState; }
}
