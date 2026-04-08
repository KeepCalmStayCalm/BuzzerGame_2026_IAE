package application;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
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
    private Scene mainScene;

    private Parent startupView;
    private StartupViewController startupController;
    private Parent lobbyView;
    private LobbyViewController lobbyController;
    private Parent questionView;
    private QuestionViewController questionController;
    private Parent answerView;
    private AnswerViewController answerController;
    private Parent endView;
    private EndViewController endController;

    private int rundenCounter = 0;
    private List<Frage> eingeleseneFragen;
    private Spielrunde spielrunde;
    private Set<Spieler> alleSpieler = new HashSet<>();
    private IBuzzer buzzer1, buzzer2, buzzer3;

    private int MAX_ZEIT = 30;   // changed default: 30 s
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

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public static void main(String[] args) {
        launch(args);
    }

    private void readPreferences() {
        prefs = Preferences.userRoot().node(this.getClass().getName());
        MAX_FRAGEN       = Integer.parseInt(prefs.get("anzahl_fragen", "5"));
        MAX_ZEIT         = Integer.parseInt(prefs.get("time_out", "30"));  // default now 30 s
        shuffleQuestions = prefs.getBoolean("shuffle_questions", true);
        fullScreen       = prefs.getBoolean("full_screen", true);
        questionFile     = prefs.get("questions_file", "resources/fragenBuzzerGame.csv");
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

        mainScene = new Scene(new javafx.scene.layout.Pane(), 1920, 1080);
        mainScene.getStylesheets().add(style);
        myStage.setScene(mainScene);

        preloadViews();
        showStartupView();

        myStage.setOnCloseRequest(e -> System.exit(0));
        myStage.show();
    }

    private void preloadViews() {
        try {
            System.out.println("⏳ Preloading views...");

            FXMLLoader l1 = new FXMLLoader(getClass().getResource("/view/StartupView.fxml"));
            startupView = l1.load();
            startupController = l1.getController();
            startupController.setMainController(this);

            FXMLLoader l2 = new FXMLLoader(getClass().getResource("/view/LobbyView.fxml"));
            lobbyView = l2.load();
            lobbyController = l2.getController();
            lobbyController.setMainController(this);

            FXMLLoader l3 = new FXMLLoader(getClass().getResource("/view/QuestionView2025.fxml"));
            questionView = l3.load();
            questionController = l3.getController();
            questionController.setMainController(this);

            FXMLLoader l4 = new FXMLLoader(getClass().getResource("/view/AnswerView.fxml"));
            answerView = l4.load();
            answerController = l4.getController();

            FXMLLoader l5 = new FXMLLoader(getClass().getResource("/view/EndView.fxml"));
            endView = l5.load();
            endController = l5.getController();
            endController.setMainController(this);

            System.out.println("✓ All views preloaded!");
        } catch (Exception e) {
            System.err.println("Error preloading views: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void switchToView(Parent view) {
        mainScene.setRoot(view);
    }

    public void showStartupView() { switchToView(startupView); }

    public void showLobbyView() {
        alleSpieler.clear();

        if (!IS_DEV_MODE) {
            if (pi4j != null) pi4j.shutdown();
            pi4j    = Pi4J.newAutoContext();
            buzzer1 = new RaspiBuzzer(pi4j, 16, 20, 21);
            buzzer2 = new RaspiBuzzer(pi4j, 22, 27, 17);
            buzzer3 = new RaspiBuzzer(pi4j, 13, 19, 26);
        } else {
            buzzer1 = new MouseBuzzer();
            buzzer2 = new DummyBuzzer(2);
            buzzer3 = new DummyBuzzer(3);
        }

        lobbyController.resetReadyStates();

        List<IBuzzer> buzzers = List.of(buzzer1, buzzer2, buzzer3);
        String[] defaultNames = {"Spieler 1", "Spieler 2", "Spieler 3"};

        for (int i = 0; i < 3; i++) {
            final IBuzzer b        = buzzers.get(i);
            final String defName   = defaultNames[i];
            final int    playerNum = i + 1;

            if (IS_DEV_MODE) {
                Spieler s = new Spieler(0, defName, b);   // dummy ID for dev mode
                alleSpieler.add(s);
                lobbyController.setReady(playerNum, defName);
            } else {
                b.getAnswer().addListener(
                    setupBuzzerListener(defName, b, lobbyController, playerNum));
            }
        }

        if (shuffleQuestions) Collections.shuffle(eingeleseneFragen);
        spielrunde = new Spielrunde(
            eingeleseneFragen.subList(0, Math.min(MAX_FRAGEN, eingeleseneFragen.size())));

        switchToView(lobbyView);
    }

    private ChangeListener<Number> setupBuzzerListener(String defaultName, IBuzzer buzzer,
                                                        LobbyViewController lobby, int playerNum) {
        final ChangeListener<Number>[] holder = new ChangeListener[1];
        final boolean[] isProcessing = {false};

        holder[0] = (obs, old, newVal) -> {
            if (newVal == null || newVal.intValue() <= 0 || isProcessing[0]) return;
            isProcessing[0] = true;

            Platform.runLater(() -> {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Anmeldung – Spieler " + playerNum);
                dialog.setHeaderText("Spieler-ID eingeben");
                dialog.setContentText("Deine ID:");
                dialog.initModality(Modality.APPLICATION_MODAL);

                Optional<String> result = dialog.showAndWait();

                if (!result.isPresent() || result.get().trim().isEmpty()) {
                    isProcessing[0] = false;
                    return;
                }

                String id = result.get().trim();

                // Fetch the player's actual nickname from the API.
                // All registration logic runs inside thenAccept so it only
                // executes after we have the server response.
                fetchNicknameAsync(id).thenAccept(nicknameOpt -> Platform.runLater(() -> {
                    try {
                        if (!nicknameOpt.isPresent()) {
                            new Alert(Alert.AlertType.ERROR,
                                "ID '" + id + "' nicht in der Datenbank gefunden!\n" +
                                "Bitte erneut versuchen.", ButtonType.OK).showAndWait();
                            return;
                        }

                        String nickname = nicknameOpt.get();

                        if (alleSpieler.stream().anyMatch(s -> s.getName().equals(nickname))) {
                            new Alert(Alert.AlertType.WARNING,
                                "'" + nickname + "' ist bereits angemeldet!\n" +
                                "Bitte eine andere ID wählen.", ButtonType.OK).showAndWait();
                            return;
                        }

                        // === FIXED: store the numeric ID (PK) instead of only the nickname ===
                        int teilnehmerId = Integer.parseInt(id);
                        Spieler s = new Spieler(teilnehmerId, nickname, buzzer);
                        alleSpieler.add(s);
                        obs.removeListener(holder[0]);
                        lobby.setReady(playerNum, nickname);
                        System.out.println("✓ " + nickname + " (ID " + id + ") angemeldet als Spieler " + playerNum);

                    } finally {
                        isProcessing[0] = false;
                    }
                }));
            });
        };
        return holder[0];
    }

    /**
     * Calls the API with the given ID and returns the player's nickname.
     * Returns Optional.empty() if the ID is not found or the request fails.
     *
     * The API returns a paginated JSON object like:
     *   {"count":1,"results":[{"id":1,"nickname":"PRODUCTIVE_GECKO",...}]}
     * We extract the first "nickname" value from the response body.
     */
    private CompletableFuture<Optional<String>> fetchNicknameAsync(String id) {
        String url = "http://192.168.100.141:8080/teilnehmer/?id=" + id;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        System.out.println("✗ ID '" + id + "': NOT FOUND (Status: "
                                + response.statusCode() + ")");
                        return Optional.<String>empty();
                    }

                    // Parse "nickname":"VALUE" from the JSON response body
                    // without requiring an external JSON library.
                    java.util.regex.Matcher m = java.util.regex.Pattern
                            .compile("\"nickname\"\\s*:\\s*\"([^\"]+)\"")
                            .matcher(response.body());

                    if (m.find()) {
                        String nickname = m.group(1);
                        System.out.println("✓ ID '" + id + "' → nickname: " + nickname);
                        return Optional.of(nickname);
                    }

                    System.out.println("✗ ID '" + id + "': response OK but no nickname in body");
                    return Optional.<String>empty();
                })
                .exceptionally(e -> {
                    System.err.println("✗ API-Fehler für ID '" + id + "': " + e.getMessage());
                    return Optional.empty();
                });
    }

    private void sendFinalScoresToApiAsync() {
        System.out.println("=== Sending Final Scores to API (async) ===");
        String url = "http://192.168.100.141:8080/scores/";

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Spieler spieler : alleSpieler) {
            int teilnehmerPk = spieler.getTeilnehmerId();
            int score = spieler.getPunktestand().get();

            String json = String.format(
                "{\"teilnehmer\":%d,\"score\":%d,\"game_type\":\"quiz\"}",
                teilnehmerPk, score);

            System.out.println("  → Queuing score for " + spieler.getName() + " (ID " + teilnehmerPk + "): " + score);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            futures.add(
                httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200 || response.statusCode() == 201) {
                            System.out.println("    ✓ Score saved for " + spieler.getName());
                        } else {
                            System.err.println("    ✗ Failed for " + spieler.getName()
                                + " – Status: " + response.statusCode()
                                + " – " + response.body());
                        }
                    })
                    .exceptionally(e -> {
                        System.err.println("    ✗ Error for " + spieler.getName() + ": " + e.getMessage());
                        return null;
                    })
            );
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> System.out.println("=== All scores submitted ==="));
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
        questionController.initFrage(question, alleSpieler, MAX_ZEIT);
        questionController.getRestzeit().addListener(showAnswerSceneListener);
        switchToView(questionView);
    }

    public void showAnswerScene() {
        answerController.setInformation(aktuelleFrage, alleSpieler);
        answerController.getRestzeit().addListener(showNextQuestionListener);
        switchToView(answerView);
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
        sendFinalScoresToApiAsync();
        endController.setSpielerInformation(alleSpieler);
        switchToView(endView);
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
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(myStage);
            stage.showAndWait();
            readPreferences();
            // FIX: reload questions from the (possibly changed) questionFile.
            // Previously only done once at startup, so changing the CSV in
            // settings had no effect until the whole app was restarted.
            eingeleseneFragen = EinAuslesenFragen.einlesenFragen(questionFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createBuzzerView(String playername, double x, double y) {
        try {
            FXMLLoader root = new FXMLLoader(getClass().getResource("/view/FXBuzzer.fxml"));
            Parent parent = root.load();
            FXBuzzerController controller = root.getController();
            Spieler s = new Spieler(0, playername, controller);   // dummy ID for test view
            alleSpieler.add(s);
            Stage stage = new Stage();
            stage.setTitle(playername);
            stage.setScene(new Scene(parent));
            stage.setX(x);
            stage.setY(y);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Set<Spieler> getSpielerliste() {
        return alleSpieler;
    }
}
