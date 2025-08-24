
package application;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalInputConfig;
import com.pi4j.io.gpio.digital.DigitalStateChangeListener;
import com.pi4j.io.gpio.digital.PullResistance;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class RaspiBuzzer implements IBuzzer {

    private final IntegerProperty answer = new SimpleIntegerProperty();

    private final Context pi4j;
    private final DigitalInput btnA, btnB, btnC;

    public RaspiBuzzer(int p1, int p2, int p3) {
        // Create Pi4J context (can be shared across the app)
        this.pi4j = Pi4J.newAutoContext();

        // Configure buttons
        this.btnA = createButton(pi4j, p1);
        this.btnB = createButton(pi4j, p2);
        this.btnC = createButton(pi4j, p3);

        // Register listeners
        btnA.addListener(handleButton(1));
        btnB.addListener(handleButton(2));
        btnC.addListener(handleButton(3));
    }

    private DigitalInput createButton(Context pi4j, int pin) {
        DigitalInputConfig config = DigitalInput.newConfigBuilder(pi4j)
                .id("pin-" + pin)
                .name("Button " + pin)
                .address(pin)                     // BCM pin number
                .pull(PullResistance.PULL_DOWN)   // internal pull-down resistor
                .build();
        return pi4j.create(config);
    }

    private DigitalStateChangeListener handleButton(int buttonNumber) {
        return event -> {
            boolean pressed = event.state().isHigh();
            System.out.println("GPIO-PIN " + event.source().id() + ": " + event.state());
            if (pressed) {
                answer.set(buttonNumber);
            } else {
                answer.set(0);
            }
        };
    }

    @Override
    public IntegerProperty getAnswer() {
        return answer;
    }

    public void shutdown() {
        pi4j.shutdown();
    }
}