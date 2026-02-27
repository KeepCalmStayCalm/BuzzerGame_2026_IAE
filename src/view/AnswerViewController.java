package view;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import application.Frage;
import application.Spieler;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;

public class AnswerViewController {

    @FXML Label lblS1Name;
    @FXML Label lblS2Name;
    @FXML Label lblS3Name;
    @FXML Label lblS1PunkteZuvor;
    @FXML Label lblS2PunkteZuvor;
    @FXML Label lblS3PunkteZuvor;
    @FXML Label lblS1PunkteDazu;
    @FXML Label lblS2PunkteDazu;
    @FXML Label lblS3PunkteDazu;
    @FXML Label lblS1PunkteGesamt;
    @FXML Label lblS2PunkteGesamt;
    @FXML Label lblS3PunkteGesamt;
    @FXML HBox hBoxS3;
    @FXML Label lblAntwort;

    private IntegerProperty restzeit;
    private static final int TIMEOUT = 15;
    private Timer timer;
    private TimerTask timerTask;
    private EventHandler<KeyEvent> keyHandler;

    public IntegerProperty getRestzeit() {
        if (restzeit == null) {
            restzeit = new SimpleIntegerProperty(TIMEOUT);
        }
        return restzeit;
    }

    /**
     * Set player information and start countdown timer
     */
    public void setInformation(Frage frage, Set<Spieler> spielerSet) {
        // Cancel any existing timer first
        cleanup();

        // Display correct answer
        if (lblAntwort != null && frage != null) {
            lblAntwort.setText(frage.getFrage() + ": " + 
                             frage.getAntworten().get(frage.korrekteAntwortInt() - 1).getAntwort());
        }

        // Sort players by score
        List<Spieler> spielerliste = spielerSet.stream()
                                              .collect(Collectors.toList());
        Collections.sort(spielerliste, new Comparator<Spieler>() {
            @Override
            public int compare(Spieler s1, Spieler s2) {
                return s2.getPunktestand().get() - s1.getPunktestand().get();
            }
        });

        // Display player 1 (highest score)
        if (spielerliste.size() >= 1) {
            updatePlayerDisplay(1, spielerliste.get(0));
        }

        // Display player 2
        if (spielerliste.size() >= 2) {
            updatePlayerDisplay(2, spielerliste.get(1));
        }

        // Display player 3 or hide if only 2 players
        if (spielerliste.size() >= 3) {
            updatePlayerDisplay(3, spielerliste.get(2));
        } else {
            clearPlayerDisplay(3);
        }

        // Setup keyboard handler for SPACE to skip
        setupKeyHandler();

        // Start countdown timer
        startTimer();
    }

    /**
     * Update display for a specific player
     */
    private void updatePlayerDisplay(int playerNum, Spieler spieler) {
        Label lblName = getPlayerNameLabel(playerNum);
        Label lblPunkteDazu = getPlayerPunkteDazuLabel(playerNum);
        Label lblPunkteGesamt = getPlayerPunkteGesamtLabel(playerNum);

        if (lblName != null) {
            // Add player-specific CSS class
            String cssClass = spieler.getName().toString().toLowerCase().replace(" ", "");
            lblName.getStyleClass().add(cssClass);
            lblName.setText(spieler.getName().toString());
        }

        if (lblPunkteGesamt != null) {
            lblPunkteGesamt.setText(spieler.getPunktestand().getValue().toString());
        }

        if (lblPunkteDazu != null) {
            lblPunkteDazu.setText(String.valueOf(spieler.getRundenpunkte()));
        }
    }

    /**
     * Clear display for a player slot
     */
    private void clearPlayerDisplay(int playerNum) {
        Label lblName = getPlayerNameLabel(playerNum);
        Label lblPunkteDazu = getPlayerPunkteDazuLabel(playerNum);
        Label lblPunkteGesamt = getPlayerPunkteGesamtLabel(playerNum);

        if (lblName != null) lblName.setText("");
        if (lblPunkteDazu != null) lblPunkteDazu.setText("");
        if (lblPunkteGesamt != null) lblPunkteGesamt.setText("");
    }

    /**
     * Setup keyboard event handler
     */
    private void setupKeyHandler() {
        if (lblAntwort != null && lblAntwort.getScene() != null) {
            keyHandler = new EventHandler<KeyEvent>() {
                public void handle(KeyEvent ke) {
                    if (ke.getCode() == KeyCode.SPACE) {
                        System.out.println("SPACE pressed - skipping timer");
                        skipToEnd();
                        ke.consume();
                    }
                }
            };
            lblAntwort.getScene().addEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
        }
    }

    /**
     * Start the countdown timer
     */
    private void startTimer() {
        getRestzeit().setValue(TIMEOUT);
        
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    int currentTime = getRestzeit().get() - 1;
                    getRestzeit().set(currentTime);
                    
                    if (currentTime <= 0) {
                        cleanup();
                    }
                });
            }
        };
        
        timer = new Timer(true); // daemon thread
        timer.scheduleAtFixedRate(timerTask, 1000, 1000);
    }

    /**
     * Skip to end of timer
     */
    private void skipToEnd() {
        getRestzeit().setValue(0);
        cleanup();
    }

    /**
     * Clean up resources (timer, event handlers)
     */
    public void cleanup() {
        // Cancel timer
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }

        // Remove keyboard handler
        if (keyHandler != null && lblAntwort != null && lblAntwort.getScene() != null) {
            lblAntwort.getScene().removeEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
            keyHandler = null;
        }
    }

    // Helper methods to get labels
    private Label getPlayerNameLabel(int playerNum) {
        switch (playerNum) {
            case 1: return lblS1Name;
            case 2: return lblS2Name;
            case 3: return lblS3Name;
            default: return null;
        }
    }

    private Label getPlayerPunkteDazuLabel(int playerNum) {
        switch (playerNum) {
            case 1: return lblS1PunkteDazu;
            case 2: return lblS2PunkteDazu;
            case 3: return lblS3PunkteDazu;
            default: return null;
        }
    }

    private Label getPlayerPunkteGesamtLabel(int playerNum) {
        switch (playerNum) {
            case 1: return lblS1PunkteGesamt;
            case 2: return lblS2PunkteGesamt;
            case 3: return lblS3PunkteGesamt;
            default: return null;
        }
    }
}
