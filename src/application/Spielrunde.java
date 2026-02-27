package application;


import java.util.List;

public class Spielrunde {


	//private List<Fragerunde> fragerunden = new ArrayList<Fragerunde>();
	List<Frage> tenQuestions;

	private int fragerunden;

	public Spielrunde(List<Frage> tenQuestions) {
		fragerunden = 0;
		this.tenQuestions = tenQuestions;
	}




	public Frage naechsteFrage() {
		System.out.println("Anzahl Fragen in Array: " + tenQuestions.size());
		System.out.println("Anzahl Fragen gestellt: " + fragerunden);
		Frage aktuelleFrage = tenQuestions.get(fragerunden);
		fragerunden++;
		System.out.println("Fragerunde: " + fragerunden);
		System.out.println("Frage: " + tenQuestions.get(fragerunden-1).getFrage());
		return aktuelleFrage;
	}


	//	public void start(Frage f, List<Spieler> spieler) {
	//		//für jede Frage aus der Liste neue Fragerunde erstellen und ausführen
	//		Fragerunde fr = new Fragerunde(f, spieler);
	//		fragerunden.add(fr);
	//		//fr.start();
	//	}
	//
	//	public List<Fragerunde> getFr() {
	//		return fragerunden;
	//	}
	//
	//	public void setFr(List<Fragerunde> fr) {
	//		this.fragerunden = fr;
	//	}

}
