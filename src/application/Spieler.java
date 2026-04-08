package application;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class Spieler {

    private final int teilnehmerId;           // ← NEW: stores the real database ID (PK)
    String name;
    IntegerProperty punktestand;
    int rundenpunkte;
    IntegerProperty antwortNr;
    private IBuzzer buzzer;

    @Override
    public boolean equals(Object other) {
        if (other.getClass().equals(this.getClass()))
            return false;
        return ((Spieler) other).getName().equals(this.getName());
    }

    // UPDATED constructor – now requires the numeric ID first
    public Spieler(int teilnehmerId, String name, IBuzzer buzzer) {
        super();
        this.teilnehmerId = teilnehmerId;     // ← NEW
        this.name = name;
        this.punktestand = new SimpleIntegerProperty();
        this.antwortNr = new SimpleIntegerProperty(0);
        this.setBuzzer(buzzer);
        if (buzzer != null) {
            this.antwortNr.bind(getBuzzer().getAnswer());
        }
    }

    /*public void buzzerGedrueckt() {
        antwortNr.set(getBuzzer().getBuzzerNr());
        System.out.println(name + " hat BuzzerNr " + antwortNr + " gedrückt");
    }*/

    public void addPunkte(int punkte) {
        punktestand.setValue(punktestand.getValue() + punkte);
    }

    public void reset() {
        antwortNr.unbind();
        antwortNr.setValue(0);
        buzzer.getAnswer().setValue(0);
        antwortNr.bind(getBuzzer().getAnswer());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public IntegerProperty getPunktestand() {
        return punktestand;
    }

    public IntegerProperty getAntwortNr() {
        if (antwortNr == null) {
            antwortNr = new SimpleIntegerProperty();
            antwortNr.bind(getBuzzer().getAnswer());
        }
        return antwortNr;
    }

    public void aboErneuern() {
        antwortNr.bind(getBuzzer().getAnswer());
    }

    public IBuzzer getBuzzer() {
        return buzzer;
    }

    public void setBuzzer(IBuzzer buzzer2) {
        this.buzzer = buzzer2;
    }

    public int getRundenpunkte() {
        return rundenpunkte;
    }

    public void setRundenpunkte(int rundenpunkte) {
        this.rundenpunkte = rundenpunkte;
    }

    // NEW getter – used by GameController to send the correct ID to the API
    public int getTeilnehmerId() {
        return teilnehmerId;
    }
}
