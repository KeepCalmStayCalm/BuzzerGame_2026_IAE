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

    // BUG FIX: reuse one HttpClient instead of creating a new one per request
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public static void main(String[] args) {
        launch(args);
    }

    private void readPreferences() {
        prefs = Preferences.userRoot().node(this.getClass().getName());
        MAX_FRAGEN      = Integer.parseInt(prefs.get("anzahl_fragen", "5"));
        MAX_ZEIT        = Integer.parseInt(prefs.get("time_out", "20"));
        shuffleQuestions = prefs.getBoolean("shuffle_questions", true);
        fullScreen      = prefs.getBoolean("full_screen", true);
        questionFile    = prefs.get("questions_file", "resources/fragenBuzzerGame.csv");
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
            System.out.println("⏳ Preloading views for smooth transitions...");

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

            System.out.println("✓ All views preloaded successfully!");
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
                Spieler s = new Spieler(defName, b);
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
                dialog.setHeaderText("Benutzername oder ID");
                dialog.setContentText("Dein Username / ID:");
                dialog.initModality(Modality.APPLICATION_MODAL);

                Optional<String> result = dialog.showAndWait();

                if (!result.isPresent() || result.get().trim().isEmpty()) {
                    isProcessing[0] = false; // allow retry
                    return;
                }

                String id = result.get().trim();

                // BUG FIX: the original code called thenAccept() for the API check
                // but then immediately continued executing the lines below it —
                // thenAccept() only schedules a callback, it does NOT block.
                // This meant the player was always registered BEFORE the HTTP reply
                // arrived, so the API check had zero effect on registration.
                //
                // Fix: move ALL registration logic (duplicate check, new Spieler,
                // setReady, removeListener) INSIDE thenAccept so it only runs after
                // we have a confirmed answer from the server.
                // Fetch nickname from API — all registration logic lives inside thenAccept
                // so it only runs after the server has responded.
                fetchNicknameAsync(id).thenAccept(nicknameOpt -> Platform.runLater(() -> {
                    try {
                        if (!nicknameOpt.isPresent()) {
                            new Alert(Alert.AlertType.ERROR,
                                "ID / Benutzer '" + id + "' nicht in der Datenbank gefunden!\n" +
                                "Bitte erneut versuchen.", ButtonType.OK).showAndWait();
                            return;
                        }

                        String nickname = nicknameOpt.get();

                        // Duplicate check uses the resolved nickname
                        if (alleSpieler.stream().anyMatch(s -> s.getName().equals(nickname))) {
                            new Alert(Alert.AlertType.WARNING,
                                "Spieler '" + nickname + "' ist bereits angemeldet!\n" +
                                "Bitte einen anderen Benutzer wählen.", ButtonType.OK).showAndWait();
                            return;
                        }

                        // Register with the real nickname from the API
                        Spieler s = new Spieler(nickname, buzzer);
                        alleSpieler.add(s);
                        obs.removeListener(holder[0]);
                        lobby.setReady(playerNum, nickname);
                        System.out.println("✓ " + nickname + " (ID: " + id + ") angemeldet als Spieler " + playerNum);

                    } finally {
                        isProcessing[0] = false;
                    }
                }));
                // NOTE: isProcessing stays true while the HTTP call is in flight,
                // preventing a second buzzer press from opening a second dialog.
            });
        };
        return holder[0];
    }

    /**
     * Looks up a participant by ID or nickname and returns their nickname.
     * Returns Optional.empty() if not found or on error.
     *
     * The API response format is:
     *   {"count": 1, "results": [{"id": 1, "nickname": "HAPPY_DOLPHIN", ...}]}
     * We check count > 0 and extract the nickname from results[0].
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
                        System.out.println("✗ User check for ID '" + id + "': NOT FOUND (Status: " + response.statusCode() + ")");
                        return Optional.<String>empty();
                    }
                    // Parse nickname from JSON: {"count":N,"results":[{"nickname":"VALUE",...}]}
                    String body = response.body();
                    String nickname = parseNickname(body);
                    if (nickname == null) {
                        System.out.println("✗ User check for ID '" + id + "': count=0 or nickname missing");
                        return Optional.<String>empty();
                    }
                    System.out.println("✓ User check for ID '" + id + "': FOUND → nickname='" + nickname + "'");
                    return Optional.of(nickname);
                })
                .exceptionally(e -> {
                    System.err.println("✗ User-Check Fehler für ID '" + id + "': " + e.getMessage());
                    return Optional.empty();
                });
    }

    /**
     * Extracts the nickname from the API JSON response.
     * Looks for "count" > 0 and then "nickname":"VALUE" in the results array.
     * Uses simple string parsing — no external JSON library needed.
     */
    private static String parseNickname(String json) {
        // Check count is not 0
        java.util.regex.Matcher countMatcher =
            java.util.regex.Pattern.compile("\"count\"\s*:\s*(\\d+)").matcher(json);
        if (!countMatcher.find() || Integer.parseInt(countMatcher.group(1)) == 0) return null;

        // Extract first "nickname":"VALUE"
        java.util.regex.Matcher nickMatcher =
            java.util.regex.Pattern.compile("\"nickname\"\s*:\s*\"([^\"]+)\"").matcher(json);
        return nickMatcher.find() ? nickMatcher.group(1) : null;
    }

    // BUG FIX: original sendFinalScoresToApi() used client.send() — a BLOCKING call —
    // on the JavaFX Application Thread inside showEndScene().  With 3 players and a
    // slow or unreachable server, this froze the UI for up to 3 × network-timeout
    // seconds (often 30 s each = 90 s of a completely frozen screen).
    //
    // Fix: fire all three POSTs concurrently with sendAsync() and let them complete
    // in the background.  Also fixed the JSON: teilnehmer must be a quoted string,
    // not a bare identifier (original had "teilnehmer":%s without quotes around %s).
    private void sendFinalScoresToApiAsync() {
        System.out.println("=== Sending Final Scores to API (async) ===");
        String url = "http://192.168.100.141:8080/scores/";

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Spieler spieler : alleSpieler) {
            String name  = spieler.getName();
            int    score = spieler.getPunktestand().get();

            // BUG FIX: original JSON was "teilnehmer":%s — missing quotes around the
            // string value, producing invalid JSON like "teilnehmer":Max instead of
            // "teilnehmer":"Max". Fixed to use %s with surrounding quotes.
            String json = String.format(
                "{\"teilnehmer\":\"%s\",\"score\":%d,\"game_type\":\"quiz\"}",
                name, score);

            System.out.println("  → Queuing score for " + name + ": " + score);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            futures.add(
                httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200 || response.statusCode() == 201) {
                            System.out.println("    ✓ Score saved for " + name);
                        } else {
                            System.err.println("    ✗ Failed for " + name +
                                " – Status: " + response.statusCode() +
                                " – " + response.body());
                        }
                    })
                    .exceptionally(e -> {
                        System.err.println("    ✗ Error for " + name + ": " + e.getMessage());
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
        sendFinalScoresToApiAsync(); // non-blocking — UI switches immediately
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createBuzzerView(String playername, double x, double y) {
        try {
            FXMLLoader root = new FXMLLoader(getClass().getResource("/view/FXBuzzer.fxml"));
            Parent parent = root.load();
            FXBuzzerController controller = root.getController();
            Spieler s = new Spieler(playername, controller);
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
