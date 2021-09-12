package snaptea;
import org.teavm.jso.JSProperty;
import org.teavm.jso.dom.events.Event;

/**
 * Wrapper for JavaScript Touch event.
 */
public interface TouchEvent extends Event {

    @JSProperty
    boolean getCtrlKey();

    @JSProperty
    boolean getShiftKey();

    @JSProperty
    boolean getAltKey();

    @JSProperty
    boolean getMetaKey();

    @JSProperty
    Touch[] getTouches();

    @JSProperty
    Touch[] getChangedTouches();

    /**
     * Returns the first touch.
     */
    default Touch getTouch()
    {
        // Get Touches
        Touch[] touches = getTouches();

        // If at end, see if there no are changed touches
        String type = getType();
        boolean isTouchEnd = type.equals("touchend");
        if (isTouchEnd) {
            Touch[] changedTouches = getChangedTouches();
            if (changedTouches != null && changedTouches.length > 0)
                touches = changedTouches;
        }

        // Get First touch
        Touch touch = touches != null && touches.length > 0 ? touches[0] : null;
        if (touch == null)
            System.err.println("TouchEvent.getTouch: No touches?");

        // Return touch
        return touch;
    }

    default int getScreenX()
    {
        Touch touch = getTouch();
        return touch != null ? touch.getScreenX() : 0;
    }

    default int getScreenY()
    {
        Touch touch = getTouch();
        return touch != null ? touch.getScreenY() : 0;
    }

    default int getClientX()
    {
        Touch touch = getTouch();
        return touch != null ? touch.getClientX() : 0;
    }

    default int getClientY()
    {
        Touch touch = getTouch();
        return touch != null ? touch.getClientY() : 0;
    }

    default int getPageX()
    {
        Touch touch = getTouch();
        return touch != null ? touch.getPageX() : 0;
    }

    default int getPageY()
    {
        Touch touch = getTouch();
        return touch != null ? touch.getPageY() : 0;
    }
}