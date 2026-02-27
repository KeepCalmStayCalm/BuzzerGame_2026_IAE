package application;

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

import com.pi4j.Pi4J;
import com.pi4j.context.Context;

import view.FXBuzzerController;
import view.EndViewController;
import view.LobbyViewController;
import view.QuestionViewController;
import view.AnswerViewController;
import view.EditSettingsViewController;
import view.StartupViewController;

public class GameController extends Application {

	/** Set true to run without Raspberry Pi hardware (uses MouseBuzzer / DummyBuzzer). */
	public static final boolean IS_DEV_MODE = false;

	// ── Stage / screen ────────────────────────────────────────────
	private Stage myStage;
	private double screenWidth, screenHeight;

	// ── Controllers ───────────────────────────────────────────────
	private StartupViewController startupController;

	// ── Game state ────────────────────────────────────────────────
	private int rundenCounter;
	private List<Frage> eingeleseneFragen;
	private Spielrunde spielrunde;
	private Set<Spieler> alleSpieler = new HashSet<>();
	private Frage aktuelleFrage;

	// ── Hardware / settings ───────────────────────────────────────
	private IBuzzer buzzer1, buzzer2, buzzer3;
	private int MAX_ZEIT;
	private int MAX_FRAGEN;
	private boolean shuffleQuestions;
	private boolean fullScreen;

	// BUG FIX: questionFile was declared at the very bottom of the class,
	// separated from all other fields, making it easy to miss. Moved here.
	private String questionFile;

	private Preferences prefs;
	private String style;
	private Context pi4j;

	// ── Entry point ───────────────────────────────────────────────

	public static void main(String[] args) {
		launch(args);
	}

	// ── Lifecycle ─────────────────────────────────────────────────

	@Override
	public void start(Stage primaryStage) throws Exception {
		readPreferences();
		style             = getClass().getResource("buzzerStyle2025.css").toExternalForm();
		eingeleseneFragen = EinAuslesenFragen.einlesenFragen(questionFile);
		screenWidth  = 1920;
		screenHeight = 1080;

		myStage = primaryStage;
		myStage.setTitle("IAE Buzzer Game");
		if (fullScreen) {
			myStage.setFullScreenExitHint("");
			myStage.setFullScreen(true);
		}
		myStage.setOnCloseRequest(event -> {
			System.out.println("App shutdown");
			Platform.exit();
			System.exit(0);
		});

		showStartupView();
	}

	/**
	 * BUG FIX: {@code pi4j.shutdown()} was called unconditionally, causing a
	 * NullPointerException if the app was closed before the lobby was ever opened
	 * (e.g. closing from the startup screen) or when running in IS_DEV_MODE.
	 */
	@Override
	public void stop() {
		if (pi4j != null) {
			pi4j.shutdown();
		}
	}

	// ── Preferences ───────────────────────────────────────────────

	private void readPreferences() {
		prefs           = Preferences.userRoot().node(this.getClass().getName());
		MAX_FRAGEN      = Integer.parseInt(prefs.get("anzahl_fragen",  "3"));
		MAX_ZEIT        = Integer.parseInt(prefs.get("time_out",       "10"));
		fullScreen      = prefs.getBoolean("full_screen",       true);
		shuffleQuestions = prefs.getBoolean("shuffle_questions", true);
		questionFile    = prefs.get("questions_file", "resources/fragenBuzzerGame.csv");

		// BUG FIX: the original code had a redundant IS_DEV_MODE block that set
		// questionFile to the exact same value, doing nothing useful. Removed.

		System.out.println("Prefs → MAX_ZEIT=" + MAX_ZEIT + "  MAX_FRAGEN=" + MAX_FRAGEN
		                   + "  questionFile=" + questionFile);
	}

	// ── View transitions ──────────────────────────────────────────

