package view;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import application.Antwort;
import application.Frage;
import application.GameController;
import application.MouseBuzzer;
import application.Spieler;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

public class QuestionViewController implements Initializable {

	GameController gameController;
	private Frage frage;
	@FXML private Label lblRestzeit;
	@FXML private Label lblFrage;
	@FXML private Label lblAntwort1;
	@FXML private Label lblAntwort2;
	@FXML private Label lblAntwort3;

	@FXML BorderPane imageRoot;
	@FXML private ImageView image;
	
	private IntegerProperty restzeit;
	private Timer timer;
	long timeStart;
	int maxZeit;
	int antworten = 0;

	public IntegerProperty getRestzeit() {
		if (restzeit == null)
			restzeit = new SimpleIntegerProperty(maxZeit);
		return restzeit;
	}
	
	public void setMainController(GameController mainController) {
		this.gameController = mainController;
	}
	
	public void initFrage(Frage frage, Set<Spieler> spielerliste, int maxZeit) {
		this.frage = frage;
		lblFrage.setText(frage.getFrage());
		setAnswers(frage.getAntworten());
		this.maxZeit = maxZeit;
		this.timeStart = System.currentTimeMillis();

		if (frage.getImagePath() != null) {
			try {
				InputStream is = new FileInputStream(frage.getImagePath());
				image.setImage(new Image(is));
			} catch (Exception e) {
				System.out.println("Image not found: " + frage.getImagePath());
			}
		} else {
			InputStream is = this.getClass().getResourceAsStream("/resources/images/wiss_home.jpg");
			if (is != null) image.setImage(new Image(is));
		}

		// Fix für den NullPointerException: Prüfen ob imageRoot in FXML geladen wurde
		if (imageRoot != null && image.getImage() != null) {
			image.fitWidthProperty().bind(imageRoot.widthProperty());
			image.fitHeightProperty().bind(imageRoot.heightProperty());
		}
				
		getRestzeit().setValue(maxZeit);		
		timer = new Timer();
		timer.scheduleAtFixedRate(tTask, 0, 1000);
		initPlayers(spielerliste);
	}
	
	private void initPlayers(Set<Spieler> spielerliste) {
		if (GameController.IS_DEV_MODE) {
			spielerliste.iterator().forEachRemaining(spieler -> {
				if (spieler.getBuzzer() instanceof MouseBuzzer) {
					spieler.getAntwortNr().addListener(new ChangeListener<Number>() {
						@Override
						public void changed(ObservableValue<? extends Number> arg0, Number alt, Number neu) {	
							if((int)neu == frage.korrekteAntwortInt()) {
								long pressedTime = System.currentTimeMillis();
								int punkte = Math.max(0, (maxZeit*1000 - (int)(pressedTime - timeStart))/100);
								spieler.addPunkte(punkte);
								spieler.setRundenpunkte(punkte);
							}
							spieler.getAntwortNr().removeListener(this);
							antworten++;
							if (antworten >= spielerliste.size()) restzeit.set(0);
						}
					});

					lblAntwort1.setOnMouseClicked((val) -> ((MouseBuzzer)spieler.getBuzzer()).getAnswer().setValue(1));	
					lblAntwort2.setOnMouseClicked((val) -> ((MouseBuzzer)spieler.getBuzzer()).getAnswer().setValue(2));	
					lblAntwort3.setOnMouseClicked((val) -> ((MouseBuzzer)spieler.getBuzzer()).getAnswer().setValue(3));	
				}
			});
			return;
		}
		
		new HashSet<>(spielerliste).forEach(spieler -> {		
			spieler.reset();
			spieler.getAntwortNr().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> arg0, Number alt, Number neu) {	
					if((int)neu == frage.korrekteAntwortInt()) {
						long pressedTime = System.currentTimeMillis();
						int punkte = Math.max(0, (maxZeit*1000 - (int)(pressedTime - timeStart))/100);
						spieler.addPunkte(punkte);
						spieler.setRundenpunkte(punkte);
					}
					spieler.getAntwortNr().removeListener(this);
					antworten++;
					if (antworten >= spielerliste.size()) restzeit.set(0);
				}
			});
			spieler.setRundenpunkte(0);
		});
	}
	
	private void setAnswers(List<Antwort> antworten) {		
		lblAntwort1.setText(antworten.get(0).getAntwort());
		lblAntwort2.setText(antworten.get(1).getAntwort());
		lblAntwort3.setText(antworten.get(2).getAntwort());
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {}
	
	TimerTask tTask = new TimerTask() {
		@Override
		public void run() {
			getRestzeit().setValue(maxZeit - (int)(System.currentTimeMillis()-timeStart)/1000);
			Platform.runLater(updateRestzeitLabel); 
			if (getRestzeit().intValue()<=0) timer.cancel();
		}
	};
	
	private Runnable updateRestzeitLabel = () -> lblRestzeit.setText(String.valueOf(getRestzeit().intValue()));
}
