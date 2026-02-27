package view;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import application.GameController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class LobbyViewController implements Initializable {

    // Passend zu den fx:id Attributen in der LobbyView.fxml
    @FXML private Label lblReady1;
    @FXML private Label lblReady2;
    @FXML private Label lblReady3;

    @FXML private ImageView avatar1;
    @FXML private ImageView avatar2;
    @FXML private ImageView avatar3;

    private GameController gameController;

    public void setMainController(GameController gameController) {
        this.gameController = gameController;
    }

    /**
     * Wird vom GameController aufgerufen, wenn ein Hardware-Buzzer 
     * oder ein Software-Buzzer gedrückt wurde.
     */
    public void setReady(int playerNumber) {
        Platform.runLater(() -> {
            switch (playerNumber) {
                case 1: showReadyState(lblReady1); break;
                case 2: showReadyState(lblReady2); break;
                case 3: showReadyState(lblReady3); break;
            }
        });
    }

    private void showReadyState(Label lbl) {
        if (lbl != null) {
            lbl.setVisible(true);
            lbl.setText("BEREIT");
            // Ein schöneres Grün und etwas Styling direkt im Code (optional zum CSS)
            lbl.setStyle("-fx-text-fill: #28a745; " +
                         "-fx-font-weight: bold; " +
                         "-fx-background-color: rgba(255,255,255,0.9); " +
                         "-fx-padding: 10 20; " +
                         "-fx-background-radius: 15; " +
                         "-fx-effect: dropshadow(three-pass-box, rgba(0,255,0,0.4), 10, 0, 0, 0);");
        }
    }

    @FXML
    public void btnQuestionPressed(ActionEvent event) {
        // Validierung: Mindestens ein Spieler sollte da sein
        if (gameController.getSpielerliste().size() >= 1) {
            gameController.lobbyNotifyDone();
        } else {
            System.out.println("Noch keine Spieler registriert!");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadAvatars();
    }

    private void loadAvatars() {
        try {
            File avatarFolder = new File("resources/images/avatars");
            if (avatarFolder.exists() && avatarFolder.isDirectory()) {
                String[] avatars = avatarFolder.list((dir, name) -> 
                    name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg")
                );

                if (avatars != null && avatars.length >= 3) {
                    // Laden der Bilder mit URL-Sicherheit
                    avatar1.setImage(new Image(new File(avatarFolder, avatars[0]).toURI().toString()));
                    avatar2.setImage(new Image(new File(avatarFolder, avatars[1]).toURI().toString()));
                    avatar3.setImage(new Image(new File(avatarFolder, avatars[2]).toURI().toString()));
                    System.out.println("Lobby: 3 Avatare erfolgreich geladen.");
                } else {
                    System.out.println("Lobby: Zu wenige Avatare in resources/images/avatars gefunden.");
                }
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Laden der Avatare: " + e.getMessage());
        }
    }

    // Hilfsmethoden für manuelle Spieler-Registrierung (falls benötigt)
    public void btnSpieler1Pressed() { gameController.createBuzzerView("Spieler 1", 800, 400); }
    public void btnSpieler2Pressed() { gameController.createBuzzerView("Spieler 2", 800, 710); }
    public void btnSpieler3Pressed() { gameController.createBuzzerView("Spieler 3", 800, 1020); }
}
