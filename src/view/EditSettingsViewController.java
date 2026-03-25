package view;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import application.IBuzzer;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;

public class EditSettingsViewController implements Initializable {
    
    @FXML private TextField txtQuestionFile;
    @FXML private ComboBox<String> comboZeitFrage;
    @FXML private ComboBox<String> comboAnzahlFragen;
    @FXML private ToggleGroup randomQuestions;
    
    @FXML private RadioButton toggleRandomQuestionTrue;
    @FXML private RadioButton toggleRandomQuestionFalse;

    @FXML private RadioButton gpio1A;
    @FXML private RadioButton gpio1B;
    @FXML private RadioButton gpio1C;
    @FXML private RadioButton gpio2A;
    @FXML private RadioButton gpio2B;
    @FXML private RadioButton gpio2C;
    @FXML private RadioButton gpio3A;
    @FXML private RadioButton gpio3B;
    @FXML private RadioButton gpio3C;
    
    private Preferences prefs;

    @FXML
    public void openFileChooser(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        
        File initialFile = new File("resources/fragenBuzzerGame.csv");
        if (initialFile.getParentFile() != null && initialFile.getParentFile().exists()) {
            fileChooser.setInitialDirectory(initialFile.getParentFile());
        }
        
        fileChooser.setTitle("Frage-Datei auswählen");
        fileChooser.getExtensionFilters().addAll(
            // FIX: added "*.CSV" (uppercase) so files like fragenBuzzerGameNeu.CSV
            // are visible in the dialog on case-sensitive file systems (Linux/Pi).
            new ExtensionFilter("CSV-Dateien", "*.csv", "*.CSV"),
            new ExtensionFilter("Alle Dateien", "*.*")
        );
        
        Window window = txtQuestionFile.getScene().getWindow();
        File chosenFile = fileChooser.showOpenDialog(window);
        
        if (chosenFile != null) {
            txtQuestionFile.setText(chosenFile.getAbsolutePath());
        }
    }

    public void setPreferences(Preferences prefs) {
        this.prefs = prefs;
        
        if (prefs == null) return;
        
        if (txtQuestionFile != null) {
            txtQuestionFile.setText(
                prefs.get("questions_file", "resources/fragenBuzzerGame.csv")
            );
        }
        
        if (comboAnzahlFragen != null) {
            String anzahl = prefs.get("anzahl_fragen", "5");
            comboAnzahlFragen.getSelectionModel().select(anzahl);
        }
        
        // Default selection is now "30" to match the new default question time
        if (comboZeitFrage != null) {
            String zeit = prefs.get("time_out", "30");
            comboZeitFrage.getSelectionModel().select(zeit);
        }
        
        boolean isRandom = prefs.getBoolean("shuffle_questions", true);
        if (toggleRandomQuestionTrue != null && toggleRandomQuestionFalse != null) {
            if (isRandom) {
                toggleRandomQuestionTrue.setSelected(true);
            } else {
                toggleRandomQuestionFalse.setSelected(true);
            }
        }
    }
    
    public void setBuzzers(IBuzzer buzzer1, IBuzzer buzzer2, IBuzzer buzzer3) {
        if (buzzer1 != null) {
            if (gpio1A != null) gpio1A.selectedProperty().bind(buzzer1.btnAState);
            if (gpio1B != null) gpio1B.selectedProperty().bind(buzzer1.btnBState);
            if (gpio1C != null) gpio1C.selectedProperty().bind(buzzer1.btnCState);
        }
        if (buzzer2 != null) {
            if (gpio2A != null) gpio2A.selectedProperty().bind(buzzer2.btnAState);
            if (gpio2B != null) gpio2B.selectedProperty().bind(buzzer2.btnBState);
            if (gpio2C != null) gpio2C.selectedProperty().bind(buzzer2.btnCState);
        }
        if (buzzer3 != null) {
            if (gpio3A != null) gpio3A.selectedProperty().bind(buzzer3.btnAState);
            if (gpio3B != null) gpio3B.selectedProperty().bind(buzzer3.btnBState);
            if (gpio3C != null) gpio3C.selectedProperty().bind(buzzer3.btnCState);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // FIX: added "30" to both combo boxes so 30 seconds is a selectable option.
        // The list is kept in ascending order for clarity.
        String[] values = new String[]{"1", "3", "5", "10", "15", "20", "25", "30", "40", "50", "60"};
        
        if (comboZeitFrage != null) {
            comboZeitFrage.getItems().addAll(values);
        }
        
        if (comboAnzahlFragen != null) {
            comboAnzahlFragen.getItems().addAll(values);
        }
    }
    
    @FXML 
    public void save() {
        if (prefs == null) {
            System.err.println("Preferences not initialized!");
            return;
        }
        
        if (txtQuestionFile != null && txtQuestionFile.getText() != null) {
            prefs.put("questions_file", txtQuestionFile.getText());
        }
        
        if (comboAnzahlFragen != null && 
            comboAnzahlFragen.getSelectionModel().getSelectedItem() != null) {
            prefs.put("anzahl_fragen", 
                     comboAnzahlFragen.getSelectionModel().getSelectedItem());
        }
        
        if (comboZeitFrage != null && 
            comboZeitFrage.getSelectionModel().getSelectedItem() != null) {
            prefs.put("time_out", 
                     comboZeitFrage.getSelectionModel().getSelectedItem());
        }
        
        if (toggleRandomQuestionTrue != null) {
            prefs.putBoolean("shuffle_questions", toggleRandomQuestionTrue.isSelected());
        }
        
        System.out.println("Settings saved");
        closeWindow();
    }
    
    @FXML 
    public void cancel() {
        closeWindow();
    }
    
    private void closeWindow() {
        if (txtQuestionFile != null && txtQuestionFile.getScene() != null) {
            Stage stage = (Stage) txtQuestionFile.getScene().getWindow();
            if (stage != null) stage.close();
        }
    }
}
