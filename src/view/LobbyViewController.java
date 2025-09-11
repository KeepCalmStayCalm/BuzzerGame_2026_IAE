package view;



import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import application.GameController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class LobbyViewController implements Initializable{
	
	
	@FXML
	Label lblS1, lblS2, lblS3, lblS1Ready, lblS2Ready, lblS3Ready;
	
	@FXML
	ImageView avatar1;
	
	@FXML
	ImageView avatar2;
	
	@FXML
	ImageView avatar3;
	
	
	@FXML
	Button btnSpieler1, btnSpieler2, btnSpieler3;
	
	GameController gameController;
	
	public void setMainController(GameController gameController) {
		this.gameController = gameController;
	}
	
	public void btnSpieler1Pressed() {
		gameController.createBuzzerView("Spieler 1", 800, 400);
	}
	
	public void btnSpieler2Pressed() {
		gameController.createBuzzerView("Spieler 2", 800, 710);
	}
	
	public void btnSpieler3Pressed() {
		gameController.createBuzzerView("Spieler 3", 800, 1020);
	}
	
	@FXML
	public void btnQuestionPressed(ActionEvent event) {	
		if(gameController.getSpielerliste().size() > 1) {
			gameController.lobbyNotifyDone();		
		}
	}
	
	
	public void setReady(int playerNumber){
		switch (playerNumber) {
			case 1: setReady(lblS1Ready); break;
			case 2: setReady(lblS2Ready); break;
			case 3: setReady(lblS3Ready); break;	
		}
	}
	
	private void setReady(Label lbl){
		Platform.runLater(new Runnable() {
            @Override public void run() {
				lbl.setText("Ready");
				lbl.setStyle("-fx-border-color: #c10a27");
				lbl.setStyle("-fx-text-fill: black");
			}
		});
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		String[] avatars = new File("resources/images/avatars").list();
		System.out.println("Avatars fetched: " + avatars.length);
		avatar1.setImage(new ImageView(new File("resources/images/avatars/" + avatars[0]).toURI().toString()).getImage());
		avatar2.setImage(new ImageView(new File("resources/images/avatars/" + avatars[1]).toURI().toString()).getImage());
		avatar3.setImage(new ImageView(new File("resources/images/avatars/" + avatars[2]).toURI().toString()).getImage());

	}
}
