package view;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import application.Antwort;
import application.Frage;
import application.GameController;
import application.MouseBuzzer;
import application.Spieler;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

public class QuestionViewController implements Initializable {

    GameController gameController;
    private Frage frage;
    
    @FXML private Label lblRestzeit;
    @FXML private Label lblFrage;
    @FXML private Label lblAntwort1;
    @FXML private Label lblAntwort2;
    @FXML private Label lblAntwort3;
    @FXML private BorderPane imageRoot;
    @FXML private ImageView image;
    
    private IntegerProperty restzeit;
    private Timer timer;
    private TimerTask timerTask;
    private long timeStart;
    private int maxZeit;
    private int answersReceived = 0;
    private Set<ChangeListener<Number>> answerListeners = new HashSet<>();

    public IntegerProperty getRestzeit() {
        if (restzeit == null) {
            restzeit = new SimpleIntegerProperty(maxZeit);
        }
        return restzeit;
    }
    
    public void setMainController(GameController mainController) {
        this.gameController = mainController;
    }
    
    /**
     * Initialize the question screen with question data and players
     */
    public void initFrage(Frage frage, Set<Spieler> spielerliste, int maxZeit) {
        // Clean up any previous state
        cleanup();
        
        this.frage = frage;
        this.maxZeit = maxZeit;
        this.timeStart = System.currentTimeMillis();
        this.answersReceived = 0;

        // Set question text
        if (lblFrage != null) {
            lblFrage.setText(frage.getFrage());
        }
        
        // Set answer options
        setAnswers(frage.getAntworten());

        // Load question image if available
        loadQuestionImage(frage.getImagePath());
        
        // Initialize timer
        getRestzeit().setValue(maxZeit);
        startTimer();
        
        // Setup players
        initPlayers(spielerliste);
    }
    
    /**
     * Load question image if available
     */
    private void loadQuestionImage(String imagePath) {
        if (image != null && imagePath != null && !imagePath.isEmpty()) {
            try (InputStream is = new FileInputStream(imagePath)) {
                Image img = new Image(is);
                image.setImage(img);
                
                // Bind image size to container
                if (imageRoot != null) {
                    image.fitWidthProperty().bind(imageRoot.widthProperty());
                    image.fitHeightProperty().bind(imageRoot.heightProperty());
                    image.setPreserveRatio(true);
                }
            } catch (Exception e) {
                System.err.println("Bild konnte nicht geladen werden: " + e.getMessage());
            }
        }
    }
    
    /**
     * Initialize players - reset scores and setup answer listeners
     */
    private void initPlayers(Set<Spieler> spielerliste) {
        // Create a copy to avoid concurrent modification
        Set<Spieler> playersCopy = new HashSet<>(spielerliste);
        
        playersCopy.forEach(spieler -> {
            // Reset player state
            spieler.reset();
            spieler.setRundenpunkte(0);
            
            // Create and store answer listener
            ChangeListener<Number> answerListener = new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, 
                                  Number oldValue, Number newValue) {
                    handlePlayerAnswer(spieler, newValue.intValue());
                    // Remove this listener after first answer
                    spieler.getAntwortNr().removeListener(this);
                    answerListeners.remove(this);
                }
            };
            
            // Store listener for cleanup
            answerListeners.add(answerListener);
            spieler.getAntwortNr().addListener(answerListener);

            // Setup mouse click handlers for dev mode
            if (GameController.IS_DEV_MODE && spieler.getBuzzer() instanceof MouseBuzzer) {
                setupMouseClickHandlers((MouseBuzzer) spieler.getBuzzer());
            }
        });
    }
    
    /**
     * Handle a player's answer
     */
    private void handlePlayerAnswer(Spieler spieler, int answerNum) {
        if (answerNum <= 0) return; // Invalid answer
        
        // Calculate points based on speed (if correct)
        if (answerNum == frage.korrekteAntwortInt()) {
            long answerTime = System.currentTimeMillis();
            int timeTaken = (int) (answerTime - timeStart);
            int punkte = Math.max(0, (maxZeit * 1000 - timeTaken) / 100);
            
            spieler.addPunkte(punkte);
            spieler.setRundenpunkte(punkte);
            
            System.out.println(spieler.getName() + " answered correctly! Points: " + punkte);
        } else {
            spieler.setRundenpunkte(0);
            System.out.println(spieler.getName() + " answered incorrectly.");
        }
        
        // Increment answer count
        answersReceived++;
        
        // If all players answered, end immediately
        Platform.runLater(() -> {
            if (answersReceived >= gameController.getSpielerliste().size()) {
                System.out.println("All players answered - ending question");
                endQuestion();
            }
        });
    }
    
    /**
     * Setup mouse click handlers for dev mode
     */
    private void setupMouseClickHandlers(MouseBuzzer mouseBuzzer) {
        if (lblAntwort1 != null) {
            lblAntwort1.setOnMouseClicked(e -> mouseBuzzer.getAnswer().setValue(1));
        }
        if (lblAntwort2 != null) {
            lblAntwort2.setOnMouseClicked(e -> mouseBuzzer.getAnswer().setValue(2));
        }
        if (lblAntwort3 != null) {
            lblAntwort3.setOnMouseClicked(e -> mouseBuzzer.getAnswer().setValue(3));
        }
    }
    
    /**
     * Set answer text labels
     */
    private void setAnswers(List<Antwort> antworten) {
        if (antworten == null || antworten.size() < 3) return;
        
        if (lblAntwort1 != null) lblAntwort1.setText(antworten.get(0).getAntwort());
        if (lblAntwort2 != null) lblAntwort2.setText(antworten.get(1).getAntwort());
        if (lblAntwort3 != null) lblAntwort3.setText(antworten.get(2).getAntwort());
    }
    
    /**
     * Start countdown timer
     */
    private void startTimer() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - timeStart;
                int remainingSeconds = maxZeit - (int) (elapsed / 1000);
                
                Platform.runLater(() -> {
                    getRestzeit().setValue(Math.max(0, remainingSeconds));
                    
                    if (lblRestzeit != null) {
                        lblRestzeit.setText(String.valueOf(getRestzeit().get()));
                    }
                    
                    // End question when time runs out
                    if (remainingSeconds <= 0) {
                        endQuestion();
                    }
                });
            }
        };
        
        timer = new Timer(true); // daemon thread
        timer.scheduleAtFixedRate(timerTask, 0, 100); // Update every 100ms for smooth countdown
    }
    
    /**
     * End the question (called when time runs out or all players answered)
     */
    private void endQuestion() {
        cleanup();
    }
    
    /**
     * Clean up resources
     */
    public void cleanup() {
        // Cancel timer
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
        
        // Remove all answer listeners
        answerListeners.clear();
        
        // Remove mouse click handlers
        if (lblAntwort1 != null) lblAntwort1.setOnMouseClicked(null);
        if (lblAntwort2 != null) lblAntwort2.setOnMouseClicked(null);
        if (lblAntwort3 != null) lblAntwort3.setOnMouseClicked(null);
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialization if needed
    }
}
