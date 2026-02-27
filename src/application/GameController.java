package application;

import java.io.IOException;
import java.util.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.prefs.Preferences;
import com.pi4j.Pi4J;
import com.pi4j.context.Context;

import view.*;

public class GameController extends Application {

    public static final boolean IS_DEV_MODE = false;

    private Stage myStage;
    private StartupViewController startupController;
    private int rundenCounter = 0;
    private List<Frage> eingeleseneFragen;
    private Spielrunde spielrunde;
    private Set<Spieler> alleSpieler = new HashSet<>();
    private IBuzzer buzzer1, buzzer2, buzzer3;

    private int MAX_ZEIT = 20;
    private int MAX_FRAGEN = 5;
    private Frage aktuelleFrage;
    private boolean shuffleQuestions = true;
    private boolean fullScreen = true;

    private Preferences prefs;
    private String style;
    private Context pi4j;
    private String questionFile = "resources/fragenBuzzerGame.csv";

    private ChangeListener<Number> showAnswerSceneListener;
    private ChangeListener<Number> showNextQuestionListener;

    public static void main(String[] args) {
        launch(args);
    }

    private void readPreferences() {
        prefs = Preferences.userRoot().node(this.getClass().getName());
        MAX_FRAGEN = Integer.parseInt(prefs.get("anzahl_fragen", "5"));
        MAX_ZEIT = Integer.parseInt(prefs.get("time_out", "20"));
        shuffleQuestions = prefs.getBoolean("shuffle_questions", true);
        fullScreen = prefs.getBoolean("full_screen", true);
        questionFile = prefs.get("questions_file", "resources/fragenBuzzerGame.csv");
    }

    private void initListeners() {
        showAnswerSceneListener = (o, a, newValue) -> {
            if (newValue.intValue() <= 0) {
                o.removeListener(showAnswerSceneListener);
                Platform.runLater(this::showAnswerScene);
            }
        };

        showNextQuestionListener = (o, a, newValue) -> {
            if (newValue.intValue() <= 0) {
                o.removeListener(showNextQuestionListener);
                Platform.runLater(this::scoreNotifyDone);
            }
        };
    }

    @Override
    public void stop() {
        if (pi4j != null) pi4j.shutdown();
    }

    @Override
    public void start(Stage primaryStage) {
        readPreferences();
        initListeners();   // important - listeners created after methods exist

        style = getClass().getResource("buzzerStyle2025.css").toExternalForm();
        eingeleseneFragen = EinAuslesenFragen.einlesenFragen(questionFile);

        myStage = primaryStage;
        myStage.setTitle("IAE Buzzer Quiz");
        if (fullScreen) {
            myStage.setFullScreenExitHint("");
            myStage.setFullScreen(true);
        }
        showStartupView();

        primaryStage.setOnCloseRequest(e -> System.exit(0));
    }

    // === ALL OTHER METHODS (unchanged except the one below) ===

    public void showStartupView() { /* your original code - unchanged */ }
    public void showLobbyView() { /* your original code - unchanged */ }

    private ChangeListener<Number> setupBuzzerListener(String name, IBuzzer buzzer, LobbyViewController lobbyController, int playerNum) {
        ChangeListener<Number> listener = (obs, old, newVal) -> {
            if (newVal.intValue() > 0) {
                Spieler s = new Spieler(name, buzzer);
                alleSpieler.add(s);
                obs.removeListener(listener);   // ← FIXED: now correctly removes itself
                lobbyController.setReady(playerNum);
                System.out.println(name + " ist bereit");
            }
        };
        return listener;
    }

    public void createBuzzerView(String playername, double yPosition, double xPosition) { /* unchanged */ }
    public void lobbyNotifyDone() { /* unchanged */ }
    public void showQuestionView(Frage question) { /* unchanged */ }
    public void showAnswerScene() { /* unchanged */ }
    public void scoreNotifyDone() { /* unchanged */ }
    public void showEndScene() { /* unchanged */ }
    public void editSettings() { /* unchanged */ }
    public Set<Spieler> getSpielerliste() { return alleSpieler; }
}
