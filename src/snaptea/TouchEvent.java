package snaptea;
import org.teavm.jso.JSProperty;
import org.teavm.jso.dom.events.Event;

/**
 * Wrapper for JavaScript Touch event.
 */
public interface TouchEvent extends Event {

    //@JSProperty
    default boolean getCtrlKey()  { return false; }

    //@JSProperty
    default boolean getShiftKey()  { return false; }

    //@JSProperty
    default boolean getAltKey()  { return false; }

    //@JSProperty
    default boolean getMetaKey()  { return false; }

    @JSProperty
    Touch[] getTouches();

    @JSProperty
    Touch[] getChangedTouches();

    /**
     * Returns the first touch.
     */
    default Touch getTouch()
    {
        String type = getType();
        boolean isTouchEnd = type.equals("touchend");

        Touch[] touches;
        if (isTouchEnd)
            touches = getChangedTouches();
        else touches = getTouches();

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