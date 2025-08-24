package view;

import java.net.URL;
import java.util.ResourceBundle;

import application.GameController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

public class StartupViewController implements Initializable{

	//application getter setter
	GameController gameController;
	
	@FXML Button btnPlay;

	@FXML ImageView wissHome;
	@FXML BorderPane imageRoot;
	
	@FXML
	public void btnLobbyPressed(ActionEvent event) {
		gameController.showLobbyView();
		//gameController.spielrundeStarten();
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
		if (wissHome.getImage() != null) {
			wissHome.fitWidthProperty().bind(imageRoot.widthProperty());
    		//wissHome.fitHeightProperty().bind(imageRoot.heightProperty());
		}
	}
	
}
