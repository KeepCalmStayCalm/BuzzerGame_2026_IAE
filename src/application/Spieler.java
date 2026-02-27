package application;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Repräsentiert einen Spieler mit Buzzer, Punktestand und aktueller Antwort.
 */
public class Spieler {

	// BUG FIX: fields were package-private; made private.
	private String name;
	private IntegerProperty punktestand;
	private int rundenpunkte;
	private IntegerProperty antwortNr;
	private IBuzzer buzzer;

	public Spieler(String name, IBuzzer buzzer) {
		this.name = name;
		this.punktestand = new SimpleIntegerProperty(0);
		this.antwortNr   = new SimpleIntegerProperty(0);
		this.buzzer = buzzer;
		if (buzzer != null) {
			this.antwortNr.bind(buzzer.getAnswer());
		}
	}

	// ── equals / hashCode ────────────────────────────────────────

	/**
	 * BUG FIX: original logic was inverted — returned false when classes matched,
	 * then crashed with ClassCastException on the cast. Fixed to standard contract.
	 * Added hashCode so Spieler works correctly inside HashSet.
	 */
	@Override
	public boolean equals(Object other) {
		if (other == this) return true;
		if (other == null || !other.getClass().equals(this.getClass())) return false;
		return ((Spieler) other).getName().equals(this.getName());
	}

	@Override
	public int hashCode() {
		return name == null ? 0 : name.hashCode();
	}

	// ── Score helpers ────────────────────────────────────────────

	public void addPunkte(int punkte) {
		punktestand.setValue(punktestand.getValue() + punkte);
	}

	/** Resets the buzzer binding and clears the answer for the next round. */
	public void reset() {
		antwortNr.unbind();
		antwortNr.setValue(0);
		buzzer.getAnswer().setValue(0);
		antwortNr.bind(buzzer.getAnswer());
	}

	/** Re-bind the answer property (call after replacing the buzzer). */
	public void aboErneuern() {
		antwortNr.bind(buzzer.getAnswer());
	}

	// ── Getters / Setters ────────────────────────────────────────

	public String getName()                          { return name; }
	public void   setName(String name)               { this.name = name; }

	public IntegerProperty getPunktestand()          { return punktestand; }

	public IntegerProperty getAntwortNr() {
		if (antwortNr == null) {
			antwortNr = new SimpleIntegerProperty(0);
			if (buzzer != null) antwortNr.bind(buzzer.getAnswer());
		}
		return antwortNr;
	}

	public IBuzzer getBuzzer()                       { return buzzer; }
	public void    setBuzzer(IBuzzer buzzer)         { this.buzzer = buzzer; }

	public int  getRundenpunkte()                    { return rundenpunkte; }
	public void setRundenpunkte(int p)               { this.rundenpunkte = p; }
}
