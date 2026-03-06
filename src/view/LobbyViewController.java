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

    private GameController gameController;
    
    // Track which players are actually ready
    private boolean player1Ready = false;
    private boolean player2Ready = false;
    private boolean player3Ready = false;

    public void setMainController(GameController gameController) {
        this.gameController = gameController;
    }

    /**
     * Called when a player presses their buzzer in the lobby.
     * Updates both the ready status AND the player name label.
     * @param playerNumber The player number (1, 2, or 3)
     * @param playerName The player's nickname from database
     */
    public void setReady(int playerNumber, String playerName) {
        Platform.runLater(() -> {
            Label readyLabel = null;
            Label nameLabel = null;
            boolean alreadyReady = false;
            
            switch (playerNumber) {
                case 1: 
                    readyLabel = lblReady1;
                    nameLabel = lblPlayer1Name;
                    alreadyReady = player1Ready;
                    player1Ready = true;
                    break;
                case 2: 
                    readyLabel = lblReady2;
                    nameLabel = lblPlayer2Name;
                    alreadyReady = player2Ready;
                    player2Ready = true;
                    break;
                case 3: 
                    readyLabel = lblReady3;
                    nameLabel = lblPlayer3Name;
                    alreadyReady = player3Ready;
                    player3Ready = true;
                    break;
            }
            
            if (readyLabel != null && !alreadyReady) {
                // Update the ready status label (bottom)
                readyLabel.setOpacity(1.0);
                String readyText = (playerName != null && !playerName.isEmpty()) 
                    ? "✓  " + playerName 
                    : "✓  BEREIT";
                readyLabel.setText(readyText);
                readyLabel.setStyle("-fx-text-fill: #3fb950; " +
                                     "-fx-effect: dropshadow(gaussian, #3fb950, 20, 0.5, 0, 0); " +
                                     "-fx-font-weight: bold;");
                
                // Update the player name label (top)
                if (nameLabel != null && playerName != null && !playerName.isEmpty()) {
                    nameLabel.setText(playerName);
                    // Highlight the name label with accent color
                    nameLabel.setStyle("-fx-text-fill: #58a6ff; -fx-font-size: 18pt; " +
                                      "-fx-font-weight: bold; -fx-letter-spacing: 2;");
                }
            }
        });
    }
    
    /**
     * Overloaded method for backwards compatibility
     */
    public void setReady(int playerNumber) {
        setReady(playerNumber, null);
    }
    
    /**
     * Reset all ready states - useful when returning to lobby
     * Also resets player name labels back to default
     */
    public void resetReadyStates() {
        player1Ready = false;
        player2Ready = false;
        player3Ready = false;
        
        Platform.runLater(() -> {
            // Reset ready labels
            if (lblReady1 != null) {
                lblReady1.setOpacity(0.0);
                lblReady1.setText("✓  BEREIT");
            }
            if (lblReady2 != null) {
                lblReady2.setOpacity(0.0);
                lblReady2.setText("✓  BEREIT");
            }
            if (lblReady3 != null) {
                lblReady3.setOpacity(0.0);
                lblReady3.setText("✓  BEREIT");
            }
            
            // Reset player name labels back to default
            if (lblPlayer1Name != null) {
                lblPlayer1Name.setText("SPIELER 1");
                lblPlayer1Name.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 18pt; " +
                                       "-fx-font-weight: bold; -fx-letter-spacing: 2;");
            }
            if (lblPlayer2Name != null) {
                lblPlayer2Name.setText("SPIELER 2");
                lblPlayer2Name.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 18pt; " +
                                       "-fx-font-weight: bold; -fx-letter-spacing: 2;");
            }
            if (lblPlayer3Name != null) {
                lblPlayer3Name.setText("SPIELER 3");
                lblPlayer3Name.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 18pt; " +
                                       "-fx-font-weight: bold; -fx-letter-spacing: 2;");
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
        // Ensure ready labels start hidden
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
                    if (avatar1 != null) {
                        avatar1.setImage(new Image(new File(folder, files[0]).toURI().toString()));
                    }
                    if (avatar2 != null) {
                        avatar2.setImage(new Image(new File(folder, files[1]).toURI().toString()));
                    }
                    if (avatar3 != null) {
                        avatar3.setImage(new Image(new File(folder, files[2]).toURI().toString()));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Avatar-Laden: " + e.getMessage());
        }
    }

    // Manual buttons (for dev purposes)
    @FXML
    public void btnSpieler1Pressed() { 
        if (gameController != null) {
            gameController.createBuzzerView("Spieler 1", 800, 400);
        }
    }
    
    @FXML
    public void btnSpieler2Pressed() { 
        if (gameController != null) {
            gameController.createBuzzerView("Spieler 2", 800, 710);
        }
    }
    
    @FXML
    public void btnSpieler3Pressed() { 
        if (gameController != null) {
            gameController.createBuzzerView("Spieler 3", 800, 1020);
        }
    }
}
