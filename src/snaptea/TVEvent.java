package snaptea;
import org.teavm.jso.dom.events.*;
import snap.gfx.Point;
import snap.view.*;

/**
 * A ViewEvent implementation for TeaVM.
 */
public class TVEvent extends ViewEvent {

    // The mouse location
    double            _mx = Float.MIN_VALUE, _my = Float.MIN_VALUE;
    
    // The click count
    int               _ccount = -1;

/** Returns the mouse event x. */
public double getX()  { if(_mx==Float.MIN_VALUE) setXY(); return _mx; }

/** Returns the mouse event y. */
public double getY()  { if(_my==Float.MIN_VALUE) setXY(); return _my; }

/** Sets the event point from browser mouse event. */
void setXY()
{
    if(getEvent() instanceof Touch)  { setXYTouch(); return; }
    MouseEvent event = (MouseEvent)getEvent(); if(event==null) { _mx = _my = 0; return; }
    boolean winMaximized = getView().getWindow().isMaximized();
    double x = winMaximized? event.getClientX() : TV.getPageX(event);
    double y = winMaximized? event.getClientY() : TV.getPageY(event);
    Point pt = getView().parentToLocal(x,y, null);
    _mx = pt.x; _my = pt.y;
}

/** Sets the event point from browser mouse event. */
void setXYTouch()
{
    Touch event = (Touch)getEvent();
    boolean winMaximized = getView().getWindow().isMaximized();
    double x = winMaximized? event.getClientX() : event.getPageX();
    double y = winMaximized? event.getClientY() : event.getPageY();
    Point pt = getView().parentToLocal(x,y,null);
    _mx = pt.x; _my = pt.y;
}

/** Returns the click count for a mouse event. */
public int getClickCount()  { return _ccount; }

/** Returns the scroll amount for a wheel event. */
public double getScrollX()
{
    WheelEvent event = (WheelEvent)getEvent();
    return _mx = event.getDeltaX();
}

/** Returns the scroll amount for a wheel event. */
public double getScrollY()
{
    WheelEvent event = (WheelEvent)getEvent();
    return _my = event.getDeltaY();
}

/** Returns the event keycode. */
public int getKeyCode()
{
    KeyboardEvent kev = (KeyboardEvent)getEvent();
    int kcode = kev.getKeyCode(); if(kcode==13) kcode = 10;
    return kcode;
}

/** Returns the event key char. */
public char getKeyChar()  { return (char)getKeyCode(); }

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

/**
 * Returns a view event at new point.
 */
public ViewEvent copyForViewPoint(View aView, double aX, double aY, int aClickCount)
{
    String name = getName(); if(name!=null && (name.length()==0 || name.equals(getView().getName()))) name = null;
    TVEvent copy = (TVEvent)TVViewEnv.get().createEvent(aView, getEvent(), getType(), name);
    copy._mx = aX; copy._my = aY; copy._ccount = aClickCount>0? aClickCount : _ccount;
    return copy;
}

/**
 * Returns the event type.
 */
protected Type getTypeImpl()  { return null; }

}