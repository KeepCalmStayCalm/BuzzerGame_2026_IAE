package application;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.prefs.Preferences;
import com.pi4j.Pi4J;
import com.pi4j.context.Context;

import view.*;

public class GameController extends Application {

    public static final boolean IS_DEV_MODE = false;

    private Stage myStage;
    private StartupViewController startupController;
    private int rundenCounter = 0;
    private List<Frage> eingeleseneFragen;
    private Spielrunde spielrunde;
    private Set<Spieler> alleSpieler = new HashSet<>();
    private IBuzzer buzzer1, buzzer2, buzzer3;

    private int MAX_ZEIT = 20;
    private int MAX_FRAGEN = 5;
    private Frage aktuelleFrage;
    private boolean shuffleQuestions = true;
    private boolean fullScreen = true;

    private Preferences prefs;
    private String style;
    private Context pi4j;
    private String questionFile = "resources/fragenBuzzerGame.csv";

    private ChangeListener<Number> showAnswerSceneListener;
    private ChangeListener<Number> showNextQuestionListener;

    public static void main(String[] args) {
        launch(args);
    }

    private void readPreferences() {
        prefs = Preferences.userRoot().node(this.getClass().getName());
        MAX_FRAGEN = Integer.parseInt(prefs.get("anzahl_fragen", "5"));
        MAX_ZEIT = Integer.parseInt(prefs.get("time_out", "20"));
        shuffleQuestions = prefs.getBoolean("shuffle_questions", true);
        fullScreen = prefs.getBoolean("full_screen", true);
        questionFile = prefs.get("questions_file", "resources/fragenBuzzerGame.csv");
    }

    private void initListeners() {
        showAnswerSceneListener = (o, a, newValue) -> {
            if (newValue.intValue() <= 0) {
                o.removeListener(showAnswerSceneListener);
                Platform.runLater(this::showAnswerScene);
            }
        };

        showNextQuestionListener = (o, a, newValue) -> {
            if (newValue.intValue() <= 0) {
                o.removeListener(showNextQuestionListener);
                Platform.runLater(this::scoreNotifyDone);
            }
        };
    }

    @Override
    public void stop() {
        if (pi4j != null) pi4j.shutdown();
    }

    @Override
    public void start(Stage primaryStage) {
        readPreferences();
        initListeners();

        style = getClass().getResource("buzzerStyle2025.css").toExternalForm();
        eingeleseneFragen = EinAuslesenFragen.einlesenFragen(questionFile);

        myStage = primaryStage;
        myStage.setTitle("IAE Buzzer Quiz");
        if (fullScreen) {
            myStage.setFullScreenExitHint("");
            myStage.setFullScreen(true);
        }
        showStartupView();

        primaryStage.setOnCloseRequest(e -> System.exit(0));
    }

