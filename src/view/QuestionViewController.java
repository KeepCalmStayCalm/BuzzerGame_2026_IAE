package view;

import java.util.Set;

import application.Frage;
import application.GameController;
import application.Spieler;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class QuestionViewController {

    @FXML private Label lblFrage;
    @FXML private Label lblAntwort1;
    @FXML private Label lblAntwort2;
    @FXML private Label lblAntwort3;
    @FXML private Label lblZeit;

    private application.GameController mainController;

    public void setMainController(application.GameController mc) {
        this.mainController = mc;
    }

    private final IntegerProperty restzeit = new SimpleIntegerProperty(0);
    private Timeline countdown;

    private int totalPlayers;
    private int answeredPlayers;
    private int maxZeit;
    private Set<Spieler> spielerSet;

    private java.util.Map<Spieler, ChangeListener<Number>> playerListeners = new java.util.HashMap<>();

    public void initFrage(Frage frage, Set<Spieler> spieler, int maxZeit) {
        cleanup();

        this.spielerSet      = spieler;
        this.totalPlayers    = spieler.size();
        this.answeredPlayers = 0;
        this.maxZeit         = maxZeit;

        System.out.println(">>> Initializing " + totalPlayers + " players for new question");

        lblFrage.setText(frage.getFrage());
        var antworten = frage.getAntworten();
        lblAntwort1.setText(antworten.size() > 0 ? antworten.get(0).getAntwort() : "");
        lblAntwort2.setText(antworten.size() > 1 ? antworten.get(1).getAntwort() : "");
        lblAntwort3.setText(antworten.size() > 2 ? antworten.get(2).getAntwort() : "");

        restzeit.set(maxZeit);
        lblZeit.setText(String.valueOf(maxZeit));

        for (Spieler s : spieler) {
            s.reset();
            ChangeListener<Number> listener = (obs, oldVal, newVal) -> {
                int answerNr = newVal.intValue();
                if (answerNr > 0) {
                    handlePlayerAnswer(s, answerNr, frage);
                }
            };
            playerListeners.put(s, listener);
            s.getAntwortNr().addListener(listener);
            System.out.println("  → Player " + s.getName() + " ready");
        }

        countdown = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> {
                int current = restzeit.get() - 1;
                restzeit.set(current);
                lblZeit.setText(String.valueOf(Math.max(current, 0)));

                if (current <= 0) {
                    detachPlayerListeners();
                    if (countdown != null) countdown.stop();
                }
            })
        );
        countdown.setCycleCount(maxZeit);
        countdown.play();
    }

    public IntegerProperty getRestzeit() {
        return restzeit;
    }

    private synchronized void handlePlayerAnswer(Spieler spieler, int answerNr, Frage frage) {
        if (!playerListeners.containsKey(spieler)) return;

        System.out.println(">>> handlePlayerAnswer: " + spieler.getName() + " → " + answerNr);

        spieler.getAntwortNr().removeListener(playerListeners.remove(spieler));

        boolean correct = (answerNr == frage.korrekteAntwortInt());
        if (correct) {
            // Max score is 100 (when answered immediately, restzeit == maxZeit).
            // Scales linearly down to a minimum of 10 for very late answers.
            int points = Math.max(10, restzeit.get() * 100 / (maxZeit > 0 ? maxZeit : 10));
            spieler.addPunkte(points);
            spieler.setRundenpunkte(points);
            System.out.println(spieler.getName() + " richtig! Punkte: " + points);
        } else {
            spieler.setRundenpunkte(0);
            System.out.println(spieler.getName() + " falsch.");
        }

        answeredPlayers++;
        System.out.println("Antworten: " + answeredPlayers + "/" + totalPlayers);

        if (answeredPlayers >= totalPlayers) {
            System.out.println("Alle Spieler haben geantwortet – Frage beenden");
            cleanup();
        }
    }

    private void cleanup() {
        System.out.println(">>> QuestionViewController cleanup...");

        if (countdown != null) {
            countdown.stop();
            countdown = null;
        }

        detachPlayerListeners();

        System.out.println(">>> cleanup complete");

        // Setting restzeit to 0 fires the GameController listener and triggers
        // the transition to the answer screen.
        restzeit.set(0);
    }

    private void detachPlayerListeners() {
        if (spielerSet == null) return;
        for (Spieler s : spielerSet) {
            ChangeListener<Number> l = playerListeners.remove(s);
            if (l != null) s.getAntwortNr().removeListener(l);
        }
    }
}
