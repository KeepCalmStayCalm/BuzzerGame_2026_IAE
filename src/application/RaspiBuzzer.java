
package application;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class RaspiBuzzer implements IBuzzer {
	
	private IntegerProperty answer = new SimpleIntegerProperty();
	
	
	GpioController gpio;
    GpioPinDigitalInput btnA, btnB, btnC;

    
    public RaspiBuzzer(Pin p1, Pin p2, Pin p3){

        gpio = GpioFactory.getInstance();
        
        btnA = gpio.provisionDigitalInputPin(p1, PinPullResistance.PULL_DOWN);
        btnB = gpio.provisionDigitalInputPin(p2, PinPullResistance.PULL_DOWN);
        btnC = gpio.provisionDigitalInputPin(p3, PinPullResistance.PULL_DOWN);
        
	
	
	
	    btnA.addListener(new GpioPinListenerDigital() {
                @Override   
                public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                    // when button is pressed, speed up the blink rate on LED #2
                    System.out.println("GPIO-PIN: "+event.getPin() + ": " + event.getState());
                    btnAState.set(event.getState().isHigh());
                    if(btnA.getState().isHigh()) {
                        getAnswer().setValue(1);
                    }
                    else {
                        getAnswer().setValue(0);
                        
                   
                    }
                }
            });
            
            
    	btnB.addListener(new GpioPinListenerDigital() {
                @Override
                public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                    // when button is pressed, speed up the blink rate on LED #2
                    System.out.println("GPIO-PIN: "+event.getPin() + ": " + event.getState());
                    btnBState.set(event.getState().isHigh());
                    if(btnB.getState().isHigh()) {
                        getAnswer().setValue(2);
                    }
                    else {
                        getAnswer().setValue(0);
                        
                   
                    }
                }
            });
            
            
	    btnC.addListener(new GpioPinListenerDigital() {
                @Override
                public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                    // when button is pressed, speed up the blink rate on LED #2
                    System.out.println("GPIO-PIN: "+event.getPin() + ": " + event.getState());
                    btnCState.set(event.getState().isHigh());
                    if(btnC.getState().isHigh()) {
                        getAnswer().setValue(3);
                    }
                    else {
                        getAnswer().setValue(0);
                        
                   
                    }
                }
            });
	
	}

	@Override
	public IntegerProperty getAnswer() {
		if(answer == null) {
			answer = new SimpleIntegerProperty();
		}
		return answer;
	
	}

}


	
	
	
