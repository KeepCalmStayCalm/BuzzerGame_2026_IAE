package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.nio.charset.StandardCharsets;

/**
 * Reads questions from a CSV file.
 *
 * Format per line: question;answer1;answer2;answer3;correctIndex(1-3)
 *
 * FIX: previously a row with a correct-answer index outside 1–3 (e.g. "4")
 * caused an IndexOutOfBoundsException that was silently caught, resulting in
 * 0 questions being returned and the game crashing the moment play was started.
 * Now every row is validated before being added, and bad rows are skipped with
 * a clear warning instead of crashing the entire load.
 */
public class EinAuslesenFragen {

    public static List<Frage> einlesenFragen(String fileCSV) {
        List<Frage> fragen = new ArrayList<>();
        int skipped = 0;

        File file = new File(fileCSV);
        if (!file.exists()) {
            System.err.println("CSV file not found: " + fileCSV);
            return fragen;
        }

        try (BufferedReader br = new BufferedReader(
                new FileReader(fileCSV, StandardCharsets.UTF_8))) {

            String line;
            int lineNumber = 0;

            while ((line = br.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(";");

                // Need at least 5 columns: question + 3 answers + correct index
                if (parts.length < 5) {
                    System.err.println("Line " + lineNumber + " skipped: only "
                            + parts.length + " columns (need 5)");
                    skipped++;
                    continue;
                }

                // Parse and validate the correct-answer index
                int correctIndex;
                try {
                    correctIndex = Integer.parseInt(parts[4].trim()) - 1; // 0-based
                } catch (NumberFormatException e) {
                    System.err.println("Line " + lineNumber + " skipped: '"
                            + parts[4] + "' is not a valid answer index");
                    skipped++;
                    continue;
                }

                // Index must point to one of the 3 answers (0, 1, or 2)
                if (correctIndex < 0 || correctIndex > 2) {
                    System.err.println("Line " + lineNumber + " skipped: correct index "
                            + (correctIndex + 1) + " is out of range (must be 1–3)");
                    skipped++;
                    continue;
                }

                // Build the answer list
                ArrayList<Antwort> antworten = new ArrayList<>();
                for (int i = 1; i <= 3; i++) {
                    antworten.add(new Antwort(parts[i].trim(), false));
                }
                antworten.get(correctIndex).setCorrect(true);

                Frage f = new Frage(parts[0].trim(), antworten);

                // Optional image path in column 6
                if (parts.length > 5 && !parts[5].trim().isEmpty()) {
                    f.setImagePath(file.getParent() + File.separator + parts[5].trim());
                }

                fragen.add(f);
            }

        } catch (Exception e) {
            System.err.println("Error reading CSV: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Questions loaded: " + fragen.size()
                + (skipped > 0 ? " (" + skipped + " rows skipped)" : ""));
        return fragen;
    }
}
