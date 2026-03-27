package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.nio.charset.*;

public class EinAuslesenFragen {

    public static List<Frage> einlesenFragen(String fileCSV) {
        List<Frage> fragen = new ArrayList<>();

        String line = "";
        File file = new File(fileCSV);

        try {
            BufferedReader br = new BufferedReader(
                    new FileReader(fileCSV, StandardCharsets.UTF_8));

            while ((line = br.readLine()) != null && line.trim().length() > 0) {

                // Use quoted-CSV-aware splitter instead of line.split(";")
                String[] bestandTeilLinie = splitCSVLine(line, ';');

                if (bestandTeilLinie.length >= 5) {
                    ArrayList<Antwort> antworten = new ArrayList<>();

                    for (int i = 1; i < 4; i++) {
                        antworten.add(new Antwort(bestandTeilLinie[i], false));
                    }

                    int correctIndex = Integer.parseInt(
                            bestandTeilLinie[4].trim()) - 1;

                    // Defensive bounds check — skip rows with an invalid index
                    if (correctIndex < 0 || correctIndex >= antworten.size()) {
                        System.err.println("EinAuslesenFragen: skipping row with "
                                + "invalid correct-answer index "
                                + (correctIndex + 1)
                                + " (must be 1–" + antworten.size() + "): "
                                + line);
                        continue;
                    }

                    antworten.get(correctIndex).setCorrect(true);

                    Frage f = new Frage(bestandTeilLinie[0], antworten);
                    if (bestandTeilLinie.length > 5) {
                        f.setImagePath(file.getParent()
                                + File.separator + bestandTeilLinie[5]);
                    }

                    fragen.add(f);
                }
            }
            System.out.println("Fragen einlesen länge: " + fragen.size());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fragen;
    }

    private static String[] splitCSVLine(String line, char delimiter) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;          // toggle quoted mode, strip quote char
            } else if (c == delimiter && !inQuotes) {
                fields.add(current.toString()); // end of field
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());        // last field (no trailing delimiter)

        return fields.toArray(new String[0]);
    }
}
