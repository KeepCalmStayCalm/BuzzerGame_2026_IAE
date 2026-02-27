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

    /**
     * Open file chooser for question file selection
     */
    @FXML
    public void openFileChooser(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        
        // Set initial directory
        File initialFile = new File("resources/fragenBuzzerGame.csv");
        if (initialFile.getParentFile() != null && initialFile.getParentFile().exists()) {
            fileChooser.setInitialDirectory(initialFile.getParentFile());
        }
        
        fileChooser.setTitle("Frage-Datei auswählen");
        fileChooser.getExtensionFilters().addAll(
            new ExtensionFilter("CSV-Dateien", "*.csv"),
            new ExtensionFilter("Alle Dateien", "*.*")
        );
        
        Window window = txtQuestionFile.getScene().getWindow();
        File chosenFile = fileChooser.showOpenDialog(window);
        
        if (chosenFile != null) {
            txtQuestionFile.setText(chosenFile.getAbsolutePath());
        }
    }

    /**
     * Load settings from preferences
     */
    public void setPreferences(Preferences prefs) {
        this.prefs = prefs;
        
        if (prefs == null) return;
        
        // Load question file path
        if (txtQuestionFile != null) {
            txtQuestionFile.setText(
                prefs.get("questions_file", "resources/fragenBuzzerGame.csv")
            );
        }
        
        // Load number of questions
        if (comboAnzahlFragen != null) {
            String anzahl = prefs.get("anzahl_fragen", "5");
            comboAnzahlFragen.getSelectionModel().select(anzahl);
        }
        
        // Load time per question
        if (comboZeitFrage != null) {
            String zeit = prefs.get("time_out", "10");
            comboZeitFrage.getSelectionModel().select(zeit);
        }
        
        // Load shuffle setting
        boolean isRandom = prefs.getBoolean("shuffle_questions", true);
        if (toggleRandomQuestionTrue != null && toggleRandomQuestionFalse != null) {
            if (isRandom) {
                toggleRandomQuestionTrue.setSelected(true);
            } else {
                toggleRandomQuestionFalse.setSelected(true);
            }
        }
    }
    
    /**
     * Bind buzzer buttons to state properties for hardware testing
     */
    public void setBuzzers(IBuzzer buzzer1, IBuzzer buzzer2, IBuzzer buzzer3) {
        // Buzzer 1
        if (buzzer1 != null) {
            if (gpio1A != null) gpio1A.selectedProperty().bind(buzzer1.btnAState);
            if (gpio1B != null) gpio1B.selectedProperty().bind(buzzer1.btnBState);
            if (gpio1C != null) gpio1C.selectedProperty().bind(buzzer1.btnCState);
        }
        
        // Buzzer 2
        if (buzzer2 != null) {
            if (gpio2A != null) gpio2A.selectedProperty().bind(buzzer2.btnAState);
            if (gpio2B != null) gpio2B.selectedProperty().bind(buzzer2.btnBState);
            if (gpio2C != null) gpio2C.selectedProperty().bind(buzzer2.btnCState);
        }
        
        // Buzzer 3
        if (buzzer3 != null) {
            if (gpio3A != null) gpio3A.selectedProperty().bind(buzzer3.btnAState);
            if (gpio3B != null) gpio3B.selectedProperty().bind(buzzer3.btnBState);
            if (gpio3C != null) gpio3C.selectedProperty().bind(buzzer3.btnCState);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Populate combo boxes with options
        String[] values = new String[]{"1", "3", "5", "7", "10", "13", "15", "20", "42"};
        
        if (comboZeitFrage != null) {
            comboZeitFrage.getItems().addAll(values);
        }
        
        if (comboAnzahlFragen != null) {
            comboAnzahlFragen.getItems().addAll(values);
        }
    }
    
    /**
     * Save settings and close window
     */
    @FXML 
    public void save() {
        if (prefs == null) {
            System.err.println("Preferences not initialized!");
            return;
        }
        
        // Save question file
        if (txtQuestionFile != null && txtQuestionFile.getText() != null) {
            prefs.put("questions_file", txtQuestionFile.getText());
        }
        
        // Save number of questions
        if (comboAnzahlFragen != null && 
            comboAnzahlFragen.getSelectionModel().getSelectedItem() != null) {
            prefs.put("anzahl_fragen", 
                     comboAnzahlFragen.getSelectionModel().getSelectedItem());
        }
        
        // Save time per question
        if (comboZeitFrage != null && 
            comboZeitFrage.getSelectionModel().getSelectedItem() != null) {
            prefs.put("time_out", 
                     comboZeitFrage.getSelectionModel().getSelectedItem());
        }
        
        // Save shuffle setting
        if (toggleRandomQuestionTrue != null) {
            prefs.putBoolean("shuffle_questions", toggleRandomQuestionTrue.isSelected());
        }
        
        System.out.println("Settings saved successfully");
        closeWindow();
    }
    
    /**
     * Cancel without saving
     */
    @FXML 
    public void cancel() {
        System.out.println("Settings cancelled - not saving");
        closeWindow();
    }
    
    /**
     * Close the settings window
     */
    private void closeWindow() {
        if (txtQuestionFile != null && txtQuestionFile.getScene() != null) {
            Stage stage = (Stage) txtQuestionFile.getScene().getWindow();
            if (stage != null) {
                stage.close();
            }
        }
    }
}
