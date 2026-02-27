package application;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Buzzer für Maus/Tastatur-Steuerung im Entwicklungsmodus.
 * Die Antwort wird manuell per {@link #setAnswer(int)} gesetzt (z.B. durch
 * einen Button-Click im FXBuzzer-Fenster).
 */
public class MouseBuzzer implements IBuzzer {

	private final IntegerProperty answer = new SimpleIntegerProperty(0);

	public MouseBuzzer() { }

	@Override
	public IntegerProperty getAnswer() {
		return answer;
	}

	public void setAnswer(int answerIndex) {
		answer.setValue(answerIndex);
	}
}
