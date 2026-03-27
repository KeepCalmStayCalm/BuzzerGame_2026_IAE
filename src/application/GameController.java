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

    /** Holds both the numeric API pk and the display nickname. */
    private static class PlayerInfo {
        final int    id;
        final String nickname;
        PlayerInfo(int id, String nickname) { this.id = id; this.nickname = nickname; }
    }

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

    private int MAX_ZEIT = 30;
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

    public static void main(String[] args) { launch(args); }

    private void readPreferences() {
        prefs = Preferences.userRoot().node(this.getClass().getName());
        MAX_FRAGEN       = Integer.parseInt(prefs.get("anzahl_fragen", "5"));
        MAX_ZEIT         = Integer.parseInt(prefs.get("time_out", "30"));
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
    public void stop() { if (pi4j != null) pi4j.shutdown(); }

    @Override
    public void start(Stage primaryStage) {
        readPreferences();
        initListeners();

        style = getClass().getResource("buzzerStyle2025.css").toExternalForm();
        eingeleseneFragen = EinAuslesenFragen.einlesenFragen(questionFile);

        // FIX: warn the user immediately at startup if the CSV could not be loaded,
        // rather than silently continuing with 0 questions and crashing on game start.
        if (eingeleseneFragen.isEmpty()) {
            System.err.println("WARNING: No questions loaded from: " + questionFile);
        }

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
            System.out.println("Preloading views...");

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

            System.out.println("All views preloaded.");
        } catch (Exception e) {
            System.err.println("Error preloading views: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void switchToView(Parent view) { mainScene.setRoot(view); }

    public void showStartupView() { switchToView(startupView); }

    public void showLobbyView() {
        // Re-read the question file every time the lobby is entered so that
        // a CSV change in settings takes effect without restarting the app.
        eingeleseneFragen = EinAuslesenFragen.einlesenFragen(questionFile);
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

        List<IBuzzer> buzzers   = List.of(buzzer1, buzzer2, buzzer3);
        String[] defaultNames   = {"Spieler 1", "Spieler 2", "Spieler 3"};

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

        int available = eingeleseneFragen.size();
        spielrunde = new Spielrunde(
            eingeleseneFragen.subList(0, Math.min(MAX_FRAGEN, available)));

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
                dialog.setTitle("Anmeldung - Spieler " + playerNum);
                dialog.setHeaderText("Spieler-ID eingeben");
                dialog.setContentText("Deine ID:");
                dialog.initModality(Modality.APPLICATION_MODAL);

                Optional<String> result = dialog.showAndWait();

                if (!result.isPresent() || result.get().trim().isEmpty()) {
                    isProcessing[0] = false;
                    return;
                }

                String inputId = result.get().trim();

                fetchPlayerInfoAsync(inputId).thenAccept(infoOpt -> Platform.runLater(() -> {
                    try {
                        if (!infoOpt.isPresent()) {
                            new Alert(Alert.AlertType.ERROR,
                                "ID '" + inputId + "' nicht in der Datenbank gefunden!\n" +
                                "Bitte erneut versuchen.", ButtonType.OK).showAndWait();
                            return;
                        }

                        PlayerInfo info = infoOpt.get();

                        if (alleSpieler.stream().anyMatch(s -> s.getName().equals(info.nickname))) {
                            new Alert(Alert.AlertType.WARNING,
                                "'" + info.nickname + "' ist bereits angemeldet!\n" +
                                "Bitte eine andere ID waehlen.", ButtonType.OK).showAndWait();
                            return;
                        }

                        Spieler s = new Spieler(info.nickname, buzzer);
                        s.setApiId(info.id);
                        alleSpieler.add(s);
                        obs.removeListener(holder[0]);
                        lobby.setReady(playerNum, info.nickname);
                        System.out.println("Registered: " + info.nickname
                            + " (pk=" + info.id + ") as player " + playerNum);

                    } finally {
                        isProcessing[0] = false;
                    }
                }));
            });
        };
        return holder[0];
    }

    /**
     * Fetches both the numeric pk and the nickname from the first result.
     * Returns Optional.empty() if the ID is not found or the request fails.
     */
    private CompletableFuture<Optional<PlayerInfo>> fetchPlayerInfoAsync(String searchId) {
        String url = "http://192.168.100.141:8080/teilnehmer/?id=" + searchId;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url)).GET().build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        System.out.println("ID '" + searchId + "': HTTP " + response.statusCode());
                        return Optional.<PlayerInfo>empty();
                    }
                    String body = response.body();

                    java.util.regex.Matcher countM = java.util.regex.Pattern
                            .compile("\"count\"\\s*:\\s*(\\d+)").matcher(body);
                    if (countM.find() && Integer.parseInt(countM.group(1)) == 0) {
                        System.out.println("ID '" + searchId + "': not found (count=0)");
                        return Optional.<PlayerInfo>empty();
                    }

                    java.util.regex.Matcher idM = java.util.regex.Pattern
                            .compile("\"id\"\\s*:\\s*(\\d+)").matcher(body);
                    java.util.regex.Matcher nickM = java.util.regex.Pattern
                            .compile("\"nickname\"\\s*:\\s*\"([^\"]+)\"").matcher(body);

                    if (idM.find() && nickM.find()) {
                        int    apiId    = Integer.parseInt(idM.group(1));
                        String nickname = nickM.group(1);
                        System.out.println("ID '" + searchId + "' -> " + nickname + " (pk=" + apiId + ")");
                        return Optional.of(new PlayerInfo(apiId, nickname));
                    }

                    System.out.println("ID '" + searchId + "': response OK but no id/nickname found");
                    return Optional.<PlayerInfo>empty();
                })
                .exceptionally(e -> {
                    System.err.println("API error for ID '" + searchId + "': " + e.getMessage());
                    return Optional.empty();
                });
    }

    /**
     * Posts final scores using the numeric player pk.
     * "teilnehmer" is the integer pk, not the nickname string.
     */
    private void sendFinalScoresToApiAsync() {
        System.out.println("=== Sending final scores to API ===");
        String url = "http://192.168.100.141:8080/scores/";
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Spieler spieler : alleSpieler) {
            String name  = spieler.getName();
            int    score = spieler.getPunktestand().get();
            int    apiId = spieler.getApiId();

            if (apiId < 0) {
                System.out.println("Skipping " + name + " (no API id - dev mode)");
                continue;
            }

            String json = String.format(
                "{\"teilnehmer\":%d,\"score\":%d,\"game_type\":\"quiz\"}",
                apiId, score);

            System.out.println("Queuing score for " + name + " (pk=" + apiId + "): " + score);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            futures.add(
                httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200 || response.statusCode() == 201) {
                            System.out.println("Score saved for " + name);
                        } else {
                            System.err.println("Failed for " + name
                                + " - Status: " + response.statusCode()
                                + " - " + response.body());
                        }
                    })
                    .exceptionally(e -> {
                        System.err.println("Error for " + name + ": " + e.getMessage());
                        return null;
                    })
            );
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> System.out.println("=== All scores submitted ==="));
    }

    public void lobbyNotifyDone() {
        if (alleSpieler.size() < 2) {
            new Alert(Alert.AlertType.WARNING,
                "Mindestens zwei Spieler benoetigt!", ButtonType.OK).showAndWait();
            return;
        }

        // FIX: show a clear error if no questions were loaded (bad or missing CSV)
        // instead of crashing with IndexOutOfBoundsException inside Spielrunde.
        if (eingeleseneFragen.isEmpty()) {
            new Alert(Alert.AlertType.ERROR,
                "Keine Fragen geladen!\n\n" +
                "Bitte pruefen Sie die Einstellungen und stellen Sie sicher,\n" +
                "dass eine gueltige CSV-Datei ausgewaehlt ist.",
                ButtonType.OK).showAndWait();
            return;
        }

        rundenCounter = 0;
        aktuelleFrage = spielrunde.naechsteFrage();
        showQuestionView(aktuelleFrage);
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
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/EditSettingsView.fxml"));
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
            FXMLLoader root = new FXMLLoader(
                getClass().getResource("/view/FXBuzzer.fxml"));
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

    public Set<Spieler> getSpielerliste() { return alleSpieler; }
}
