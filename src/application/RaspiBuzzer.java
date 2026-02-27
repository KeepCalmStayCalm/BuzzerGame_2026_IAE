package application;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.gpio.digital.DigitalStateChangeListener;
import com.pi4j.io.gpio.digital.PullResistance;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Hardware-Buzzer via Pi4J GPIO (BCM pin numbering).
 * Jeder Buzzer belegt drei GPIO-Pins (A, B, C).
 */
public class RaspiBuzzer implements IBuzzer {

    private final IntegerProperty answer;

    // Per-instance button state properties (fix: were shared statics in IBuzzer)
    private final BooleanProperty btnAState = new SimpleBooleanProperty(false);
    private final BooleanProperty btnBState = new SimpleBooleanProperty(false);
    private final BooleanProperty btnCState = new SimpleBooleanProperty(false);

    private final DigitalInput btnA, btnB, btnC;

    public RaspiBuzzer(Context pi4j, int p1, int p2, int p3) {
        this.btnA   = createButton(pi4j, p1);
        this.btnB   = createButton(pi4j, p2);
        this.btnC   = createButton(pi4j, p3);
        this.answer = new SimpleIntegerProperty(0);

        btnA.addListener(handleButton(1, btnAState));
        btnB.addListener(handleButton(2, btnBState));
        btnC.addListener(handleButton(3, btnCState));
    }

    private DigitalInput createButton(Context pi4j, int pin) {
        return pi4j.create(
            DigitalInput.newConfigBuilder(pi4j)
                .id("pin-" + pin)
                .name("Button-" + pin)
                .address(pin)
                .pull(PullResistance.PULL_DOWN)
                .debounce(3000L)
        );
    }

    private DigitalStateChangeListener handleButton(int buttonNumber, BooleanProperty stateProperty) {
        return event -> {
            boolean pressed = event.state() == DigitalState.HIGH;
            System.out.println("GPIO " + event.source().id() + " → " + event.state());
            // BUG FIX: Pi4J fires on a GPIO background thread; must dispatch to FX thread.
            Platform.runLater(() -> {
                stateProperty.set(pressed);
                if (pressed) answer.set(buttonNumber);
            });
        };
    }

    @Override public IntegerProperty getAnswer()          { return answer; }
    @Override public BooleanProperty btnAStateProperty()  { return btnAState; }
    @Override public BooleanProperty btnBStateProperty()  { return btnBState; }
    @Override public BooleanProperty btnCStateProperty()  { return btnCState; }

    // ── Standalone GPIO test ─────────────────────────────────────
    public static void main(String[] args) throws InterruptedException {
        Context pi4j = Pi4J.newAutoContext();
        System.out.println("Pi4J Provider:");
        pi4j.providers().describe().print(System.out);

        final int pin = 26;
        DigitalInput di = pi4j.create(
            DigitalInput.newConfigBuilder(pi4j)
                .id("pin-" + pin).name("Button-" + pin)
                .address(pin).pull(PullResistance.PULL_DOWN).debounce(1000L)
        );
        di.addListener(ev -> System.out.println("Pin " + pin + ": " + ev.state()));
        System.out.println("Drücke Buzzer-Knopf — Strg+C zum Beenden.");
        while (true) Thread.sleep(1000);
    }
}