	public void showStartupView() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/StartupView.fxml"));
		try {
			Scene scene = new Scene(loader.load(), screenWidth, screenHeight);
			scene.getStylesheets().add(style);
			startupController = loader.getController();
			startupController.setMainController(this);
			setScene(scene);
		} catch (Exception e) {
			handleFatalError("showStartupView", e);
		}
	}

	public void showLobbyView() {
		alleSpieler.clear();

		if (!IS_DEV_MODE) {
			// BUG FIX: each call to showLobbyView() (e.g. after a round ends)
			// created a new Pi4J context without shutting down the previous one,
			// leaking GPIO resources. Shut the old context down first.
			if (pi4j != null) {
				pi4j.shutdown();
			}
			pi4j    = Pi4J.newAutoContext();
			buzzer1 = new RaspiBuzzer(pi4j, 16, 20, 21);
			buzzer2 = new RaspiBuzzer(pi4j, 22, 27, 17);
			buzzer3 = new RaspiBuzzer(pi4j, 13, 19, 26);
		} else {
			System.out.println("<-- DEV MODE ohne Hardware-Buzzer -->");
			buzzer1 = new MouseBuzzer();
			buzzer2 = new DummyBuzzer(2);
			buzzer3 = new DummyBuzzer(3);
		}

		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LobbyView.fxml"));
		try {
			Scene scene = new Scene(loader.load(), screenWidth, screenHeight);
			scene.getStylesheets().add(style);

			LobbyViewController lobbyController = loader.getController();
			lobbyController.setMainController(this);

			List<IBuzzer> buzzers = List.of(buzzer1, buzzer2, buzzer3);

			if (IS_DEV_MODE) {
				// In DEV mode all players are pre-registered, show them as ready immediately.
				for (int i = 0; i < buzzers.size(); i++) {
					alleSpieler.add(new Spieler("Spieler " + (i + 1), buzzers.get(i)));
					lobbyController.setReady(i);
				}
			} else {
				// BUG FIX: lobbyController.setReady(index) was called immediately for
				// every buzzer in the prod loop, making all three slots appear as
				// "BEREIT" the moment the lobby opened — before any player had pressed
				// a button. Moved the setReady call inside the listener so it only
				// fires when the player actually presses their buzzer.
				for (int i = 0; i < buzzers.size(); i++) {
					final LobbyViewController lc  = lobbyController;
					final int                 idx = i;
					buzzers.get(i).getAnswer().addListener(
						setupBuzzerListener("Spieler " + (i + 1), buzzers.get(i), lc, idx)
					);
				}
			}

			if (shuffleQuestions) Collections.shuffle(eingeleseneFragen);
			spielrunde = new Spielrunde(
				eingeleseneFragen.subList(0, Math.min(MAX_FRAGEN, eingeleseneFragen.size()))
			);
			System.out.println("Spielrunde erstellt mit " + MAX_FRAGEN + " Fragen.");

			setScene(scene);
		} catch (Exception e) {
			handleFatalError("showLobbyView", e);
		}
	}

	public void showQuestionView(Frage question) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/QuestionView2025.fxml"));
		try {
			Scene scene = new Scene(loader.load(), screenWidth, screenHeight);
			scene.getStylesheets().add(style);
			QuestionViewController qc = loader.getController();
			qc.initFrage(question, alleSpieler, MAX_ZEIT);
			qc.getRestzeit().addListener(showAnswerSceneListener);
			setScene(scene);
		} catch (Exception e) {
			handleFatalError("showQuestionView", e);
		}
	}

	public void showAnswerScene() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AnswerView.fxml"));
		try {
			Scene scene = new Scene(loader.load(), screenWidth, screenHeight);
			scene.getStylesheets().add(style);
			AnswerViewController ac = loader.getController();
			ac.setInformation(aktuelleFrage, alleSpieler);
			ac.getRestzeit().addListener(showNextQuestionListener);
			setScene(scene);
		} catch (Exception e) {
			handleFatalError("showAnswerScene", e);
		}
	}

	public void showEndScene() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EndView.fxml"));
		try {
			Scene scene = new Scene(loader.load(), screenWidth, screenHeight);
			scene.getStylesheets().add(style);
			EndViewController ec = loader.getController();
			ec.setMainController(this);
			ec.setSpielerInformation(alleSpieler);
			setScene(scene);
		} catch (Exception e) {
			handleFatalError("showEndScene", e);
		}
	}

	/** Opens the settings dialog. Safe to call before the lobby (buzzers may be null). */
	public void editSettings() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EditSettingsView.fxml"));
		try {
			Stage dialog = new Stage();
			dialog.initModality(Modality.APPLICATION_MODAL);
			dialog.initOwner(myStage);

			// BUG FIX: title was set AFTER showAndWait() returned, so it was
			// never visible to the user. Set it before showing the dialog.
			dialog.setTitle("Einstellungen und Hardware-Test");

			Scene scene = new Scene(loader.load());
			scene.getStylesheets().add(style);
			dialog.setScene(scene);

			EditSettingsViewController controller = loader.getController();
			controller.setPreferences(
				Preferences.userRoot().node(this.getClass().getName())
			);
			// BUG FIX: buzzers are null before the lobby is opened; passing null
			// here is safe as long as EditSettingsViewController handles it.
			controller.setBuzzers(buzzer1, buzzer2, buzzer3);

			dialog.showAndWait();
			readPreferences();   // re-load any changed values
		} catch (Exception e) {
			handleFatalError("editSettings", e);
		}
	}

	// ── Buzzer popup (legacy FXBuzzer window) ─────────────────────

	public void createBuzzerView(String playername, double x, double y) {
		FXMLLoader root = new FXMLLoader(getClass().getResource("/view/FXBuzzer.fxml"));
		try {
			Parent parent = root.load();
			FXBuzzerController buzzerCtrl = root.getController();
			Spieler s = new Spieler(playername, buzzerCtrl);
			alleSpieler.add(s);
			System.out.println(playername + " hinzugefügt (Gesamt: " + alleSpieler.size() + ")");

			Stage stage = new Stage();
			stage.setTitle(playername);
			stage.setScene(new Scene(parent));
			stage.setX(x);
			stage.setY(y);
			stage.show();
		} catch (IOException e) {
			handleFatalError("createBuzzerView", e);
		}
	}

	// ── Game flow callbacks (called by view controllers) ──────────

	public void lobbyNotifyDone() {
		if (alleSpieler.size() > 1) {
			rundenCounter = 0;
			aktuelleFrage = spielrunde.naechsteFrage();
			showQuestionView(aktuelleFrage);
		} else {
			Alert al = new Alert(Alert.AlertType.WARNING,
				"Es werden mindestens zwei Spieler benötigt.", ButtonType.OK);
			al.setTitle("Zu wenige Spieler");
			al.initModality(Modality.APPLICATION_MODAL);
			al.initOwner(myStage);
			al.showAndWait();
		}
	}

	public void scoreNotifyDone() {
		rundenCounter++;
		System.out.println("Runden gespielt: " + rundenCounter + "/" + MAX_FRAGEN);
		if (rundenCounter < MAX_FRAGEN) {
			aktuelleFrage = spielrunde.naechsteFrage();
			showQuestionView(aktuelleFrage);
		} else {
			Platform.runLater(this::showEndScene);
		}
	}

	public void endNotifyDone() {
		showLobbyView();
	}

	// ── Listener fields ───────────────────────────────────────────

	private final ChangeListener<Number> showAnswerSceneListener = (o, oldVal, newVal) -> {
		if (newVal.intValue() <= 0) {
			o.removeListener(this.showAnswerSceneListener);
			Platform.runLater(this::showAnswerScene);
		}
	};

	private final ChangeListener<Number> showNextQuestionListener = (o, oldVal, newVal) -> {
		if (newVal.intValue() <= 0) {
			o.removeListener(this.showNextQuestionListener);
			Platform.runLater(this::scoreNotifyDone);
		}
	};

	// ── Helpers ───────────────────────────────────────────────────

	/**
	 * Updated buzzer listener signature: now also receives the lobby controller
	 * and slot index so it can call {@code setReady()} at the right moment.
	 *
	 * BUG FIX: was a 2-arg method; setReady was called outside the listener.
	 */
	private ChangeListener<Number> setupBuzzerListener(
			String name, IBuzzer buzzer,
			LobbyViewController lobbyController, int slotIndex) {

		return new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends Number> obs,
								Number oldVal, Number newVal) {
				if (newVal.intValue() <= 0) return;   // ignore reset to 0
				Spieler s = new Spieler(name, buzzer);
				alleSpieler.add(s);
				buzzer.getAnswer().removeListener(this);  // one-time registration
				System.out.println(name + " bereit (" + alleSpieler.size() + " gesamt)");
				Platform.runLater(() -> lobbyController.setReady(slotIndex));
			}
		};
	}

	private void setScene(Scene scene) {
		myStage.setScene(scene);
		if (fullScreen) myStage.setFullScreen(true);
		myStage.show();
	}

	private void handleFatalError(String context, Exception e) {
		System.err.println("Fatal error in " + context + ": " + e.getMessage());
		e.printStackTrace();
		Platform.exit();
	}

	public Set<Spieler> getSpielerliste() {
		return alleSpieler;
	}
}
