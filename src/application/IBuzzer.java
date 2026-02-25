package application;


import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;


public interface IBuzzer {

	public IntegerProperty getAnswer();
	public BooleanProperty btnAState = new SimpleBooleanProperty();
	public BooleanProperty btnBState = new SimpleBooleanProperty();
	public BooleanProperty btnCState = new SimpleBooleanProperty();
}
