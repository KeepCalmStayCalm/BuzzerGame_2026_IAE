package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import  java.nio.charset.*;

/**
 * Kümmert sich um ein- und auslesen aller Fragen
 * @author jacmo
 * 
 */
public class EinAuslesenFragen {	

	public static List<Frage> einlesenFragen(String fileCSV) {
		List<Frage> fragen = new ArrayList<Frage>();

		String line = "";
		String splitBy = ";";
		File file = new File(fileCSV);

		try {
			BufferedReader br = new BufferedReader(new FileReader(fileCSV, StandardCharsets.UTF_8));
			while ((line = br.readLine()) != null && line.trim().length() > 0) {
				String[] bestandTeilLinie = line.split(splitBy);
				if (bestandTeilLinie.length >= 5){
					ArrayList<Antwort> antworten = new ArrayList<Antwort>();

					for (int i = 1; i < 4; i++) {
						Antwort a = new Antwort(bestandTeilLinie[i], false);					 
						antworten.add(a);					
					}
					int correctIndex = Integer.parseInt(bestandTeilLinie[4])-1;
					antworten.get(correctIndex).setCorrect(true);

					Frage f = new Frage(bestandTeilLinie[0], antworten);
					if (bestandTeilLinie.length > 5){
						f.setImagePath(file.getParent() + File.separator + bestandTeilLinie[5]);
					}
						
					fragen.add(f);
				}
			}
			System.out.println("Fragen einlesen länge: " + fragen.size());
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return fragen;
	}


}


