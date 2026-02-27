package application;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;


public class Spieler {

	String name;
	IntegerProperty punktestand;
	int rundenpunkte;
	IntegerProperty antwortNr;
	private IBuzzer buzzer;

	@Override
	public boolean equals(Object other) {
		if (other.getClass().equals(this.getClass()))
			return false;
		return ((Spieler)other).getName().equals(this.getName());
	}

	public Spieler(String name, IBuzzer buzzer) {
		super();
		this.name = name;
		this.punktestand = new SimpleIntegerProperty();
		this.antwortNr = new SimpleIntegerProperty(0);
		this.setBuzzer(buzzer);
		if (buzzer != null) {
			this.antwortNr.bind(getBuzzer().getAnswer());
		}
	}

	//Spieler muss mitbekommen, dass Buzzer gedrückt
	//GameContrller muss mitbekommen, dass von Spieler X ein BUzzer betätigt wurde
	//gaem controller entscheidet, welcher Spieler richtig gedrückt hat und verteilt punkte

	/*public void buzzerGedrueckt() {
		//antwortNr.set(buzzer.welcherBuzzerGedrueckt());
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
		if(antwortNr == null) {
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

}
