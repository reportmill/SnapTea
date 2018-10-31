package snaptea;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.JSProperty;

/**
 * Wrapper for JavaScript Touch event.
 */
public interface Touch extends Event {

@JSProperty
int getScreenX();

@JSProperty
int getScreenY();

@JSProperty
int getClientX();

@JSProperty
int getClientY();

@JSProperty
int getPageX();

@JSProperty
int getPageY();


}