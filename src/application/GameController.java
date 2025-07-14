package application;


import com.pi4j.io.gpio.RaspiPin;
import java.io.IOException;
import java.util.*;

import javafx.application.Application;
import javafx.application.Platform;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.prefs.Preferences;

import view.FXBuzzerController;
import view.EndViewController;
import view.LobbyViewController;
import view.QuestionViewController;
import view.AnswerViewController;
import view.EditSettingsViewController;
import view.StartupViewController;

public class GameController extends Application {

	public static final boolean IS_DEV_MODE = true;

	private Stage myStage = null;
	private double screenHeight, screenWidth;

	private StartupViewController startupController;
	private int rundenCounter;
	private List<Frage> eingeleseneFragen;

	private Spielrunde spielrunde;
	private Set<Spieler> alleSpieler = new HashSet<Spieler>();
	private IBuzzer buzzer1, buzzer2, buzzer3;
	
	private int MAX_ZEIT;
	private int MAX_FRAGEN;
	private Frage aktuelleFrage;
	private boolean shuffleQuestions;
	private boolean fullScreen;

	private Preferences prefs;
	private String style;
	
	public static void main(String[] args) {

		System.out.println("DEVELOPMENT_MODE: "+System.getenv("DEVELOPMENT_MODE"));

		launch(args);
	}
	
	private void readPreferences(){
		System.out.println("Prefs-File: "+Preferences.userRoot().node(this.getClass().getName()));
		prefs = Preferences.userRoot().node(this.getClass().getName());
		MAX_FRAGEN = Integer.parseInt(prefs.get("anzahl_fragen", "3"));
		fullScreen = prefs.getBoolean("full_screen", true);
		MAX_ZEIT = Integer.parseInt(prefs.get("time_out", "10"));	
		shuffleQuestions = prefs.getBoolean("shuffle_questions", true);	
		questionFile = prefs.get("questions_file", "/home/pi/Desktop/fragenBuzzerGame_290620.csv");
		if (IS_DEV_MODE) {
			questionFile = prefs.get("questions_file", "resources/fragenBuzzerGame.csv");
		}
		
		System.out.println("MAX_ZEIT: "+MAX_ZEIT);
		System.out.println("MAX_FRAGEN: "+MAX_FRAGEN);
	}
	
	

