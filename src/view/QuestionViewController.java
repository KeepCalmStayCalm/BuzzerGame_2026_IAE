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

    @FXML private Label lblZeit;          // must match QuestionView2025.fxml
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

    public IntegerProperty getRestzeit() {
        if (restzeit == null) {
            restzeit = new SimpleIntegerProperty(maxZeit);
        }
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
        copy.forEach(spieler -> {
            spieler.reset();
            spieler.setRundenpunkte(0);

            // Robust listener with direct reference
            ChangeListener<Number> listener = (obs, oldVal, newVal) -> {
                if (newVal != null) {
                    handlePlayerAnswer(spieler, newVal.intValue());
                }
            };
            spieler.getAntwortNr().addListener(listener);
        });
    }

    private void handlePlayerAnswer(Spieler spieler, int answerNum) {
        System.out.println(">>> handlePlayerAnswer called for " + spieler.getName() + " with answer " + answerNum);

        if (answerNum <= 0 || gameController == null) return;

        if (answerNum == frage.korrekteAntwortInt()) {
            long timeTaken = System.currentTimeMillis() - timeStart;
            int punkte = Math.max(0, (maxZeit * 1000 - (int)timeTaken) / 100);
            spieler.addPunkte(punkte);
            spieler.setRundenpunkte(punkte);
            System.out.println(spieler.getName() + " answered correctly! Points: " + punkte);
        } else {
            spieler.setRundenpunkte(0);
            System.out.println(spieler.getName() + " answered incorrectly.");
        }

        answersReceived++;

        // If all players answered → end immediately
        if (answersReceived >= gameController.getSpielerliste().size()) {
            System.out.println("All players answered - forcing end of question");
            endQuestionImmediately();
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

                    if (remaining <= 0) {
                        endQuestionImmediately();
                    }
                });
            }
        };

        timer = new Timer(true);
        timer.scheduleAtFixedRate(timerTask, 0, 100);
    }

    private void endQuestionImmediately() {
        cleanup();
        if (gameController != null) {
            // Force the restzeit to 0 → this triggers showAnswerSceneListener in GameController
            getRestzeit().setValue(0);
        }
    }

    public void cleanup() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) timerTask = null;
    }

    private void loadQuestionImage(String imagePath) {
        if (image != null && imagePath != null && !imagePath.isEmpty()) {
            try (InputStream is = new FileInputStream(imagePath)) {
                Image img = new Image(is);
                image.setImage(img);
                if (imageRoot != null) {
                    image.fitWidthProperty().bind(imageRoot.widthProperty());
                    image.fitHeightProperty().bind(imageRoot.heightProperty());
                    image.setPreserveRatio(true);
                }
            } catch (Exception e) {
                System.err.println("Bild konnte nicht geladen werden: " + e.getMessage());
            }
        }
    }

    private void setAnswers(List<Antwort> antworten) {
        if (antworten == null || antworten.size() < 3) return;
        if (lblAntwort1 != null) lblAntwort1.setText(antworten.get(0).getAntwort());
        if (lblAntwort2 != null) lblAntwort2.setText(antworten.get(1).getAntwort());
        if (lblAntwort3 != null) lblAntwort3.setText(antworten.get(2).getAntwort());
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {}
}
