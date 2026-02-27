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

    GameController gameController;
    
    @FXML Button btnPlay;
    @FXML ImageView wissHome;
    @FXML AnchorPane centerPane;
    
    @FXML
    public void btnLobbyPressed(ActionEvent event) {
        gameController.showLobbyView();
    }
    
    @FXML public void btnSettingsPressed() {
        gameController.editSettings();
    }
    
    public void setMainController(GameController gameController) {
        this.gameController = gameController;
        btnPlay.requestFocus();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (wissHome != null && centerPane != null) {
            // Ensure the background stays within the center bounds
            wissHome.fitWidthProperty().bind(centerPane.widthProperty());
            wissHome.fitHeightProperty().bind(centerPane.heightProperty());
            wissHome.setPreserveRatio(true);
        }
    }
}
