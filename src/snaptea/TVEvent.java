package snaptea;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.dom.events.*;
import snap.geom.Point;
import snap.view.*;

/**
 * A ViewEvent implementation for TeaVM.
 */
public class TVEvent extends ViewEvent {

    /**
     * Returns the event point from browser mouse event.
     */
    protected Point getPointImpl()
    {
        // Handle MouseEvent
        Event event = (Event) getEvent();
        if (isMouseEvent(event))
            return getPointForMouseEvent((MouseEvent) event);

        // Handle TouchEvent
        if (isTouchEvent())
            return getPointForTouchEvent((TouchEvent) event);

        // Handle unknown event type (Currently called by ViewEvent.copyForView())
        //System.out.println("TVEvent.getPointImpl: Unsupported event type: " + event.getType());
        return new Point();
    }

    /**
     * Returns the event point from browser MouseEvent.
     */
    private Point getPointForMouseEvent(MouseEvent mouseEvent)
    {
        // Get event X/Y and convert to view
        View view = getView();
        boolean winMaximized = view.getWindow().isMaximized();
        double viewX = winMaximized ? mouseEvent.getClientX() : mouseEvent.getPageX(); viewX = Math.round(viewX);
        double viewY = winMaximized ? mouseEvent.getClientY() : mouseEvent.getPageY(); viewY = Math.round(viewY);
        Point point = view.parentToLocal(viewX, viewY, null);
        return point;
    }

    /**
     * Returns the event point from browser TouchEvent.
     */
    private Point getPointForTouchEvent(TouchEvent touchEvent)
    {
        // Get event X/Y and convert to view
        View view = getView();
        boolean winMaximized = view.getWindow().isMaximized();
        double viewX = winMaximized ? touchEvent.getClientX() : touchEvent.getPageX(); viewX = Math.round(viewX);
        double viewY = winMaximized ? touchEvent.getClientY() : touchEvent.getPageY(); viewY = Math.round(viewY);
        Point point = view.parentToLocal(viewX,viewY, null);
        return point;
    }

    /**
     * Returns the scroll amount for a wheel event.
     */
    public double getScrollX()
    {
        MouseEvent mouseEvent = getMouseEvent();
        WheelEvent wheelEvent = (WheelEvent) mouseEvent;
        return wheelEvent.getDeltaX();
    }

    /**
     * Returns the scroll amount for a wheel event.
     */
    public double getScrollY()
    {
        MouseEvent mouseEvent = getMouseEvent();
        WheelEvent wheelEvent = (WheelEvent) mouseEvent;
        return wheelEvent.getDeltaY();
    }

    /**
     * Returns the event keycode.
     */
    public int getKeyCode()
    {
        KeyboardEvent keyboardEvent = getKeyEvent();
        int kcode = keyboardEvent.getKeyCode();
        if (kcode==13) kcode = 10;
        return kcode;
    }

    /**
     * Returns the event key char.
     */
    public String getKeyString()
    {
        KeyboardEvent keyboardEvent = getKeyEvent();
        String str = keyboardEvent.getKey();
        if (str.length()>1) str = "";
        return str;
    }

    /**
     * Returns whether shift key is down.
     */
    public boolean isShiftDown()
    {
        if (isKeyEvent())
            return getKeyEvent().isShiftKey();
        if (isMouseEvent())
            return getMouseEvent().getShiftKey();
        //if (isTouchEvent())
        //    return getTouchEvent().getShiftKey();
        return false;
    }

    /**
     * Returns whether control key is down.
     */
    public boolean isControlDown()
    {
        if (isKeyEvent())
            return getKeyEvent().isCtrlKey();
        if (isMouseEvent())
            return getMouseEvent().getCtrlKey();
        //if (isTouchEvent())
        //    return getTouchEvent().getCtrlKey();
        return false;
    }

    /**
     * Returns whether alt key is down.
     */
    public boolean isAltDown()
    {
        if (isKeyEvent())
            return getKeyEvent().isAltKey();
        if (isMouseEvent())
            return getMouseEvent().getAltKey();
        //if (isTouchEvent())
        //    return getTouchEvent().getAltKey();
        return false;
    }

    /**
     * Returns whether "meta" key is down (the command key on Mac with no equivalent on Windows).
     */
    public boolean isMetaDown()
    {
        if (isKeyEvent())
            return getKeyEvent().isMetaKey();
        if (isMouseEvent())
            return getMouseEvent().getMetaKey();
        //if (isTouchEvent())
        //    return getTouchEvent().getMetaKey();
        return false;
    }

    /**
     * Returns whether shortcut key is pressed.
     */
    public boolean isShortcutDown()
    {
        if (isKeyEvent())
            return getKeyEvent().isMetaKey();
        if (isMouseEvent())
            return isMetaDown() || isControlDown();
        //if (isTouchEvent())
        //    return isMetaDown() || isControlDown();
        return false;
    }

    /**
     * Returns whether popup trigger is down.
     */
    public boolean isPopupTrigger()
    {
        MouseEvent mouseEvent = getMouseEvent();
        return mouseEvent != null && mouseEvent.getButton() == MouseEvent.RIGHT_BUTTON;
    }

    /**
     * Returns the JSO KeyEvent (or null, if not available).
     */
    private KeyboardEvent getKeyEvent()
    {
        JSObject eventObj = (JSObject) getEvent();
        if (isKeyEvent())
            return (KeyboardEvent) eventObj;
        return null;
    }

    /**
     * Returns the JSO MouseEvent (or null, if not available).
     */
    private MouseEvent getMouseEvent()
    {
        JSObject eventObj = (JSObject) getEvent();
        if (isMouseEvent(eventObj))
            return (MouseEvent) eventObj;
        return null;
    }

    /**
     * Returns whether event is TouchEvent.
     */
    private boolean isTouchEvent()
    {
        Event event = (Event) getEvent();
        String type = event.getType();
        return type.startsWith("touch");
        //return isTouchEvent(eventObj);
    }

    /**
     * Returns the JSO TouchEvent (or null, if not available).
     */
    private TouchEvent getTouchEvent()
    {
        if (isTouchEvent())
            return (TouchEvent) getEvent();
        return null;
    }

    /**
     * Returns whether given object is MouseEvent.
     */
    @JSBody(params={ "anObj" }, script = "return anObj instanceof MouseEvent;")
    private static native boolean isMouseEvent(JSObject anObj);

    /**
     * Returns whether given object is TouchEvent. Safari doesn't know what a TouchEvent is or has AppTouchEvent?
     */
    //@JSBody(params={ "anObj" }, script = "return anObj instanceof TouchEvent;")
    //private static native boolean isTouchEvent(JSObject anObj);

    /**
     * Returns the event type.
     */
    protected Type getTypeImpl()
    {
        Event event = (Event) getEvent();
        String type = event.getType();
        switch(type) {
            case "dragstart": return Type.DragGesture;
            case "dragend": return Type.DragSourceEnd;
            case "dragenter": return Type.DragEnter;
            case "dragexit": return Type.DragExit;
            case "dragover": return Type.DragOver;
            case "drop": return Type.DragDrop;
            default: return null;
        }
    }

    /**
     * Returns the drag Clipboard for this event.
     */
    public Clipboard getClipboard()
    {
        return TVDragboard.getDrag(this);
    }

    /**
     * Called to indicate that drop is accepted.
     */
    public void acceptDrag()
    {
        TVDragboard.getDrag(this).acceptDrag();
    }

    /**
     * Called to indicate that drop is complete.
     */
    public void dropComplete()
    {
        TVDragboard.getDrag(this).dropComplete();
    }
}