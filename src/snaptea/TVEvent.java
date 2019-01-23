package snaptea;
import org.teavm.jso.dom.events.*;
import snap.gfx.Point;
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
    if(getEvent() instanceof Touch) return getTouchPoint();
    MouseEvent event = (MouseEvent)getEvent(); if(event==null) { return new Point(); }
    boolean winMaximized = getView().getWindow().isMaximized();
    double x = winMaximized? event.getClientX() : TV.getPageX(event);
    double y = winMaximized? event.getClientY() : TV.getPageY(event);
    Point pt = getView().parentToLocal(x,y, null);
    return pt;
}

/**
 * Returns the event point from browser touch event.
 */
Point getTouchPoint()
{
    Touch event = (Touch)getEvent();
    boolean winMaximized = getView().getWindow().isMaximized();
    double x = winMaximized? event.getClientX() : event.getPageX();
    double y = winMaximized? event.getClientY() : event.getPageY();
    Point pt = getView().parentToLocal(x,y,null);
    return pt;
}

/** Returns the scroll amount for a wheel event. */
public double getScrollX()
{
    WheelEvent event = (WheelEvent)getEvent();
    return event.getDeltaX();
}

/** Returns the scroll amount for a wheel event. */
public double getScrollY()
{
    WheelEvent event = (WheelEvent)getEvent();
    return event.getDeltaY();
}

/** Returns the event keycode. */
public int getKeyCode()
{
    KeyboardEvent kev = (KeyboardEvent)getEvent();
    int kcode = kev.getKeyCode(); if(kcode==13) kcode = 10;
    return kcode;
}

/** Returns the event key char. */
public String getKeyString()
{
    KeyboardEvent kev = (KeyboardEvent)getEvent();
    String str = kev.getKey();
    return str;
}

/** Returns whether shift key is down. */
public boolean isShiftDown()
{
    if(isKeyEvent()) return ((KeyboardEvent)getEvent()).isShiftKey();
    if(isMouseEvent()) return ((MouseEvent)getEvent()).getShiftKey();
    return false;
}

/** Returns whether control key is down. */
public boolean isControlDown()
{
    if(isKeyEvent()) return ((KeyboardEvent)getEvent()).isCtrlKey();
    if(isMouseEvent()) return ((MouseEvent)getEvent()).getCtrlKey();
    return false;
}

/** Returns whether alt key is down. */
public boolean isAltDown()
{
    if(isKeyEvent()) return ((KeyboardEvent)getEvent()).isAltKey();
    if(isMouseEvent()) return ((MouseEvent)getEvent()).getAltKey();
    return false;
}

/** Returns whether "meta" key is down (the command key on Mac with no equivalent on Windows). */
public boolean isMetaDown()
{
    if(isKeyEvent()) return ((KeyboardEvent)getEvent()).isMetaKey();
    if(isMouseEvent()) return ((MouseEvent)getEvent()).getMetaKey();
    return false;
}

/** Returns whether shortcut key is pressed. */
public boolean isShortcutDown()
{
    if(isKeyEvent()) return ((KeyboardEvent)getEvent()).isMetaKey();
    if(isMouseEvent()) return isMetaDown() || isControlDown();
    return false;
}

/** Returns whether popup trigger is down. */
public boolean isPopupTrigger()
{
    return isMouseEvent() && ((MouseEvent)getEvent()).getButton()==MouseEvent.RIGHT_BUTTON;
}

/**
 * Returns the event type.
 */
protected Type getTypeImpl()  { return null; }

}