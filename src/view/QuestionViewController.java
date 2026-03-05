package view;

import java.util.Set;

import application.Frage;
import application.GameController;
import application.Spieler;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.util.Duration;

/**
 * Controls the question screen.
 *
 * ROOT CAUSE OF "screen never transitions / timer display frozen":
 *
 *   The previous implementation cancelled the Timeline in cleanup() but never
 *   set restzeit to 0.  GameController's showAnswerSceneListener only fires when
 *   restzeit <= 0.  So after "All players answered – forcing end of question",
 *   cleanup() ran, the Timeline stopped, and then nothing happened — the screen
 *   was permanently frozen.
 *
 *   Fix: cleanup() now always calls restzeit.set(0) AFTER stopping the Timeline.
 *   That single change triggers the GameController listener and the transition
 *   to AnswerView fires correctly.
 *
 * SECONDARY ISSUE — timer display not updating:
 *   The previous Timeline only decremented the internal restzeit property but
 *   did not update lblZeit directly.  The label was only set once in initFrage().
 *   Fix: the Timeline KeyFrame now updates both restzeit and lblZeit together.
 */
public class QuestionViewController {

    // ── FXML bindings ─────────────────────────────────────────────
    @FXML private Label lblFrage;
    @FXML private Label lblAntwort1;
    @FXML private Label lblAntwort2;
    @FXML private Label lblAntwort3;
    @FXML private Label lblZeit;

    // ── Back-reference to GameController (injected by preloadViews) ──
    private application.GameController mainController;

    /** Called by GameController.preloadViews() — stores back-reference. */
    public void setMainController(application.GameController mc) {
        this.mainController = mc;
    }

    // ── State ─────────────────────────────────────────────────────
    private final IntegerProperty restzeit = new SimpleIntegerProperty(0);
    private Timeline countdown;

    private int totalPlayers;
    private int answeredPlayers;
    private int maxZeit;
    private Set<Spieler> spielerSet;

    // Listeners we register so we can clean them up again
    private java.util.Map<Spieler, ChangeListener<Number>> playerListeners = new java.util.HashMap<>();

    // ── Public API (called by GameController) ─────────────────────

    /**
     * Sets up the question, attaches per-player answer listeners, and starts
     * the countdown.  Call this AFTER adding the showAnswerSceneListener to
     * getRestzeit() so the listener is already in place when restzeit hits 0.
     */
    public void initFrage(Frage frage, Set<Spieler> spieler, int maxZeit) {
        // Clean up any leftover state from a previous question
        cleanup();

        this.spielerSet    = spieler;
        this.totalPlayers  = spieler.size();
        this.answeredPlayers = 0;
        this.maxZeit       = maxZeit;

        System.out.println(">>> Initializing " + totalPlayers + " players for new question");

        // Populate labels
        lblFrage.setText(frage.getFrage());
        var antworten = frage.getAntworten();
        lblAntwort1.setText(antworten.size() > 0 ? antworten.get(0).getAntwort() : "");
        lblAntwort2.setText(antworten.size() > 1 ? antworten.get(1).getAntwort() : "");
        lblAntwort3.setText(antworten.size() > 2 ? antworten.get(2).getAntwort() : "");

        // Reset and display timer
        restzeit.set(maxZeit);
        lblZeit.setText(String.valueOf(maxZeit));

        // Register a buzzer listener for each player
        for (Spieler s : spieler) {
            s.reset();   // clear last round's answer
            ChangeListener<Number> listener = (obs, oldVal, newVal) -> {
                int answerNr = newVal.intValue();
                if (answerNr > 0) {
                    handlePlayerAnswer(s, answerNr, frage);
                }
            };
            playerListeners.put(s, listener);
            s.getAntwortNr().addListener(listener);
            System.out.println("  → Player " + s.getName() + " ready");
        }

        // Start the countdown Timeline
        // FIX: the KeyFrame updates BOTH restzeit (triggers GameController listener)
        // AND lblZeit (so the on-screen number actually counts down).
        countdown = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> {
                int current = restzeit.get() - 1;
                restzeit.set(current);
                lblZeit.setText(String.valueOf(Math.max(current, 0)));

                if (current <= 0) {
                    // Time's up — cleanup will be triggered by GameController
                    // via the restzeit listener (restzeit is already 0 here).
                    detachPlayerListeners();
                    if (countdown != null) countdown.stop();
                }
            })
        );
        countdown.setCycleCount(maxZeit);
        countdown.play();
    }

    /** Called by GameController to observe when the question ends. */
    public IntegerProperty getRestzeit() {
        return restzeit;
    }

    // ── Internal ──────────────────────────────────────────────────

    private synchronized void handlePlayerAnswer(Spieler spieler, int answerNr, Frage frage) {
        // Guard: ignore if already counted (can fire twice on HIGH then LOW)
        if (!playerListeners.containsKey(spieler)) return;

        System.out.println(">>> handlePlayerAnswer called for " + spieler.getName()
                           + " with answer " + answerNr);

        // Remove listener immediately so duplicate GPIO events don't count twice
        spieler.getAntwortNr().removeListener(playerListeners.remove(spieler));

        // Score = time-based: more points for faster answer
        boolean correct = (answerNr == frage.korrekteAntwortInt());
        if (correct) {
            int points = Math.max(10, restzeit.get() * (maxZeit > 0 ? 200 / maxZeit : 10));
            spieler.addPunkte(points);
            spieler.setRundenpunkte(points);
            System.out.println(spieler.getName() + " answered correctly! Points: " + points);
        } else {
            spieler.setRundenpunkte(0);
            System.out.println(spieler.getName() + " answered incorrectly.");
        }

        answeredPlayers++;
        System.out.println("Answers received: " + answeredPlayers + "/" + totalPlayers);

        if (answeredPlayers >= totalPlayers) {
            System.out.println("All players answered - forcing end of question");
            // FIX: this was the bug — cleanup() was called but restzeit was never
            // set to 0, so GameController's listener never fired and the screen
            // was permanently stuck on the question view.
            cleanup();
        }
    }

    /**
     * Stops the timeline, removes all player listeners, and signals the
     * GameController by setting restzeit to 0.
     *
     * KEY FIX: restzeit.set(0) is called LAST, after everything is torn down,
     * so GameController's showAnswerSceneListener fires and triggers the
     * Platform.runLater(() -> showAnswerScene()) call.
     */
    private void cleanup() {
        System.out.println(">>> QuestionViewController cleanup starting...");

        // Stop the countdown
        if (countdown != null) {
            countdown.stop();
            countdown = null;
            System.out.println("  → Timer cancelled");
        }

        // Detach all remaining player listeners
        detachPlayerListeners();

        System.out.println(">>> QuestionViewController cleanup complete");

        // *** THE FIX ***
        // Set restzeit to 0 AFTER stopping everything.
        // This fires the ChangeListener that GameController attached via
        // questionController.getRestzeit().addListener(showAnswerSceneListener).
        // Without this line, the GameController listener never fires and the
        // screen stays frozen forever after cleanup().
        restzeit.set(0);
    }

    private void detachPlayerListeners() {
        if (spielerSet == null) return;
        for (Spieler s : spielerSet) {
            ChangeListener<Number> l = playerListeners.remove(s);
            if (l != null) s.getAntwortNr().removeListener(l);
        }
    }
}
