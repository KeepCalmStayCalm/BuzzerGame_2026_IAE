package application;

///usr/bin/env jbang "$0" "$@" ; exit $?

//DEPS org.slf4j:slf4j-api:2.0.12
//DEPS org.slf4j:slf4j-simple:2.0.12
//DEPS com.pi4j:pi4j-core:2.8.0
//DEPS com.pi4j:pi4j-plugin-raspberrypi:2.8.0
//DEPS com.pi4j:pi4j-plugin-gpiod:2.8.0
//DEPS org.openjfx:javafx-controls:15.0.1

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalInputProvider;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.gpio.digital.DigitalStateChangeListener;
import com.pi4j.io.gpio.digital.PullResistance;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class RaspiBuzzer implements IBuzzer {

    private final IntegerProperty answer = new SimpleIntegerProperty();

    private final DigitalInput btnA, btnB, btnC;
    
    public static void main(String[] args){
        Context pi4j = Pi4J.newAutoContext();
        
        System.out.println("------------------");
        System.out.println("Pi4J Provider: ");   
        pi4j.providers().describe().print(System.out);
        System.out.println();
        System.out.println("------------------");
        
        
        int pin = 26;
        //RaspiBuzzer demo = new RaspiBuzzer(pi4j, 13,24,26);
        //System.out.println("RaspiBuzzer instance created");
        DigitalInput di26 = pi4j.create(
            DigitalInput.newConfigBuilder(pi4j)
                .id("pin-" + pin)
                .name("Button-" + pin)
                .address(pin)                     // BCM pin number
                .pull(PullResistance.PULL_DOWN)   // internal pull-down resistor
                .debounce(1000L)
                );
        
        di26.addListener(ev -> {
           System.out.println("Button state changed: "+ev.state());  
        });
        
        System.out.println("Pi4J registered GPIOs:");
        pi4j.registry().describe().print(System.out);
        System.out.println();
        System.out.println("------------------");
        
        
        while (true) {
            try {
                Thread.sleep(1000);
                System.out.println("Buzzer Answer: "+di26);
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
        btnA.addListener(handleButton(1));
        btnB.addListener(handleButton(2));
        btnC.addListener(handleButton(3));
    }

    private DigitalInput createButton(Context pi4j, int pin) {
        var config = DigitalInput.newConfigBuilder(pi4j)
                .id("pin-" + pin)
                .name("Button " + pin)
                .address(pin)                     // BCM pin number
                .pull(PullResistance.PULL_DOWN)   // internal pull-down resistor
                .debounce(3000L);
        return pi4j.create(config);
    }

    private DigitalStateChangeListener handleButton(int buttonNumber) {
        return event -> {
            boolean pressed = event.state() == DigitalState.HIGH;
            System.out.println("GPIO-PIN " + event.source().id() + ": " + event.state());
            if (pressed) {
                System.out.println("setting answer number: "+buttonNumber);
                answer.set(buttonNumber);
            } 
        };
    }

    @Override
    public IntegerProperty getAnswer() {
        return answer;
    }

}