    public void showStartupView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/StartupView.fxml"));
            Scene scene = new Scene(loader.load(), 1920, 1080);
            scene.getStylesheets().add(style);
            startupController = loader.getController();
            startupController.setMainController(this);
            myStage.setScene(scene);
            if (fullScreen) myStage.setFullScreen(true);
            myStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showLobbyView() {
        alleSpieler.clear();
        if (!IS_DEV_MODE) {
            pi4j = Pi4J.newAutoContext();
            buzzer1 = new RaspiBuzzer(pi4j, 16, 20, 21);
            buzzer2 = new RaspiBuzzer(pi4j, 22, 27, 17);
            buzzer3 = new RaspiBuzzer(pi4j, 13, 19, 26);
        } else {
            buzzer1 = new MouseBuzzer();
            buzzer2 = new DummyBuzzer(2);
            buzzer3 = new DummyBuzzer(3);
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LobbyView.fxml"));
            Scene lobbyScene = new Scene(loader.load(), 1920, 1080);
            lobbyScene.getStylesheets().add(style);

            LobbyViewController lobbyController = loader.getController();
            lobbyController.setMainController(this);
            lobbyController.resetReadyStates();

            List<IBuzzer> buzzers = List.of(buzzer1, buzzer2, buzzer3);
            String[] defaultNames = {"Spieler 1", "Spieler 2", "Spieler 3"};

            for (int i = 0; i < 3; i++) {
                final int playerNum = i + 1;
                final IBuzzer b = buzzers.get(i);
                final String defaultName = defaultNames[i];

                if (IS_DEV_MODE) {
                    Spieler s = new Spieler(defaultName, b);
                    alleSpieler.add(s);
                    lobbyController.setReady(playerNum, defaultName);
                } else {
                    b.getAnswer().addListener(setupBuzzerListener(defaultName, b, lobbyController, playerNum));
                }
            }

            if (shuffleQuestions) Collections.shuffle(eingeleseneFragen);
            spielrunde = new Spielrunde(eingeleseneFragen.subList(0, Math.min(MAX_FRAGEN, eingeleseneFragen.size())));

            myStage.setScene(lobbyScene);
            if (fullScreen) myStage.setFullScreen(true);
            myStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Setup buzzer listener for player registration in lobby.
     * CRITICAL: Wraps UI operations in Platform.runLater() to avoid threading issues.
     * The listener stays active until a player successfully registers (allows retry on cancel/error).
     */
    private ChangeListener<Number> setupBuzzerListener(String defaultName, IBuzzer buzzer,
                                                       LobbyViewController lobbyController, int playerNum) {
        final ChangeListener<Number>[] holder = new ChangeListener[1];
        final boolean[] isProcessing = {false}; // Prevent multiple simultaneous dialogs
        
        holder[0] = (obs, old, newVal) -> {
            if (newVal != null && newVal.intValue() > 0 && !isProcessing[0]) {
                isProcessing[0] = true; // Lock to prevent multiple dialogs
                
                // CRITICAL FIX: Pi4J calls this from a background thread
                // ALL JavaFX UI operations MUST run on the FX Application Thread
                Platform.runLater(() -> {
                    try {
                        TextInputDialog dialog = new TextInputDialog();
                        dialog.setTitle("Anmeldung – Spieler " + playerNum);
                        dialog.setHeaderText("Benutzername oder ID");
                        dialog.setContentText("Dein Username / ID:");
                        dialog.initModality(Modality.APPLICATION_MODAL);

                        Optional<String> result = dialog.showAndWait();
                        
                        // If cancelled, allow retry by not removing listener
                        if (!result.isPresent() || result.get().trim().isEmpty()) {
                            System.out.println("Player " + playerNum + " cancelled registration - can retry");
                            return;
                        }

                        String usernameOrId = result.get().trim();

                        // Check if user exists in database
                        if (!checkUserExists(usernameOrId)) {
                            Alert alert = new Alert(Alert.AlertType.ERROR,
                                    "Benutzer '" + usernameOrId + "' nicht in der Datenbank gefunden!\n" +
                                    "Bitte erneut versuchen.");
                            alert.showAndWait();
                            // Don't remove listener - allow retry
                            return;
                        }

                        // Check if user is already registered
                        if (alleSpieler.stream().anyMatch(s -> s.getName().equals(usernameOrId))) {
                            Alert alert = new Alert(Alert.AlertType.WARNING,
                                    "Benutzer '" + usernameOrId + "' ist bereits angemeldet!\n" +
                                    "Bitte einen anderen Benutzer wählen.");
                            alert.showAndWait();
                            // Don't remove listener - allow retry
                            return;
                        }

                        // SUCCESS - Register player and remove listener
                        Spieler s = new Spieler(usernameOrId, buzzer);
                        alleSpieler.add(s);
                        obs.removeListener(holder[0]); // Only remove on success
                        lobbyController.setReady(playerNum, usernameOrId);
                        System.out.println("✓ " + usernameOrId + " erfolgreich angemeldet (Spieler " + playerNum + ")");
                        
                    } finally {
                        isProcessing[0] = false; // Always unlock
                    }
                });
            }
        };
        return holder[0];
    }

    /**
     * Check if a user exists in the database via API call
     * API endpoint: GET /teilnehmer/?id={id}
     */
    private boolean checkUserExists(String usernameOrId) {
        // API expects: /teilnehmer/?id=1
        String url = "http://192.168.100.141:8080/teilnehmer/?id=" + usernameOrId;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            boolean exists = response.statusCode() == 200;
            
            if (exists) {
                System.out.println("✓ User check for ID '" + usernameOrId + "': FOUND");
            } else {
                System.out.println("✗ User check for ID '" + usernameOrId + "': NOT FOUND (Status: " + response.statusCode() + ")");
            }
            
            return exists;
        } catch (Exception e) {
            System.err.println("✗ User-Check Fehler für ID '" + usernameOrId + "': " + e.getMessage());
            return false;
        }
    }

    public void createBuzzerView(String playername, double xPosition, double yPosition) {
        try {
            FXMLLoader root = new FXMLLoader(getClass().getResource("/view/FXBuzzer.fxml"));
            Parent parent = root.load();
            Stage stage = new Stage();
            Scene scene = new Scene(parent);
            stage.setTitle(playername);
            FXBuzzerController controller = root.getController();
            Spieler s = new Spieler(playername, controller);
            alleSpieler.add(s);
            stage.setScene(scene);
            stage.setX(xPosition);
            stage.setY(yPosition);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void lobbyNotifyDone() {
        if (alleSpieler.size() >= 2) {
            rundenCounter = 0;
            aktuelleFrage = spielrunde.naechsteFrage();
            showQuestionView(aktuelleFrage);
        } else {
            new Alert(Alert.AlertType.WARNING, "Mindestens zwei Spieler benötigt!", ButtonType.OK).showAndWait();
        }
    }

    public void showQuestionView(Frage question) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/QuestionView2025.fxml"));
            Scene scene = new Scene(loader.load(), 1920, 1080);
            scene.getStylesheets().add(style);

            QuestionViewController qController = loader.getController();
            qController.setMainController(this);
            qController.initFrage(question, alleSpieler, MAX_ZEIT);

            qController.getRestzeit().addListener(showAnswerSceneListener);

            myStage.setScene(scene);
            if (fullScreen) myStage.setFullScreen(true);
            myStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showAnswerScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AnswerView.fxml"));
            Scene scene = new Scene(loader.load(), 1920, 1080);
            scene.getStylesheets().add(style);
            AnswerViewController controller = loader.getController();
            controller.setInformation(aktuelleFrage, alleSpieler);
            controller.getRestzeit().addListener(showNextQuestionListener);

            myStage.setScene(scene);
            if (fullScreen) myStage.setFullScreen(true);
            myStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void scoreNotifyDone() {
        rundenCounter++;
        if (rundenCounter < MAX_FRAGEN) {
            aktuelleFrage = spielrunde.naechsteFrage();
            showQuestionView(aktuelleFrage);
        } else {
            showEndScene();
        }
    }

    public void showEndScene() {
        sendFinalScoresToApi();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EndView.fxml"));
            Scene scene = new Scene(loader.load(), 1920, 1080);
            scene.getStylesheets().add(style);
            EndViewController controller = loader.getController();
            controller.setMainController(this);
            controller.setSpielerInformation(alleSpieler);

            myStage.setScene(scene);
            if (fullScreen) myStage.setFullScreen(true);
            myStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Send final scores for ALL players to the API
     * Sends INDIVIDUAL POST requests for each player (API expects separate records)
     */
    private void sendFinalScoresToApi() {
        System.out.println("=== Sending Final Scores to API ===");
        System.out.println("Total players: " + alleSpieler.size());
        
        String url = "http://192.168.100.141:8080/scores/";
        HttpClient client = HttpClient.newHttpClient();
        
        int successCount = 0;
        int failCount = 0;
        
        // Send individual POST for EACH player
        for (Spieler spieler : alleSpieler) {
            String playerName = spieler.getName();
            int playerScore = spieler.getPunktestand().get();
            
            // Build JSON for this ONE player
            String jsonBody = String.format(
                "{\"teilnehmer\":%s,\"score\":%d,\"game_type\":\"quiz\"}",
                playerName,  // Player ID (e.g., "1", "2", "3")
                playerScore  // Final score
            );
            
            System.out.println("  → Sending score for player " + playerName + ": " + playerScore + " Punkte");
            System.out.println("    JSON: " + jsonBody);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200 || response.statusCode() == 201) {
                    System.out.println("    ✓ SUCCESS: Player " + playerName + " score saved!");
                    successCount++;
                } else {
                    System.err.println("    ✗ FAILED: Status " + response.statusCode());
                    System.err.println("    Response: " + response.body());
                    failCount++;
                }
                
            } catch (Exception e) {
                System.err.println("    ✗ ERROR: " + e.getMessage());
                failCount++;
            }
        }
        
        System.out.println("=================================");
        System.out.println("Summary: " + successCount + " succeeded, " + failCount + " failed");
        System.out.println("=================================");
    }

    public void editSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EditSettingsView.fxml"));
            Scene scene = new Scene(loader.load());
            EditSettingsViewController controller = loader.getController();
            controller.setPreferences(prefs);
            if (!IS_DEV_MODE) controller.setBuzzers(buzzer1, buzzer2, buzzer3);

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Einstellungen und Hardware-Test");
            stage.showAndWait();
            readPreferences();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Set<Spieler> getSpielerliste() {
        return alleSpieler;
    }
}
