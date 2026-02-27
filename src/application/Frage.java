package application;

import java.io.Serializable;
import java.util.List;

/**
 * Verwaltet eine Frage und die dazugehörigen Antworten.
 */
public class Frage implements Serializable {

	private static final long serialVersionUID = 1L;

	private String frage;
	private List<Antwort> antworten;
	private String imagePath;

	public Frage(String frage, List<Antwort> antworten) {
		this.frage = frage;
		this.antworten = antworten;
	}

	/** @return true wenn die gegebene Antwort korrekt ist. */
	public boolean pruefeAntwort(Antwort a) {
		return a.isCorrect();
	}

	/** @return den Text der richtigen Antwort, oder {@code null} falls keine gesetzt. */
	public String korrekteAntwort() {
		for (Antwort a : antworten) {
			if (a.isCorrect()) return a.getAntwort();
		}
		return null;
	}

	/** @return die 1-basierte Indexnummer der richtigen Antwort (0 = nicht gefunden). */
	public int korrekteAntwortInt() {
		for (int i = 0; i < antworten.size(); i++) {
			if (antworten.get(i).isCorrect()) return i + 1;
		}
		return 0;
	}

	// ── Getters / Setters ────────────────────────────────────────

	public String getFrage()                       { return frage; }
	public void   setFrage(String frage)           { this.frage = frage; }

	public List<Antwort> getAntworten()            { return antworten; }
	public void setAntworten(List<Antwort> a)      { this.antworten = a; }

	public String getImagePath()                   { return imagePath; }
	public void   setImagePath(String imagePath)   { this.imagePath = imagePath; }
}
