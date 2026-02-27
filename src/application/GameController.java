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

import view.*;

public class GameController extends Application {

    public static final boolean IS_DEV_MODE = false;

    private Stage myStage;
    private StartupViewController startupController;
    private int rundenCounter;
    private List<Frage> eingeleseneFragen;
    private Spielrunde spielrunde;
    private Set<Spieler> alleSpieler = new HashSet<>();
    private IBuzzer buzzer1, buzzer2, buzzer3;

    private int MAX_ZEIT;
    private int MAX_FRAGEN;
    private Frage aktuelleFrage;
    private boolean shuffleQuestions;
    private boolean fullScreen;

    private Preferences prefs;
    private String style;
    private Context pi4j;
    private String questionFile;

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

    @Override
    public void stop() {
        if (pi4j != null) pi4j.shutdown();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        readPreferences();
        style = getClass().getResource("buzzerStyle2025.css").toExternalForm();
        eingeleseneFragen = EinAuslesenFragen.einlesenFragen(questionFile);

        myStage = primaryStage;
        myStage.setTitle("IAE Buzzer Quiz");
        if (fullScreen) {
            myStage.setFullScreenExitHint("");
            myStage.setFullScreen(true);
        }
        showStartupView();

        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
    }

    public void showStartupView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/StartupView.fxml"));
            Scene scene = new Scene(loader.load(), 1920, 1080);
            scene.getStylesheets().add(style);
            startupController = loader.getController();
            startupController.setMainController(this);
            myStage.setScene(scene);
            if (fullScreen) myStage.setFullScreen(true);
            myStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showLobbyView() {
        alleSpieler.clear();
        if (!IS_DEV_MODE) {
            pi4j = Pi4J.newAutoContext();
            buzzer1 = new RaspiBuzzer(pi4j, 16, 20, 21);
            buzzer2 = new RaspiBuzzer(pi4j, 22, 27, 17);
            buzzer3 = new RaspiBuzzer(pi4j, 13, 19, 26);
        } else {
            buzzer1 = new MouseBuzzer();
            buzzer2 = new DummyBuzzer(2);
            buzzer3 = new DummyBuzzer(3);
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LobbyView.fxml"));
            Scene lobbyScene = new Scene(loader.load(), 1920, 1080);
            lobbyScene.getStylesheets().add(style);

            LobbyViewController lobbyController = loader.getController();
            lobbyController.setMainController(this);
            lobbyController.resetReadyStates();   // clean start

            List<IBuzzer> buzzers = List.of(buzzer1, buzzer2, buzzer3);
            String[] names = {"Spieler 1", "Spieler 2", "Spieler 3"};

            for (int i = 0; i < 3; i++) {
                final int playerNum = i + 1;
                final IBuzzer b = buzzers.get(i);
                final String name = names[i];

                if (IS_DEV_MODE) {
                    Spieler s = new Spieler(name, b);
                    alleSpieler.add(s);
                    lobbyController.setReady(playerNum);
                } else {
                    b.getAnswer().addListener(setupBuzzerListener(name, b, lobbyController, playerNum));
                }
            }

            if (shuffleQuestions) Collections.shuffle(eingeleseneFragen);
            spielrunde = new Spielrunde(eingeleseneFragen.subList(0, Math.min(MAX_FRAGEN, eingeleseneFragen.size())));

            myStage.setScene(lobbyScene);
            if (fullScreen) myStage.setFullScreen(true);
            myStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ChangeListener<Number> setupBuzzerListener(String name, IBuzzer buzzer, LobbyViewController lobbyController, int playerNum) {
        return (obs, old, newVal) -> {
            if (newVal.intValue() > 0) {
                Spieler s = new Spieler(name, buzzer);
                alleSpieler.add(s);
                buzzer.getAnswer().removeListener(this);
                lobbyController.setReady(playerNum);
                System.out.println(name + " ist bereit");
            }
        };
    }

    public void lobbyNotifyDone() {
        if (alleSpieler.size() >= 2) {
            rundenCounter = 0;
            aktuelleFrage = spielrunde.naechsteFrage();
            showQuestionView(aktuelleFrage);
        } else {
            new Alert(Alert.AlertType.WARNING, "Mindestens zwei Spieler benötigt!", ButtonType.OK).showAndWait();
        }
    }

    public void showQuestionView(Frage question) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/QuestionView2025.fxml"));
            Scene scene = new Scene(loader.load(), 1920, 1080);
            scene.getStylesheets().add(style);

            QuestionViewController qController = loader.getController();
            qController.setMainController(this);          // CRITICAL FIX
            qController.initFrage(question, alleSpieler, MAX_ZEIT);

            qController.getRestzeit().addListener(showAnswerSceneListener);

            myStage.setScene(scene);
            if (fullScreen) myStage.setFullScreen(true);
            myStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final ChangeListener<Number> showAnswerSceneListener = (o, a, newValue) -> {
        if (newValue.intValue() <= 0) {
            o.removeListener(this.showAnswerSceneListener);
            Platform.runLater(this::showAnswerScene);
        }
    };

    // keep ALL your other methods unchanged (showAnswerScene, showEndScene, scoreNotifyDone, editSettings, createBuzzerView, getSpielerliste, etc.)
    // ... (copy-paste the rest from your original file)

    public Set<Spieler> getSpielerliste() {
        return alleSpieler;
    }
}
