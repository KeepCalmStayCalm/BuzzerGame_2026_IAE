package view;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import application.GameController;
import application.Spieler;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class EndViewController{


	GameController gameController;

	@FXML
	Label lblS1Name;
	@FXML
	Label lblS2Name;
	@FXML
	Label lblS3Name;
	@FXML
	Label lblS1PunkteGesamt;
	@FXML
	Label lblS2PunkteGesamt;
	@FXML
	Label lblS3PunkteGesamt;

	@FXML AnchorPane rectPlatz1;
	@FXML AnchorPane rectPlatz2;
	@FXML AnchorPane rectPlatz3;

	public void setMainController(GameController mainController) {
		this.gameController = mainController;
	}

	public void endGameRound(){
		gameController.showStartupView();
		//Platform.exit();
	}

	public void setSpielerInformation(Set<Spieler> spielerSet) {
		List<Spieler> spielerliste = spielerSet.stream().collect(Collectors.toList());
		Collections.sort(spielerliste, new Comparator<Spieler>() {

			@Override
			public int compare(Spieler arg0, Spieler arg1) {
				return arg1.getPunktestand().get() - arg0.getPunktestand().get();
			}
		});

		Spieler currentSpieler = spielerliste.get(0);
		String platz1Style = currentSpieler.getName().toString().toLowerCase().replace(" ","");
		//lblS1Name.getStyleClass().add(platz1Style);
		rectPlatz1.getStyleClass().add(platz1Style);
		lblS1Name.setText(currentSpieler.getName().toString());
		lblS1PunkteGesamt.setText(currentSpieler.getPunktestand().getValue().toString() + " Punkte");

		currentSpieler = spielerliste.get(1);
		String platz2Style = currentSpieler.getName().toString().toLowerCase().replace(" ","");
		//lblS2Name.getStyleClass().add(platz2Style);
		rectPlatz2.getStyleClass().add(platz2Style);
		lblS2Name.setText(currentSpieler.getName().toString());
		lblS2PunkteGesamt.setText(currentSpieler.getPunktestand().getValue().toString() + " Punkte");

		if(spielerliste.size() == 2) {
			lblS3Name.setText("");
			lblS3PunkteGesamt.setText("");
			lblS3Name.setStyle("-fx-border-color: none;");
		}

		else if(spielerliste.size() == 3) {

			System.out.println("Spieler 3 sollte angezeigt werden");
			currentSpieler = spielerliste.get(2);
			String platz3Style = currentSpieler.getName().toString().toLowerCase().replace(" ","");
			//lblS3Name.getStyleClass().add(platz3Style);
			rectPlatz3.getStyleClass().add(platz3Style);

			lblS3Name.setText(currentSpieler.getName().toString());
			lblS3PunkteGesamt.setText(currentSpieler.getPunktestand().getValue().toString() + " Punkte");

		}

	}


}
