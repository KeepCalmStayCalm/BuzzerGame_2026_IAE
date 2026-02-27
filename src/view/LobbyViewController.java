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
    @FXML private ImageView avatar1, avatar2, avatar3;

    private GameController gameController;
    
    // Track which players are ready (1, 2 or 3)
    private boolean player1Ready = false;
    private boolean player2Ready = false;
    private boolean player3Ready = false;

    public void setMainController(GameController gameController) {
        this.gameController = gameController;
    }

    /**
     * Called when a player presses their buzzer.
     * Only marks them ready if they weren't already ready.
     * playerNumber must be 1, 2 or 3
     */
    public void setReady(int playerNumber) {
        if (playerNumber < 1 || playerNumber > 3) return;   // safety

        Platform.runLater(() -> {
            Label targetLabel = null;
            boolean alreadyReady = false;
            
            switch (playerNumber) {
                case 1: 
                    targetLabel = lblReady1;
                    alreadyReady = player1Ready;
                    player1Ready = true;
                    break;
                case 2: 
                    targetLabel = lblReady2;
                    alreadyReady = player2Ready;
                    player2Ready = true;
                    break;
                case 3: 
                    targetLabel = lblReady3;
                    alreadyReady = player3Ready;
                    player3Ready = true;
                    break;
            }
            
            if (targetLabel != null && !alreadyReady) {
                targetLabel.setOpacity(1.0);
                targetLabel.setText("✓  BEREIT");
                targetLabel.setStyle("-fx-text-fill: #3fb950; " +
                                     "-fx-effect: dropshadow(gaussian, #3fb950, 20, 0.5, 0, 0); " +
                                     "-fx-font-weight: bold;");
            }
        });
    }
    
    /**
     * Reset all ready states when entering the lobby
     */
    public void resetReadyStates() {
        player1Ready = false;
        player2Ready = false;
        player3Ready = false;
        
        Platform.runLater(() -> {
            resetLabel(lblReady1);
            resetLabel(lblReady2);
            resetLabel(lblReady3);
        });
    }
    
    private void resetLabel(Label label) {
        if (label != null) {
            label.setOpacity(0.0);
            label.setText("✓  BEREIT");
            label.setStyle("");
        }
    }

    @FXML
    public void btnQuestionPressed(ActionEvent event) {
        if (gameController != null) {
            gameController.lobbyNotifyDone();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Make sure ready labels start hidden
        resetReadyStates();
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

    // === Manual dev buttons (only used in development) ===
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
