package application;

import java.io.Serializable;

/**
 * Erfasst eine Antwort.
 */
public class Antwort implements Serializable {

	private static final long serialVersionUID = 1L;

	private String antwort;
	private boolean isCorrect;

	public Antwort(String antwort, boolean isCorrect) {
		this.antwort = antwort;
		this.isCorrect = isCorrect;
	}

	public boolean isCorrect() {
		return isCorrect;
	}

	public void setCorrect(boolean isCorrect) {
		this.isCorrect = isCorrect;
	}

	public String getAntwort() {
		return antwort;
	}
}
