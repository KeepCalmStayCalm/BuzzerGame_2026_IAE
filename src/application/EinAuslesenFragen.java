package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Liest Fragen aus einer CSV-Datei ein.
 * Format: Frage;AntwortA;AntwortB;AntwortC;KorrekteAntwort(1-3)[;BildPfad]
 */
public class EinAuslesenFragen {

	public static List<Frage> einlesenFragen(String fileCSV) {
		List<Frage> fragen = new ArrayList<>();
		File file = new File(fileCSV);

		// BUG FIX: BufferedReader was never closed — resource leak on every load.
		// Replaced bare try{} with try-with-resources.
		try (BufferedReader br = new BufferedReader(
				new FileReader(fileCSV, StandardCharsets.UTF_8))) {

			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty()) continue;           // skip blank lines

				String[] parts = line.split(";");
				if (parts.length < 5) {
					System.err.println("Zeile übersprungen (zu wenig Felder): " + line);
					continue;
				}

				// Build the three answer objects
				ArrayList<Antwort> antworten = new ArrayList<>();
				for (int i = 1; i <= 3; i++) {
					antworten.add(new Antwort(parts[i].trim(), false));
				}

				// BUG FIX: no bounds check; a malformed CSV value caused an
				// uncaught IndexOutOfBoundsException that silently killed the load.
				int correctIndex;
				try {
					correctIndex = Integer.parseInt(parts[4].trim()) - 1;
				} catch (NumberFormatException e) {
					System.err.println("Ungültiger Korrekt-Index in Zeile: " + line);
					continue;
				}
				if (correctIndex < 0 || correctIndex >= antworten.size()) {
					System.err.println("Korrekt-Index außerhalb 1-3 in Zeile: " + line);
					continue;
				}
				antworten.get(correctIndex).setCorrect(true);

				Frage f = new Frage(parts[0].trim(), antworten);
				if (parts.length > 5 && !parts[5].isBlank()) {
					f.setImagePath(file.getParent() + File.separator + parts[5].trim());
				}
				fragen.add(f);
			}

		} catch (Exception e) {
			System.err.println("Fehler beim Einlesen der Fragen: " + e.getMessage());
			e.printStackTrace();
		}

		System.out.println("Fragen eingelesen: " + fragen.size());
		return fragen;
	}
}
