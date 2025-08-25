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
	@FXML
	private TextField txtQuestionFile;
	@FXML
	private ComboBox<String> comboZeitFrage;
	@FXML
	private ComboBox<String> comboAnzahlFragen;
	@FXML
	private ToggleGroup randomQuestions;
	
	@FXML RadioButton  toggleRandomQuestionTrue;
	@FXML RadioButton  toggleRandomQuestionFalse;

	@FXML RadioButton gpio1A;
	@FXML RadioButton gpio1B;
	@FXML RadioButton gpio1C;
	@FXML RadioButton gpio2A;
	@FXML RadioButton gpio2B;
	@FXML RadioButton gpio2C;
	@FXML RadioButton gpio3A;
	@FXML RadioButton gpio3B;
	@FXML RadioButton gpio3C;

	
	private Preferences prefs;

	// Event Listener on Button.onAction
	@FXML
	public void openFileChooser(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File("resources/fragenBuzzerGame.csv"));
		fileChooser.setTitle("Frage-Datei auswählen");
		fileChooser.getExtensionFilters().addAll(
		         new ExtensionFilter("CSV-Dateien", "*.csv"));
		         
		Window w = txtQuestionFile.getScene().getWindow(); 
		File choosenFile = fileChooser.showOpenDialog(w);
		if (choosenFile != null) {
			txtQuestionFile.setText(choosenFile.getAbsolutePath());
		}
	}

	public void setPreferences(Preferences prefs) {
		this.prefs = prefs;
		
		txtQuestionFile.setText(prefs.get("questions_file", "resources/fragenBuzzerGame.csv"));
		comboAnzahlFragen.getSelectionModel().select(prefs.get("anzahl_fragen", "5"));
		comboZeitFrage.getSelectionModel().select(prefs.get("time_out", "10"));
		boolean isRandom = prefs.getBoolean("shuffle_questions", true);
		if (isRandom) {
			toggleRandomQuestionTrue.setSelected(true);
		} else {
			toggleRandomQuestionFalse.setSelected(true);
		}
	}
	
	public void setBuzzers(IBuzzer buzzer1, IBuzzer buzzer2, IBuzzer buzzer3) {
		if (buzzer1 != null) {
			gpio1A.selectedProperty().bind(buzzer1.btnAState);
			gpio1B.selectedProperty().bind(buzzer1.btnBState);
			gpio1C.selectedProperty().bind(buzzer1.btnCState);
			
		} 
		if (buzzer2 != null) {
			gpio2A.selectedProperty().bind(buzzer2.btnAState);
			gpio2B.selectedProperty().bind(buzzer2.btnBState);
			gpio2C.selectedProperty().bind(buzzer2.btnCState);
			
		} 
		if (buzzer3 != null) {
			gpio3A.selectedProperty().bind(buzzer3.btnAState);
			gpio3B.selectedProperty().bind(buzzer3.btnBState);
			gpio3C.selectedProperty().bind(buzzer3.btnCState);
			
		} 
		
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		String[] values = new String[]{"1", "3", "5", "7", "10", "13", "15", "20", "42"};
		comboZeitFrage.getItems().addAll(values);
		comboAnzahlFragen.getItems().addAll(values);
		
	}
	
	@FXML public void save() {
		prefs.put("questions_file", txtQuestionFile.getText());
		prefs.put("anzahl_fragen", comboAnzahlFragen.getSelectionModel().getSelectedItem());
		prefs.put("time_out", comboZeitFrage.getSelectionModel().getSelectedItem());
		prefs.putBoolean("shuffle_questions", toggleRandomQuestionTrue.isSelected());
		System.out.println("saving stuff");
		closeWindow();
	}
	
	@FXML public void cancel() {
		System.out.println("NOT saving stuff");
		closeWindow();
	}
	
	private void closeWindow() {
		Stage w = (Stage)txtQuestionFile.getScene().getWindow();
		w.close();
	}
	
}
