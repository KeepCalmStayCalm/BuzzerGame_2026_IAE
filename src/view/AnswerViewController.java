package view;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import application.Frage;
import application.Spieler;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

/**
 * Controls the answer / intermediate-score screen.
 *
 * Displays the correct answer and per-player scores for 10 seconds, then
 * auto-advances to the next question (or end screen) by setting restzeit to 0.
 * Pressing SPACE skips the countdown immediately.
 */
public class AnswerViewController {

    @FXML private Label lblAntwort;

    @FXML private Label lblS1Name;
    @FXML private Label lblS1PunkteDazu;
    @FXML private Label lblS1PunkteGesamt;

    @FXML private Label lblS2Name;
    @FXML private Label lblS2PunkteDazu;
    @FXML private Label lblS2PunkteGesamt;

    @FXML private Label lblS3Name;
    @FXML private Label lblS3PunkteDazu;
    @FXML private Label lblS3PunkteGesamt;

    private static final int DISPLAY_SECONDS = 10;
    private final IntegerProperty restzeit = new SimpleIntegerProperty(DISPLAY_SECONDS);
    private Timeline countdown;


    /**
     * Populates the score table and correct-answer label, then starts the
     * 10-second auto-advance countdown.
     */
    public void setInformation(Frage frage, Set<Spieler> spieler) {
        // Show the correct answer
        lblAntwort.setText(frage.korrekteAntwort() != null
            ? frage.korrekteAntwort()
            : "–");

        // Sort players by total score descending for display
        List<Spieler> sorted = new ArrayList<>(spieler);
        sorted.sort(Comparator.comparingInt(s -> -s.getPunktestand().get()));

        Label[] names   = {lblS1Name,   lblS2Name,   lblS3Name};
        Label[] dazu    = {lblS1PunkteDazu,  lblS2PunkteDazu,  lblS3PunkteDazu};
        Label[] gesamt  = {lblS1PunkteGesamt, lblS2PunkteGesamt, lblS3PunkteGesamt};

        for (int i = 0; i < 3; i++) {
            if (i < sorted.size()) {
                Spieler s = sorted.get(i);
                names[i].setText(s.getName());
                int runden = s.getRundenpunkte();
                dazu[i].setText(runden > 0 ? "+" + runden : "–");
                gesamt[i].setText(String.valueOf(s.getPunktestand().get()));
            } else {
                names[i].setText("–");
                dazu[i].setText("–");
                gesamt[i].setText("–");
            }
        }

        startCountdown();
        enableSpacebarSkip();
    }

    /** Called by GameController to observe when the display time ends. */
    public IntegerProperty getRestzeit() {
        return restzeit;
    }


    private void startCountdown() {
        restzeit.set(DISPLAY_SECONDS);
        countdown = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> {
                int current = restzeit.get() - 1;
                restzeit.set(current);
                if (current <= 0 && countdown != null) {
                    countdown.stop();
                }
            })
        );
        countdown.setCycleCount(DISPLAY_SECONDS);
        countdown.play();
    }

    /** Allow pressing SPACE to skip the display timer immediately. */
    private void enableSpacebarSkip() {
        // The scene may not be set yet at this point; attach on next pulse
        javafx.application.Platform.runLater(() -> {
            if (lblAntwort.getScene() != null) {
                lblAntwort.getScene().setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.SPACE) {
                        if (countdown != null) countdown.stop();
                        restzeit.set(0);
                    }
                });
            }
        });
    }
}
