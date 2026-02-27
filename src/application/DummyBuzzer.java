package application;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Buzzer-Stub für Tests / Demo-Betrieb ohne Hardware.
 */
public class DummyBuzzer implements IBuzzer {

    private final IntegerProperty answer;
    private final BooleanProperty btnAState = new SimpleBooleanProperty(false);
    private final BooleanProperty btnBState = new SimpleBooleanProperty(false);
    private final BooleanProperty btnCState = new SimpleBooleanProperty(false);

    public DummyBuzzer(int answerIndex) {
        this.answer = new SimpleIntegerProperty(answerIndex);
        btnAState.set(answerIndex == 1);
        btnBState.set(answerIndex == 2);
        btnCState.set(answerIndex == 3);
    }

    @Override public IntegerProperty getAnswer()          { return answer; }
    @Override public BooleanProperty btnAStateProperty()  { return btnAState; }
    @Override public BooleanProperty btnBStateProperty()  { return btnBState; }
    @Override public BooleanProperty btnCStateProperty()  { return btnCState; }
}
