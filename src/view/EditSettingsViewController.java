package view;

import java.io.File;
import java.util.prefs.Preferences;

import application.IBuzzer;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Controller for the settings and hardware-test dialog.
 *
 * BUG FIX: original code referenced buzzer1.btnAState, buzzer1.btnBState etc.
 * as direct field accesses on IBuzzer.  Those were interface-level static fields,
 * meaning all three buzzers shared the same BooleanProperty instances — pressing
 * any button on any buzzer updated the same property, making the hardware test
 * unable to distinguish which physical buzzer was pressed.
 *
 * Fixed by calling the new per-instance getter methods (btnAStateProperty(),
 * btnBStateProperty(), btnCStateProperty()) instead.
 */
public class EditSettingsViewController {

    // ── Game settings ─────────────────────────────────────────────
    @FXML private TextField  txtQuestionFile;
    @FXML private ComboBox<Integer> comboAnzahlFragen;
    @FXML private ComboBox<Integer> comboZeitFrage;
    @FXML private RadioButton toggleRandomQuestionTrue;
    @FXML private RadioButton toggleRandomQuestionFalse;

    // ── Hardware test — buzzer 1 ──────────────────────────────────
    @FXML private RadioButton gpio1A;
    @FXML private RadioButton gpio1B;
    @FXML private RadioButton gpio1C;

    // ── Hardware test — buzzer 2 ──────────────────────────────────
    @FXML private RadioButton gpio2A;
    @FXML private RadioButton gpio2B;
    @FXML private RadioButton gpio2C;

    // ── Hardware test — buzzer 3 ──────────────────────────────────
    @FXML private RadioButton gpio3A;
    @FXML private RadioButton gpio3B;
    @FXML private RadioButton gpio3C;

    private Preferences prefs;

    @FXML
    public void initialize() {
        // Populate combo boxes
        for (int i = 1; i <= 20; i++) comboAnzahlFragen.getItems().add(i);
        for (int i = 5; i <= 60; i += 5) comboZeitFrage.getItems().add(i);
    }

    /** Called by GameController to inject saved preferences into the form. */
    public void setPreferences(Preferences prefs) {
        this.prefs = prefs;

        txtQuestionFile.setText(
            prefs.get("questions_file", "resources/fragenBuzzerGame.csv"));

        int anzahl = Integer.parseInt(prefs.get("anzahl_fragen", "3"));
        comboAnzahlFragen.setValue(anzahl);

        int zeit = Integer.parseInt(prefs.get("time_out", "10"));
        comboZeitFrage.setValue(zeit);

        boolean shuffle = prefs.getBoolean("shuffle_questions", true);
        toggleRandomQuestionTrue.setSelected(shuffle);
        toggleRandomQuestionFalse.setSelected(!shuffle);
    }

    /**
     * Called by GameController to wire the hardware-test radio buttons to the
     * physical buzzers.  Null-safe: if buzzers are not yet initialised (e.g. the
     * settings dialog is opened before the lobby), the hardware section is simply
     * left unbound.
     *
     * FIX: uses per-instance property getters instead of the old shared static
     * field references (buzzer1.btnAState → buzzer1.btnAStateProperty()).
     */
    public void setBuzzers(IBuzzer buzzer1, IBuzzer buzzer2, IBuzzer buzzer3) {
        if (buzzer1 != null) {
            // BUG FIX was here (lines 81-83 in original):
            // OLD: gpio1A.selectedProperty().bindBidirectional(buzzer1.btnAState);
            // NEW: use the per-instance getter
            gpio1A.selectedProperty().bindBidirectional(buzzer1.btnAStateProperty());
            gpio1B.selectedProperty().bindBidirectional(buzzer1.btnBStateProperty());
            gpio1C.selectedProperty().bindBidirectional(buzzer1.btnCStateProperty());
        }
        if (buzzer2 != null) {
            // BUG FIX was here (lines 87-89 in original):
            gpio2A.selectedProperty().bindBidirectional(buzzer2.btnAStateProperty());
            gpio2B.selectedProperty().bindBidirectional(buzzer2.btnBStateProperty());
            gpio2C.selectedProperty().bindBidirectional(buzzer2.btnCStateProperty());
        }
        if (buzzer3 != null) {
            // BUG FIX was here (lines 93-95 in original):
            gpio3A.selectedProperty().bindBidirectional(buzzer3.btnAStateProperty());
            gpio3B.selectedProperty().bindBidirectional(buzzer3.btnBStateProperty());
            gpio3C.selectedProperty().bindBidirectional(buzzer3.btnCStateProperty());
        }
    }

    @FXML
    private void openFileChooser() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Fragedatei auswählen");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV-Dateien", "*.csv"));

        String current = txtQuestionFile.getText();
        if (current != null && !current.isBlank()) {
            File f = new File(current).getParentFile();
            if (f != null && f.exists()) fc.setInitialDirectory(f);
        }

        Stage stage = (Stage) txtQuestionFile.getScene().getWindow();
        File selected = fc.showOpenDialog(stage);
        if (selected != null) {
            txtQuestionFile.setText(selected.getAbsolutePath());
        }
    }

    @FXML
    private void save() {
        if (prefs == null) return;

        prefs.put("questions_file",    txtQuestionFile.getText());
        prefs.put("anzahl_fragen",     String.valueOf(comboAnzahlFragen.getValue()));
        prefs.put("time_out",          String.valueOf(comboZeitFrage.getValue()));
        prefs.putBoolean("shuffle_questions", toggleRandomQuestionTrue.isSelected());

        closeDialog();
    }

    @FXML
    private void cancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) txtQuestionFile.getScene().getWindow();
        stage.close();
    }
}
