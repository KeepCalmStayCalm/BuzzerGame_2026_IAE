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

    private GameController gameController;

    @FXML Label lblS1Name;
    @FXML Label lblS2Name;
    @FXML Label lblS3Name;
    @FXML Label lblS1PunkteGesamt;
    @FXML Label lblS2PunkteGesamt;
    @FXML Label lblS3PunkteGesamt;

    @FXML AnchorPane rectPlatz1;
    @FXML AnchorPane rectPlatz2;
    @FXML AnchorPane rectPlatz3;

    /**
     * Set the main game controller
     */
    public void setMainController(GameController mainController) {
        this.gameController = mainController;
    }

    /**
     * Return to startup screen after game ends
     */
    @FXML
    public void endGameRound() {
        if (gameController != null) {
            gameController.showStartupView();
        }
    }

    /**
     * Display final player rankings
     */
    public void setSpielerInformation(Set<Spieler> spielerSet) {
        if (spielerSet == null || spielerSet.isEmpty()) {
            System.err.println("No players to display!");
            return;
        }

        // Sort players by final score (descending)
        List<Spieler> spielerliste = spielerSet.stream()
                                               .collect(Collectors.toList());
        Collections.sort(spielerliste, new Comparator<Spieler>() {
            @Override
            public int compare(Spieler s1, Spieler s2) {
                return s2.getPunktestand().get() - s1.getPunktestand().get();
            }
        });

        // Display 1st place
        if (spielerliste.size() >= 1) {
            displayPlayer(1, spielerliste.get(0));
        }

        // Display 2nd place
        if (spielerliste.size() >= 2) {
            displayPlayer(2, spielerliste.get(1));
        }

        // Display 3rd place or hide if only 2 players
        if (spielerliste.size() >= 3) {
            displayPlayer(3, spielerliste.get(2));
        } else {
            hidePlayer(3);
        }
    }

    /**
     * Display a player on their podium position
     */
    private void displayPlayer(int position, Spieler spieler) {
        Label lblName = getNameLabel(position);
        Label lblPunkte = getPunkteLabel(position);
        AnchorPane rect = getRectangle(position);

        if (spieler == null) return;

        // Set player name
        if (lblName != null) {
            String playerStyle = spieler.getName().toString()
                                       .toLowerCase()
                                       .replace(" ", "");
            
            // Add player color to podium
            if (rect != null) {
                rect.getStyleClass().add(playerStyle);
            }
            
            lblName.setText(spieler.getName().toString());
        }

        // Set player score
        if (lblPunkte != null) {
            lblPunkte.setText(spieler.getPunktestand().getValue().toString() + " Punkte");
        }
    }

    /**
     * Hide a player position (for 2-player games)
     */
    private void hidePlayer(int position) {
        Label lblName = getNameLabel(position);
        Label lblPunkte = getPunkteLabel(position);

        if (lblName != null) {
            lblName.setText("");
            lblName.setStyle("-fx-border-color: none;");
        }
        
        if (lblPunkte != null) {
            lblPunkte.setText("");
        }
    }

    // Helper methods to get UI elements
    private Label getNameLabel(int position) {
        switch (position) {
            case 1: return lblS1Name;
            case 2: return lblS2Name;
            case 3: return lblS3Name;
            default: return null;
        }
    }

    private Label getPunkteLabel(int position) {
        switch (position) {
            case 1: return lblS1PunkteGesamt;
            case 2: return lblS2PunkteGesamt;
            case 3: return lblS3PunkteGesamt;
            default: return null;
        }
    }

    private AnchorPane getRectangle(int position) {
        switch (position) {
            case 1: return rectPlatz1;
            case 2: return rectPlatz2;
            case 3: return rectPlatz3;
            default: return null;
        }
    }
}
