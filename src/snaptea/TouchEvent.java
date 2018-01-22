package snaptea;
import org.teavm.jso.JSProperty;
import org.teavm.jso.dom.events.Event;

/**
 * Wrapper for JavaScript Touch event.
 */
public interface TouchEvent extends Event {

@JSProperty
Touch[] getTouches();

@JSProperty
Touch[] getChangedTouches();

}