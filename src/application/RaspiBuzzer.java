
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

    private final DigitalInput btnA, btnB, btnC;
    
    public static void main(String[] args){
        Context pi4j = Pi4J.newAutoContext();
        RaspiBuzzer demo = new RaspiBuzzer(pi4j, 13,19,26);
        System.out.println("RaspiBuzzer instance created");
        demo.getAnswer().addListener((x, oldVal, newVal) -> {
           System.out.println("Button state changed: "+newVal);  
        });
        
        while (true) {
            try {
                Thread.sleep(500);
            }
            catch (Exception ex) {
                System.out.println("Done");
                pi4j.shutdown();
            }   
        }     
        
    }

    public RaspiBuzzer(Context pi4j, int p1, int p2, int p3) {
        // Configure buttons
        this.btnA = createButton(pi4j, p1);
        this.btnB = createButton(pi4j, p2);
        this.btnC = createButton(pi4j, p3);

        // Register listeners
        btnA.addListener((event)->{
            if (btnA.isHigh()){
                System.out.println("Button A pressed");
                answer.set(1);
            }
        });
        btnB.addListener((event)->{
            if (btnB.isHigh()){
                System.out.println("Button B pressed");
                answer.set(2);
            }
        });
        btnC.addListener((event)->{
            if (btnC.isHigh()){
                System.out.println("Button C pressed");
                answer.set(3);
            }
        });
    }

    private DigitalInput createButton(Context pi4j, int pin) {
        DigitalInputConfig config = DigitalInput.newConfigBuilder(pi4j)
                .id("pin-" + pin)
                .name("Button " + pin)
                .address(pin)                     // BCM pin number
                .pull(PullResistance.PULL_DOWN)   // internal pull-down resistor
                .debounce(3000L)
                .build();
        return pi4j.create(config);
    }

    // i guess this doesnt work as it should...
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

}
