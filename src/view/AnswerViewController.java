package view;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import application.Frage;
import application.Spieler;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;

public class AnswerViewController{

	@FXML
	Label lblS1Name;
	@FXML
	Label lblS2Name;
	@FXML
	Label lblS3Name;
	@FXML
	Label lblS1PunkteZuvor;
	@FXML
	Label lblS2PunkteZuvor;
	@FXML
	Label lblS3PunkteZuvor;
	@FXML
	Label lblS1PunkteDazu;
	@FXML
	Label lblS2PunkteDazu;
	@FXML
	Label lblS3PunkteDazu;
	@FXML
	Label lblS1PunkteGesamt;
	@FXML
	Label lblS2PunkteGesamt;
	@FXML
	Label lblS3PunkteGesamt;
	@FXML
	HBox hBoxS3;
	
	@FXML Label lblAntwort;
	

	private IntegerProperty restzeit;
	private static int TIMEOUT = 15;
	private Timer timer;
	long tStart;

	public IntegerProperty getRestzeit() {
		if (restzeit == null) 
			restzeit = new SimpleIntegerProperty(TIMEOUT);
		return restzeit;
	}

	TimerTask tTask = new TimerTask() {
		@Override
		public void run() {
			getRestzeit().set(getRestzeit().get()-1);
			if (getRestzeit().intValue()==0) {
				timer.cancel();
			}

		}
	};

	public void setInformation(Frage frage, Set<Spieler> spielerSet) {
		
		lblAntwort.setText(frage.getFrage() + ": " + frage.getAntworten().get(frage.korrekteAntwortInt()-1).getAntwort());
		
		List<Spieler> spielerliste = spielerSet.stream().collect(Collectors.toList());
		Collections.sort(spielerliste, new Comparator<Spieler>() {

			@Override
			public int compare(Spieler arg0, Spieler arg1) {
				return arg1.getPunktestand().get() - arg0.getPunktestand().get();
			}
		});
		
		//lblS1Name.getStyleClass().clear();
		lblS1Name.getStyleClass().add(spielerliste.get(0).getName().toString().toLowerCase().replace(" ",""));
		
		lblS1Name.setText(spielerliste.get(0).getName().toString());
		lblS1PunkteGesamt.setText(spielerliste.get(0).getPunktestand().getValue().toString());
		lblS1PunkteDazu.setText(String.valueOf(spielerliste.get(0).getRundenpunkte()));

		//lblS2Name.getStyleClass().clear();
		lblS2Name.getStyleClass().add(spielerliste.get(1).getName().toString().toLowerCase().replace(" ",""));
		
		lblS2Name.setText(spielerliste.get(1).getName().toString());
		lblS2PunkteGesamt.setText(spielerliste.get(1).getPunktestand().getValue().toString());
		lblS2PunkteDazu.setText(String.valueOf(spielerliste.get(1).getRundenpunkte()));
		
		if(spielerliste.size() == 2) {
			lblS3Name.setText("");
			lblS3PunkteDazu.setText("");
			lblS3PunkteGesamt.setText("");
			//lblS3PunkteZuvor.setText("");
		} 
		else if(spielerliste.size() == 3) {
			int S3punkteDavor = (spielerliste.get(2).getPunktestand().getValue()) - (spielerliste.get(2).getRundenpunkte());
			System.out.println("Spieler 3 sollte angezeigt werden");
			//lblS3Name.getStyleClass().clear();
			lblS3Name.getStyleClass().add(spielerliste.get(2).getName().toString().toLowerCase().replace(" ",""));
			
			lblS3Name.setText(spielerliste.get(2).getName().toString());
			lblS3PunkteGesamt.setText(spielerliste.get(2).getPunktestand().getValue().toString());
			lblS3PunkteDazu.setText(String.valueOf(spielerliste.get(2).getRundenpunkte()));
		}
		
		lblAntwort.getScene().addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
		    //final KeyCombination keyCombinationSave = new KeyCodeCombination(KeyCode.S,
		    //                                                      KeyCodeCombination.CONTROL_DOWN);
		    
		    
			public void handle(KeyEvent ke) {
		        if (ke.getCode() == KeyCode.SPACE) {
		            System.out.println("Key Pressed: " + ke.getCode());
		            restzeit.setValue(0);
		            ke.consume(); // <-- stops passing the event to next node
		        }
		    }
		});
		
		getRestzeit().setValue(TIMEOUT);
		tStart = new Date().getTime(); //setze Startzeit
		timer = new Timer();
		timer.scheduleAtFixedRate(tTask, 100, 1000);
	

	}

}