	@Override
	public void start(Stage primaryStage) throws Exception {
		readPreferences();
		
		style = getClass().getResource("buzzerStyle2025.css").toExternalForm();
		
		eingeleseneFragen = EinAuslesenFragen.einlesenFragen(questionFile);
		screenWidth = 1200;
		screenHeight = 800;
		try {
			myStage = primaryStage;
			myStage.setTitle("Buzzer Game");
			if (fullScreen) {
				myStage.setFullScreenExitHint("");
				myStage.setFullScreen(true);
			}
			showStartupView();

		} catch (Exception e) {
			e.printStackTrace();
			Platform.exit();
		}
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent event) {
				System.out.println("App shutdown");
				Platform.exit();
				System.exit(0);
			}
			
		});
	}

	public void showStartupView() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/StartupView.fxml"));
		
		if (!IS_DEV_MODE) {
			buzzer1 = new RaspiBuzzer(RaspiPin.GPIO_27, RaspiPin.GPIO_28, RaspiPin.GPIO_29);
			buzzer2 = new RaspiBuzzer(RaspiPin.GPIO_03, RaspiPin.GPIO_02, RaspiPin.GPIO_00);
			buzzer3 = new RaspiBuzzer(RaspiPin.GPIO_23, RaspiPin.GPIO_24, RaspiPin.GPIO_25);
		} else {
			buzzer1 = new MouseBuzzer();
			buzzer2 = new DummyBuzzer(2);
			buzzer3 = new DummyBuzzer(3);
		}
			
		
		try {
			Scene startupScene = new Scene(loader.load(), screenWidth, screenHeight);
			startupScene.getStylesheets().add(style);
			startupController = loader.getController();
			startupController.setMainController(this);
			
			myStage.setScene(startupScene);
			if (fullScreen) myStage.setFullScreen(true);
			myStage.show();
		} catch(Exception e) {
			e.printStackTrace();
			Platform.exit();
		}
	}


	public void showLobbyView() {
		alleSpieler.clear();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LobbyView.fxml"));
		try {
			Scene lobbyScene = new Scene(loader.load(), screenWidth, screenHeight);
			
			lobbyScene.getStylesheets().add(style);
			LobbyViewController lobbyController = loader.getController();
			lobbyController.setMainController(this);


			if (IS_DEV_MODE) {
				Spieler s = new Spieler("Spieler 1", buzzer1);
				alleSpieler.add(s);
				lobbyController.setReady1();
				Spieler s2 = new Spieler("Spieler 2", buzzer2);
				alleSpieler.add(s2);
				lobbyController.setReady2();
				Spieler s3 = new Spieler("Spieler 3", buzzer3);
				alleSpieler.add(s3);
				lobbyController.setReady3();
			} else {
				buzzer1.getAnswer().addListener(new ChangeListener<Number>() {
	
				@Override
				public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
					Spieler s = new Spieler("Spieler 1", buzzer1);
					alleSpieler.add(s);
					buzzer1.getAnswer().removeListener(this);
					System.out.println("Spieler1 erstellt");
					lobbyController.setReady1();				
					
				}			
				});
			
				buzzer2.getAnswer().addListener(new ChangeListener<Number>() {
		
					@Override
					public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
						Spieler s = new Spieler("Spieler 2", buzzer2);
						alleSpieler.add(s);
						buzzer2.getAnswer().removeListener(this);
						System.out.println("Spieler2 erstellt");
						lobbyController.setReady2();
					}			
				});

				buzzer3.getAnswer().addListener(new ChangeListener<Number>() {
	
					@Override
					public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
						Spieler s = new Spieler("Spieler 3", buzzer3);
						alleSpieler.add(s);
						buzzer3.getAnswer().removeListener(this);
						System.out.println("Spieler3 erstellt");
						lobbyController.setReady3();
					}			
				});

			}
			
			if (shuffleQuestions)
				Collections.shuffle(eingeleseneFragen);
			
			System.out.println("Size eingelesene Fragen: " + eingeleseneFragen.size());
			spielrunde = new Spielrunde(eingeleseneFragen.subList(0, MAX_FRAGEN));
			System.out.println("Spielrunde erstellt");
			
			myStage.setScene(lobbyScene);
			if (fullScreen) myStage.setFullScreen(true);
			myStage.show();	
			
								
		} catch (Exception e) {
			e.printStackTrace();
			Platform.exit();
		}
		
	}

	public void createBuzzerView(String playername, double yPosition, double xPosition) {

		try {
			FXMLLoader root = new FXMLLoader(getClass().getResource("../view/FXBuzzer.fxml"));
			Parent parent = root.load();
			Stage stage = new Stage();
			Scene scene = new Scene(parent);
			stage.setTitle(playername);
			
			FXBuzzerController buzzerSet1Controller = root.getController();
			
			Spieler s = new Spieler(playername, buzzerSet1Controller);

			if(alleSpieler.size() == 0) {
				System.out.println("Erster Spieler hinzugefügt");
				alleSpieler.add(s);
				stage.setScene(scene);
				stage.setY(yPosition);
				stage.setX(xPosition);
				stage.show();				
			}
			else {
				alleSpieler.add(s);
				System.out.println(s.getName() + " hinzugefügt");
				stage.setScene(scene);
				stage.setY(yPosition);
				stage.setX(xPosition);
				stage.show();
			}

		} catch (IOException e) {
			e.printStackTrace();
			Platform.exit();
		}

	}

	public void showQuestionView(Frage question) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/QuestionView2025.fxml"));
		try {

			Scene questionScene = new Scene(loader.load(), screenWidth, screenHeight);

			QuestionViewController questionController = loader.getController();
			questionController.initFrage(question, alleSpieler, MAX_ZEIT);
			
			questionController.getRestzeit().addListener(showAnswerSceneListener);

			myStage.setScene(questionScene);
			if (fullScreen) myStage.setFullScreen(true);
			myStage.show();

		} catch(Exception e) {
			e.printStackTrace();
			Platform.exit();
		}
	}


	//XD added showScoreScene()
	public void showAnswerScene() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AnswerView.fxml"));
		try {
			Scene answerScene = new Scene(loader.load(), screenWidth, screenHeight);
			answerScene.getStylesheets().add(style);
			AnswerViewController scoreController = loader.getController();
			scoreController.setInformation(aktuelleFrage, alleSpieler);
			scoreController.getRestzeit().addListener(showNextQuestionListener);

			myStage.setScene(answerScene);
			if (fullScreen) myStage.setFullScreen(true);
			myStage.show();
		} catch (Exception e) {
			e.printStackTrace();
			Platform.exit();
		}
	}

	public void showEndScene() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EndView.fxml"));
		try {
			Scene endScene = new Scene(loader.load(), screenWidth, screenHeight);
			endScene.getStylesheets().add(style);

			EndViewController endController = loader.getController();
			endController.setMainController(this);

			endController.setSpielerInformation(alleSpieler);

			
			myStage.setScene(endScene);
			if (fullScreen) myStage.setFullScreen(true);
			myStage.show();
		} catch (Exception e) {
			e.printStackTrace();
			Platform.exit();
		}
	}


	public void lobbyNotifyDone() {
		if(getSpielerliste().size() > 1){
			rundenCounter = 0;
			aktuelleFrage = spielrunde.naechsteFrage();
			showQuestionView(aktuelleFrage);
		}
		else {
			Alert al = new Alert(Alert.AlertType.WARNING, "Es sind mindestens zwei Spieler benötigt",ButtonType.OK);
			al.initModality(Modality.APPLICATION_MODAL);
	        al.initOwner(myStage);
			al.showAndWait();
		}
	}

	//von Score zu nächste Frage oder Ende
	public void scoreNotifyDone() {
		System.out.println("'Gamecontroller: ' Runden gespielt: " + rundenCounter);
		rundenCounter++;
		if (rundenCounter < MAX_FRAGEN){
			aktuelleFrage = spielrunde.naechsteFrage();
			showQuestionView(aktuelleFrage);
		} else {
			Platform.runLater(() -> showEndScene());
		}

	}

	public void endNotifyDone() {
		System.out.println("Spielerliste grösse: "+ alleSpieler.size());
		showLobbyView();
		
	}

	private ChangeListener<Number> showAnswerSceneListener = (o, a, newValue) -> {
		if (newValue.intValue() <= 0) {	
			System.out.println("Switching to AnswerView");		
			o.removeListener(this.showAnswerSceneListener);
			Platform.runLater(() -> showAnswerScene());
		}
	};

	private ChangeListener<Number> showNextQuestionListener = (o, a, newValue) -> {
		if (newValue.intValue() <= 0) {
			System.out.println("Switching to NextQuestionView");
			o.removeListener(this.showNextQuestionListener);
			Platform.runLater(() -> scoreNotifyDone());
		}
	};
	private String questionFile;

	
	public Set<Spieler> getSpielerliste() {
		return alleSpieler;
	}

	public void editSettings() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EditSettingsView.fxml"));
			
			Scene scene = new Scene(loader.load());
			EditSettingsViewController controller = loader.getController();
			
			Preferences prefs = Preferences.userRoot().node(this.getClass().getName()); 
			
			controller.setPreferences(prefs);
			controller.setBuzzers(buzzer1, buzzer2, buzzer3);
			
			//scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			Stage ediStage = new Stage();
			ediStage.setScene(scene);
			ediStage.showAndWait();
			ediStage.setTitle("Einstellungen und Hardwaretest");
			readPreferences();
		} catch(Exception e) {
			e.printStackTrace();
			Platform.exit();
		}
	}


}
