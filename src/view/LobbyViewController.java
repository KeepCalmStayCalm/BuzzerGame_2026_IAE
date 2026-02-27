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

    public void setMainController(GameController gameController) {
        this.gameController = gameController;
    }

    // Hardware/Buzzer-Methode
    public void setReady(int playerNumber) {
        Platform.runLater(() -> {
            Label targetLabel = null;
            switch (playerNumber) {
                case 1: targetLabel = lblReady1; break;
                case 2: targetLabel = lblReady2; break;
                case 3: targetLabel = lblReady3; break;
            }
            if (targetLabel != null) {
                targetLabel.setOpacity(1.0);
                targetLabel.setText("BEREIT");
                // Grüner Glow-Effekt
                targetLabel.setStyle("-fx-text-fill: #28a745; " +
                                     "-fx-effect: dropshadow(gaussian, #28a745, 20, 0.5, 0, 0); " +
                                     "-fx-font-weight: bold;");
            }
        });
    }

    @FXML
    public void btnQuestionPressed(ActionEvent event) {
        if (gameController.getSpielerliste().size() >= 1) {
            gameController.lobbyNotifyDone();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadAvatars();
    }

    private void loadAvatars() {
        try {
            File folder = new File("resources/images/avatars");
            if (folder.exists()) {
                String[] files = folder.list((dir, name) -> name.toLowerCase().endsWith(".png"));
                if (files != null && files.length >= 3) {
                    avatar1.setImage(new Image(new File(folder, files[0]).toURI().toString()));
                    avatar2.setImage(new Image(new File(folder, files[1]).toURI().toString()));
                    avatar3.setImage(new Image(new File(folder, files[2]).toURI().toString()));
                }
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Avatar-Laden: " + e.getMessage());
        }
    }

    // Manuelle Buttons (für Dev-Zwecke)
    public void btnSpieler1Pressed() { gameController.createBuzzerView("Spieler 1", 800, 400); }
    public void btnSpieler2Pressed() { gameController.createBuzzerView("Spieler 2", 800, 710); }
    public void btnSpieler3Pressed() { gameController.createBuzzerView("Spieler 3", 800, 1020); }
}
