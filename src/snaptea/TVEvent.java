package snaptea;
import org.teavm.jso.dom.events.KeyboardEvent;
import org.teavm.jso.dom.events.MouseEvent;
import snap.view.*;

/**
 * A custom class.
 */
public class TVEvent extends ViewEvent {

    // The mouse location
    double            _mx = Float.MIN_VALUE, _my = Float.MIN_VALUE;
    
    // The click count
    int               _ccount = -1;

/** Returns the mouse event x. */
public double getX()
{
    if(_mx!=Float.MIN_VALUE || getEvent()==null) return _mx;
    double dx = getView().getWindow().getX();
    _mx = ((MouseEvent)getEvent()).getClientX() - dx;
    return _mx;
}

/** Returns the mouse event y. */
public double getY()
{
    if(_my!=Float.MIN_VALUE || getEvent()==null) return _my;
    double dy = getView().getWindow().getY();
    _my = ((MouseEvent)getEvent()).getClientY() - dy;
    return _my;
}

/** Returns the event keycode. */
public int getKeyCode()
{
    KeyboardEvent kev = (KeyboardEvent)getEvent();
    return kev.getKeyCode();
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
    copy._mx = aX; copy._my = aY; if(aClickCount>0) copy._ccount = aClickCount;
    return copy;
}

/**
 * Returns the event type.
 */
protected Type getTypeImpl()  { return null; }

}