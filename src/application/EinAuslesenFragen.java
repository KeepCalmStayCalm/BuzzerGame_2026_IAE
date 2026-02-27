package application;

import java.io.Serializable;
import java.util.List;

/**
 * Klasse Frage verwaltet eine Fragen und die dazugehörigen Antworten
 * @author jacmo
 * 
 */
public class Frage implements Serializable {

	private String frage;
	private List<Antwort> antworten;
	private String imagePath;

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	/**
	 * 
	 * @param frage: Frage als String eingeben
	 * @param a: ArrayListe mit Antworten
	 */
	public Frage(String frage, List<Antwort> a) {
		this.setFrage(frage);
		this.antworten = a;
	}	

	/**
	 * 
	 * @param a: eine Antwort
	 * @return: gibt boolean zurück, ob Antwort korrekt ist
	 */
	public boolean pruefeAntwort(Antwort a) {
		return a.isCorrect();
	}



	/**
	 * Alle Antworten werden durchgeschleift und die richtige herausgesucht
	 * @return: die richtige Antwort wird als String zurückgegeben
	 */
	public String korrekteAntwort() {
		String korrekteAntwort = null;
		for (int i = 0; i < antworten.size(); i++) {
			if(antworten.get(i).isCorrect()) {
				korrekteAntwort = antworten.get(i).getAntwort();
			}
		}
		return korrekteAntwort;		
	}

	/**
	 * Ermittelt die Nr, die vom Buzzer gedrückt werden muss
	 * @return gibt die AntwortNummer zurück
	 */		
	public int korrekteAntwortInt() {
		int korrekteAntwort = 0;
		for (int i = 0; i < antworten.size(); i++) {
			if(antworten.get(i).isCorrect()) {
				korrekteAntwort = i+1;
			}
		}
		return korrekteAntwort;
	}

	public String getFrage() {
		return frage;
	}

	public void setFrage(String frage) {
		this.frage = frage;
	}

	public List<Antwort> getAntworten() {
		return antworten;
	}

	public void setAntworten(List<Antwort> antworten) {
		this.antworten = antworten;
	}

	/*public void frageAnzeigen() {
		System.out.println(frage);		
	}*/


}
