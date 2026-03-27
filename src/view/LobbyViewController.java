package view;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import application.GameController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class LobbyViewController implements Initializable {

    @FXML private Label lblReady1, lblReady2, lblReady3;
    @FXML private Label lblPlayer1Name, lblPlayer2Name, lblPlayer3Name;
    @FXML private ImageView avatar1, avatar2, avatar3;

    // FIX: added fx:ids for the "Buzzer drücken" hint labels so they can be
    // hidden once a player has confirmed their identity by pressing the buzzer.
    @FXML private Label lblBuzzer1, lblBuzzer2, lblBuzzer3;

    private GameController gameController;

    private boolean player1Ready = false;
    private boolean player2Ready = false;
    private boolean player3Ready = false;

    public void setMainController(GameController gameController) {
        this.gameController = gameController;
    }

    /**
     * Called when a player presses their buzzer in the lobby.
     * Updates the ready status, the player name label, and hides the
     * "Buzzer drücken" hint for that player slot.
     */
    public void setReady(int playerNumber, String playerName) {
        Platform.runLater(() -> {
            Label readyLabel  = null;
            Label nameLabel   = null;
            Label buzzerLabel = null;
            boolean alreadyReady = false;

            switch (playerNumber) {
                case 1:
                    readyLabel   = lblReady1;
                    nameLabel    = lblPlayer1Name;
                    buzzerLabel  = lblBuzzer1;
                    alreadyReady = player1Ready;
                    player1Ready = true;
                    break;
                case 2:
                    readyLabel   = lblReady2;
                    nameLabel    = lblPlayer2Name;
                    buzzerLabel  = lblBuzzer2;
                    alreadyReady = player2Ready;
                    player2Ready = true;
                    break;
                case 3:
                    readyLabel   = lblReady3;
                    nameLabel    = lblPlayer3Name;
                    buzzerLabel  = lblBuzzer3;
                    alreadyReady = player3Ready;
                    player3Ready = true;
                    break;
            }

            if (readyLabel != null && !alreadyReady) {
                // Show ✓ BEREIT label
                readyLabel.setOpacity(1.0);
                String readyText = (playerName != null && !playerName.isEmpty())
                    ? "✓  " + playerName
                    : "✓  BEREIT";
                readyLabel.setText(readyText);
                readyLabel.setStyle("-fx-text-fill: #3fb950; " +
                                    "-fx-effect: dropshadow(gaussian, #3fb950, 20, 0.5, 0, 0); " +
                                    "-fx-font-weight: bold;");

                // Hide the "Buzzer drücken" hint for this player
                if (buzzerLabel != null) {
                    buzzerLabel.setVisible(false);
                    buzzerLabel.setManaged(false); // collapse space so layout stays clean
                }

                // Highlight the player name
                if (nameLabel != null && playerName != null && !playerName.isEmpty()) {
                    nameLabel.setText(playerName);
                    nameLabel.setStyle("-fx-text-fill: #58a6ff; -fx-font-size: 18pt; " +
                                      "-fx-font-weight: bold; -fx-letter-spacing: 2;");
                }
            }
        });
    }

    /** Overloaded method for backwards compatibility */
    public void setReady(int playerNumber) {
        setReady(playerNumber, null);
    }

    /**
     * Reset all ready states. Also restores the "Buzzer drücken" hints so the
     * lobby looks correct if players return to this screen.
     */
    public void resetReadyStates() {
        player1Ready = false;
        player2Ready = false;
        player3Ready = false;

        Platform.runLater(() -> {
            // Reset ready labels
            for (Label lbl : new Label[]{lblReady1, lblReady2, lblReady3}) {
                if (lbl != null) {
                    lbl.setOpacity(0.0);
                    lbl.setText("✓  BEREIT");
                }
            }

            // Restore "Buzzer drücken" hints
            for (Label lbl : new Label[]{lblBuzzer1, lblBuzzer2, lblBuzzer3}) {
                if (lbl != null) {
                    lbl.setVisible(true);
                    lbl.setManaged(true);
                }
            }

            // Reset player name labels to defaults
            String defaultStyle = "-fx-text-fill: #8b949e; -fx-font-size: 18pt; " +
                                  "-fx-font-weight: bold; -fx-letter-spacing: 2;";
            String[] defaults = {"SPIELER 1", "SPIELER 2", "SPIELER 3"};
            Label[] nameLabels = {lblPlayer1Name, lblPlayer2Name, lblPlayer3Name};
            for (int i = 0; i < 3; i++) {
                if (nameLabels[i] != null) {
                    nameLabels[i].setText(defaults[i]);
                    nameLabels[i].setStyle(defaultStyle);
                }
            }
        });
    }

    @FXML
    public void btnQuestionPressed(ActionEvent event) {
        if (gameController != null && gameController.getSpielerliste().size() >= 1) {
            gameController.lobbyNotifyDone();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (lblReady1 != null) lblReady1.setOpacity(0.0);
        if (lblReady2 != null) lblReady2.setOpacity(0.0);
        if (lblReady3 != null) lblReady3.setOpacity(0.0);

        loadAvatars();
    }

    private void loadAvatars() {
        try {
            File folder = new File("resources/images/avatars");
            if (folder.exists() && folder.isDirectory()) {
                String[] files = folder.list((dir, name) -> name.toLowerCase().endsWith(".png"));
                if (files != null && files.length >= 3) {
                    if (avatar1 != null) avatar1.setImage(new Image(new File(folder, files[0]).toURI().toString()));
                    if (avatar2 != null) avatar2.setImage(new Image(new File(folder, files[1]).toURI().toString()));
                    if (avatar3 != null) avatar3.setImage(new Image(new File(folder, files[2]).toURI().toString()));
                }
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Avatar-Laden: " + e.getMessage());
        }
    }

    @FXML
    public void btnSpieler1Pressed() {
        if (gameController != null) gameController.createBuzzerView("Spieler 1", 800, 400);
    }

    @FXML
    public void btnSpieler2Pressed() {
        if (gameController != null) gameController.createBuzzerView("Spieler 2", 800, 710);
    }

    @FXML
    public void btnSpieler3Pressed() {
        if (gameController != null) gameController.createBuzzerView("Spieler 3", 800, 1020);
    }
}
