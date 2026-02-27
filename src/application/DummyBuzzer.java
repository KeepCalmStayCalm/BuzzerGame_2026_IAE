package application;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class DummyBuzzer implements IBuzzer{
    private IntegerProperty answer = new SimpleIntegerProperty(0);
    @Override
    public IntegerProperty getAnswer() {
        return answer;
    }

    public DummyBuzzer(int i){
        answer.setValue(i);
    }
    
}
