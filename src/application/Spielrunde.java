package application;

import java.util.List;

/**
 * Verwaltet die Fragenfolge einer Spielrunde.
 */
public class Spielrunde {

	// BUG FIX: field was package-private; made private.
	private final List<Frage> tenQuestions;
	private int fragerunden;

	public Spielrunde(List<Frage> questions) {
		this.tenQuestions = questions;
		this.fragerunden  = 0;
	}

	/**
	 * Gibt die nächste Frage zurück und erhöht den Zähler.
	 *
	 * BUG FIX: no bounds check was present; calling this past the end caused an
	 * uncaught IndexOutOfBoundsException in the game flow. Added a clear
	 * IllegalStateException so the error is obvious if the caller is out of sync.
	 *
	 * Also fixed the redundant second {@code tenQuestions.get(fragerunden-1)}
	 * log call — now uses the local variable instead.
	 */
	public Frage naechsteFrage() {
		if (fragerunden >= tenQuestions.size()) {
			throw new IllegalStateException(
				"Keine weiteren Fragen. Gestellt: " + fragerunden +
				", Verfügbar: " + tenQuestions.size());
		}
		Frage aktuelleFrage = tenQuestions.get(fragerunden);
		fragerunden++;
		System.out.println("Fragerunde " + fragerunden + "/" + tenQuestions.size() +
		                   ": " + aktuelleFrage.getFrage());
		return aktuelleFrage;
	}

	public boolean hatWeitereFragen() {
		return fragerunden < tenQuestions.size();
	}

	public int getAnzahlFragen() {
		return tenQuestions.size();
	}
}
