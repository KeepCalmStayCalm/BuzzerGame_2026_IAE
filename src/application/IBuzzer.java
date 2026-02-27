package application;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;

/**
 * Schnittstelle für alle Buzzer-Typen (Hardware, Maus, Dummy).
 *
 * DESIGN FIX: the original interface declared btnAState/btnBState/btnCState as
 * interface-level fields.  Interface fields are implicitly {@code public static
 * final}, meaning every implementor shared the exact same three BooleanProperty
 * objects — pressing A on buzzer 3 overwrote the state visible for buzzer 1 and
 * 2 as well.  The hardware-test panel in EditSettingsViewController therefore
 * could not distinguish which physical buzzer was pressed.
 *
 * Fix: replaced the shared fields with per-instance getter methods.  Each
 * implementing class now owns its own three BooleanProperty instances.
 * EditSettingsViewController must call e.g. buzzer1.btnAStateProperty()
 * instead of the old buzzer1.btnAState field reference.
 */
public interface IBuzzer {

    /** Returns the property holding the last pressed button number (1/2/3; 0 = none). */
    IntegerProperty getAnswer();

    /** Per-instance state of button A (true = currently pressed). */
    BooleanProperty btnAStateProperty();

    /** Per-instance state of button B (true = currently pressed). */
    BooleanProperty btnBStateProperty();

    /** Per-instance state of button C (true = currently pressed). */
    BooleanProperty btnCStateProperty();
}
