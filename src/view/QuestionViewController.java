package view;

import application.*;
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

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class QuestionViewController implements Initializable {

    private GameController gameController;
    private Frage frage;

    @FXML private Label lblZeit;          // FIXED: matches FXML
    @FXML private Label lblFrage;
    @FXML private Label lblAntwort1;
    @FXML private Label lblAntwort2;
    @FXML private Label lblAntwort3;
    @FXML private BorderPane imageRoot;
    @FXML private ImageView image;

    private IntegerProperty restzeit;
    private Timer timer;
    private TimerTask timerTask;
    private long timeStart;
    private int maxZeit;
    private int answersReceived = 0;
    private Set<ChangeListener<Number>> answerListeners = new HashSet<>();

    public IntegerProperty getRestzeit() {
        if (restzeit == null) restzeit = new SimpleIntegerProperty(maxZeit);
        return restzeit;
    }

    public void setMainController(GameController mainController) {
        this.gameController = mainController;
    }

    public void initFrage(Frage frage, Set<Spieler> spielerliste, int maxZeit) {
        cleanup();
        this.frage = frage;
        this.maxZeit = maxZeit;
        this.timeStart = System.currentTimeMillis();
        this.answersReceived = 0;

        if (lblFrage != null) lblFrage.setText(frage.getFrage());
        setAnswers(frage.getAntworten());
        loadQuestionImage(frage.getImagePath());

        getRestzeit().setValue(maxZeit);
        startTimer();
        initPlayers(spielerliste);
    }

    private void initPlayers(Set<Spieler> spielerliste) {
        Set<Spieler> copy = new HashSet<>(spielerliste);
        copy.forEach(s -> {
            s.reset();
            s.setRundenpunkte(0);

            ChangeListener<Number> listener = (obs, old, nv) -> {
                handlePlayerAnswer(s, nv.intValue());
            };
            answerListeners.add(listener);
            s.getAntwortNr().addListener(listener);
        });
    }

    private void handlePlayerAnswer(Spieler spieler, int answerNum) {
        if (answerNum <= 0 || gameController == null) return;

        if (answerNum == frage.korrekteAntwortInt()) {
            int timeTaken = (int)(System.currentTimeMillis() - timeStart);
            int punkte = Math.max(0, (maxZeit * 1000 - timeTaken) / 100);
            spieler.addPunkte(punkte);
            spieler.setRundenpunkte(punkte);
        } else {
            spieler.setRundenpunkte(0);
        }

        answersReceived++;
        if (answersReceived >= gameController.getSpielerliste().size()) {
            Platform.runLater(this::endQuestion);
        }
    }

    private void startTimer() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - timeStart;
                int remaining = maxZeit - (int)(elapsed / 1000);
                Platform.runLater(() -> {
                    getRestzeit().setValue(Math.max(0, remaining));
                    if (lblZeit != null) lblZeit.setText(String.valueOf(getRestzeit().get()));
                    if (remaining <= 0) endQuestion();
                });
            }
        };
        timer = new Timer(true);
        timer.scheduleAtFixedRate(timerTask, 0, 100);
    }

    private void endQuestion() {
        cleanup();
        // listener in GameController will switch to AnswerView
    }

    public void cleanup() {
        if (timer != null) { timer.cancel(); timer = null; }
        if (timerTask != null) timerTask = null;
        answerListeners.clear();
    }

    // keep your original loadQuestionImage, setAnswers, setupMouseClickHandlers, initialize
    private void loadQuestionImage(String path) { /* your original code */ }
    private void setAnswers(List<Antwort> a) { /* your original code */ }
    private void setupMouseClickHandlers(MouseBuzzer mb) { /* your original code */ }

    @Override
    public void initialize(URL url, ResourceBundle rb) {}
}
