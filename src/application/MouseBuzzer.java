package application;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class MouseBuzzer implements IBuzzer {
    
    private IntegerProperty answer = new SimpleIntegerProperty(0);
    
    public MouseBuzzer() {
    }
    
    public IntegerProperty getAnswer() {
        return answer;
    }

    public void setAnswer(int answer) {
        this.answer.setValue(answer);
    }
    
}
