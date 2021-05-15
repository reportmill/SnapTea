package snaptea;
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
        MouseEvent event = (MouseEvent)getEvent();
        if (event==null) { System.err.println("TVEvent:getPointImp: No Mouse Event"); return new Point(); }
        boolean winMaximized = getView().getWindow().isMaximized();
        double x = winMaximized ? event.getClientX() : TV.getPageX(event); x = Math.round(x);
        double y = winMaximized ? event.getClientY() : TV.getPageY(event); y = Math.round(y);
        Point pt = getView().parentToLocal(x,y, null);
        return pt;
    }

    /**
     * Returns the scroll amount for a wheel event.
     */
    public double getScrollX()
    {
        WheelEvent event = (WheelEvent)getEvent();
        return event.getDeltaX();
    }

    /**
     * Returns the scroll amount for a wheel event.
     */
    public double getScrollY()
    {
        WheelEvent event = (WheelEvent)getEvent();
        return event.getDeltaY();
    }

    /**
     * Returns the event keycode.
     */
    public int getKeyCode()
    {
        KeyboardEvent kev = (KeyboardEvent)getEvent();
        int kcode = kev.getKeyCode();
        if (kcode==13) kcode = 10;
        return kcode;
    }

    /**
     * Returns the event key char.
     */
    public String getKeyString()
    {
        KeyboardEvent kev = (KeyboardEvent)getEvent();
        String str = kev.getKey();
        if (str.length()>1) str = "";
        return str;
    }

    /**
     * Returns whether shift key is down.
     */
    public boolean isShiftDown()
    {
        if (isKeyEvent()) return ((KeyboardEvent)getEvent()).isShiftKey();
        if (isMouseEvent()) return ((MouseEvent)getEvent()).getShiftKey();
        return false;
    }

    /**
     * Returns whether control key is down.
     */
    public boolean isControlDown()
    {
        if (isKeyEvent()) return ((KeyboardEvent)getEvent()).isCtrlKey();
        if (isMouseEvent()) return ((MouseEvent)getEvent()).getCtrlKey();
        return false;
    }

    /**
     * Returns whether alt key is down.
     */
    public boolean isAltDown()
    {
        if (isKeyEvent()) return ((KeyboardEvent)getEvent()).isAltKey();
        if (isMouseEvent()) return ((MouseEvent)getEvent()).getAltKey();
        return false;
    }

    /**
     * Returns whether "meta" key is down (the command key on Mac with no equivalent on Windows).
     */
    public boolean isMetaDown()
    {
        if (isKeyEvent()) return ((KeyboardEvent)getEvent()).isMetaKey();
        if (isMouseEvent()) return ((MouseEvent)getEvent()).getMetaKey();
        return false;
    }

    /**
     * Returns whether shortcut key is pressed.
     */
    public boolean isShortcutDown()
    {
        if (isKeyEvent()) return ((KeyboardEvent)getEvent()).isMetaKey();
        if (isMouseEvent()) return isMetaDown() || isControlDown();
        return false;
    }

    /**
     * Returns whether popup trigger is down.
     */
    public boolean isPopupTrigger()
    {
        return isMouseEvent() && ((MouseEvent)getEvent()).getButton()==MouseEvent.RIGHT_BUTTON;
    }

    /**
     * Returns the event type.
     */
    protected Type getTypeImpl()
    {
        Event event = (Event)getEvent();
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