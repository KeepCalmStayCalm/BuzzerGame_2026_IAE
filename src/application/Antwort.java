package application;

import java.io.Serializable;

/**
 * Erfasst eine Antwort
 * @author jacmo
 *
 */
public class Antwort implements Serializable{

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
