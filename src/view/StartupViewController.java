package view;

import java.net.URL;
import java.util.ResourceBundle;
import application.GameController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

public class StartupViewController implements Initializable {

    private GameController gameController;
    
    @FXML private Button btnPlay;
    @FXML private ImageView wissHome;
    @FXML private AnchorPane centerPane;
    
    /**
     * Navigate to lobby view
     */
    @FXML
    public void btnLobbyPressed(ActionEvent event) {
        if (gameController != null) {
            gameController.showLobbyView();
        }
    }
    
    /**
     * Open settings window
     */
    @FXML 
    public void btnSettingsPressed() {
        if (gameController != null) {
            gameController.editSettings();
        }
    }
    
    /**
     * Set the main game controller
     */
    public void setMainController(GameController gameController) {
        this.gameController = gameController;
        
        // Set focus to play button for keyboard navigation
        if (btnPlay != null) {
            btnPlay.requestFocus();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Bind image to container size
        if (wissHome != null && centerPane != null) {
            wissHome.fitWidthProperty().bind(centerPane.widthProperty());
            wissHome.fitHeightProperty().bind(centerPane.heightProperty());
            wissHome.setPreserveRatio(true);
        }
    }
}
