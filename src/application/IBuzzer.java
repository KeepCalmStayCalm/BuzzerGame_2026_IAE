package application;

import javafx.beans.property.IntegerProperty;

/**
 * Schnittstelle für alle Buzzer-Typen (Hardware, Maus, Dummy).
 *
 * BUG FIX: the original interface declared three mutable BooleanProperty fields
 * (btnAState, btnBState, btnCState) as interface-level statics. Interface fields
 * are implicitly {@code public static final}, but a SimpleIntegerProperty is a
 * mutable object — all implementors shared the same three instances, which is
 * almost certainly not intentional and would cause inter-buzzer state corruption.
 * Since none of the game code actually reads those fields, they have been removed.
 * If per-instance button states are needed in the future, add getter methods here
 * and implement them in each class separately.
 */
public interface IBuzzer {

	/** Returns the property that holds the last pressed button number (1, 2 or 3; 0 = none). */
	IntegerProperty getAnswer();
}
