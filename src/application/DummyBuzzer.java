package application;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Buzzer-Stub für Unit-Tests und Demo-Betrieb ohne Hardware.
 * Gibt beim Start einen festen Antwort-Index zurück.
 */
public class DummyBuzzer implements IBuzzer {

	private final IntegerProperty answer;

	public DummyBuzzer(int answerIndex) {
		this.answer = new SimpleIntegerProperty(answerIndex);
	}

	@Override
	public IntegerProperty getAnswer() {
		return answer;
	}
}
