package view;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import application.GameController;
import application.Spieler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class EndViewController {

    @FXML private GameController gameController;

    @FXML private Label lblS1Name, lblS2Name, lblS3Name;
    @FXML private Label lblS1PunkteGesamt, lblS2PunkteGesamt, lblS3PunkteGesamt;
    @FXML private AnchorPane rectPlatz1, rectPlatz2, rectPlatz3;
    @FXML private Button btnEndGame;

    public void setMainController(GameController mainController) {
        this.gameController = mainController;
    }

    public void endGameRound() {
        gameController.showStartupView();
    }

    public void setSpielerInformation(Set<Spieler> spielerSet) {
        // Sort players by score (highest first)
        List<Spieler> spielerliste = spielerSet.stream()
            .sorted((s1, s2) -> Integer.compare(s2.getPunktestand().get(), s1.getPunktestand().get()))
            .collect(Collectors.toList());

        // Placement 1 (Gold)
        if (spielerliste.size() >= 1) {
            Spieler s1 = spielerliste.get(0);
            lblS1Name.setText(s1.getName());
            lblS1PunkteGesamt.setText(s1.getPunktestand().get() + " Punkte");
            rectPlatz1.getStyleClass().add(s1.getName().toLowerCase().replace(" ", ""));
        }

        // Placement 2 (Silver)
        if (spielerliste.size() >= 2) {
            Spieler s2 = spielerliste.get(1);
            lblS2Name.setText(s2.getName());
            lblS2PunkteGesamt.setText(s2.getPunktestand().get() + " Punkte");
            rectPlatz2.getStyleClass().add(s2.getName().toLowerCase().replace(" ", ""));
            rectPlatz2.setVisible(true);
        } else {
            lblS2Name.setText("");
            lblS2PunkteGesamt.setText("");
            rectPlatz2.setVisible(false);
        }

        // Placement 3 (Bronze)
        if (spielerliste.size() >= 3) {
            Spieler s3 = spielerliste.get(2);
            lblS3Name.setText(s3.getName());
            lblS3PunkteGesamt.setText(s3.getPunktestand().get() + " Punkte");
            rectPlatz3.getStyleClass().add(s3.getName().toLowerCase().replace(" ", ""));
            rectPlatz3.setVisible(true);
        } else {
            lblS3Name.setText("");
            lblS3PunkteGesamt.setText("");
            rectPlatz3.setVisible(false);
        }
    }
}
