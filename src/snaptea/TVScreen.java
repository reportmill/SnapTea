package snaptea;
import java.util.*;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.events.*;
import org.teavm.jso.dom.html.*;
import snap.gfx.Rect;
import snap.view.*;

/**
 * A class to work with the browser web page.
 */
public class TVScreen {

    // The HTMLDocument
    HTMLDocument          _doc = HTMLDocument.current();
    
    // The HTMLDocument
    HTMLBodyElement       _body = _doc.getBody();
    
    // The RootView hit by last MouseDown (if mouse still down)
    RootView              _mouseDownView;
    
    // Time of last mouse release
    long                  _lastReleaseTime;
    
    // Last number of clicks
    int                   _clicks;
    
    // The list of open windows
    List <WindowView>     _windows = new ArrayList();
    
    // The current main window
    WindowView            _win;
    
    // The focused root view
    RootView              _rview;
    
    // The shared screen object
    static TVScreen       _screen = new TVScreen();
    
/**
 * Creates a new TVScreen.
 */
private TVScreen()
{
    // Add Mouse listeners
    _body.addEventListener("mousedown", e -> mouseDown((MouseEvent)e));
    _body.addEventListener("mousemove", e -> mouseMove((MouseEvent)e));
    _body.addEventListener("mouseup", e -> mouseUp((MouseEvent)e));
    _body.addEventListener("wheel", e -> mouseWheel((WheelEvent)e));
    
    // Add Key Listeners
    _body.addEventListener("keydown", e -> keyDown((KeyboardEvent)e));
    _body.addEventListener("keypress", e -> keyPress((KeyboardEvent)e));
    _body.addEventListener("keyup", e -> keyUp((KeyboardEvent)e));
    
    // Add bounds listener
    Window.current().addEventListener("resize", e -> boundsChanged());
}

/**
 * Called when a window is ordered onscreen.
 */
public void showWindow(WindowView aWin)
{
    _windows.add(aWin);
    if(!(aWin instanceof PopupWindow)) {
        _win = aWin; _rview = aWin.getRootView(); }
    if(aWin.isGrowWidth())
        boundsChanged();
}

/**
 * Called when a window is hidden.
 */
public void hideWindow(WindowView aWin)
{
    _windows.remove(aWin);
    _win = null;
    for(int i=_windows.size()-1;i>=0;i--) { WindowView win = _windows.get(i);
        if(!(win instanceof PopupWindow)) {
            _win = win; break; }}
    _rview = _win!=null? _win.getRootView() : null;
}

/**
 * Returns the screen (browser window) bounds.
 */
public Rect getBounds()
{
    int w = Window.current().getInnerWidth();
    int h = Window.current().getInnerHeight();
    return new Rect(0, 0, w, h);
}

/**
 * Called when body gets mouseMove.
 */
public void mouseMove(MouseEvent anEvent)
{
    // If MouseDown, forward to mouseDrag()
    if(_mouseDownView!=null) { mouseDrag(anEvent); return; }
    
    // Get RootView for MouseEvent
    RootView rview = getRootView(anEvent);

    // Dispatch MouseMove event
    ViewEvent event = TVViewEnv.get().createEvent(rview, anEvent, View.MouseMove, null);
    ((TVEvent)event)._ccount = _clicks;
    rview.dispatchEvent(event);
}

/**
 * Called when body gets MouseDown.
 */
public void mouseDown(MouseEvent anEvent)
{
    // Get Click count and set MouseDown
    long time = System.currentTimeMillis();
    _clicks = time - _lastReleaseTime<400? (_clicks+1) : 1; _lastReleaseTime = time;
    
    // Get MouseDownView for event
    _mouseDownView = getRootView(anEvent);
    
    // Dispatch MousePress event
    ViewEvent event = TVViewEnv.get().createEvent(_mouseDownView, anEvent, View.MousePress, null);
    ((TVEvent)event)._ccount = _clicks;
    _mouseDownView.dispatchEvent(event);
}

/**
 * Called when body gets mouseMove with MouseDown.
 */
public void mouseDrag(MouseEvent anEvent)
{
    ViewEvent event = TVViewEnv.get().createEvent(_mouseDownView, anEvent, View.MouseDrag, null);
    ((TVEvent)event)._ccount = _clicks;
    _mouseDownView.dispatchEvent(event);
}

/**
 * Called when body gets mouseUp.
 */
public void mouseUp(MouseEvent anEvent)
{
    RootView mouseDownView = _mouseDownView; _mouseDownView = null;
    ViewEvent event = TVViewEnv.get().createEvent(mouseDownView, anEvent, View.MouseRelease, null);
    ((TVEvent)event)._ccount = _clicks;
    mouseDownView.dispatchEvent(event);
}

/* Only Y Axis Scrolling has been implemented */
public void mouseWheel(WheelEvent anEvent)
{
    // Get RootView for WheelEvent
    RootView rview = getRootView(anEvent);

    // Dispatch WheelEvent event
    ViewEvent event = TVViewEnv.get().createEvent(rview, anEvent, View.Scroll, null);
    rview.dispatchEvent(event);
    anEvent.stopPropagation();
    anEvent.preventDefault();
}

/**
 * Called when body gets keyDown.
 */
public void keyDown(KeyboardEvent anEvent)
{
    ViewEvent event = TVViewEnv.get().createEvent(_rview, anEvent, View.KeyPress, null);
    _rview.dispatchEvent(event);
    anEvent.stopPropagation();
}

/**
 * Called when body gets keyPress.
 */
public void keyPress(KeyboardEvent anEvent)
{
    ViewEvent event = TVViewEnv.get().createEvent(_rview, anEvent, View.KeyType, null);
    _rview.dispatchEvent(event);
    anEvent.stopPropagation();
}

/**
 * Called when body gets keyUp.
 */
public void keyUp(KeyboardEvent anEvent)
{
    ViewEvent event = TVViewEnv.get().createEvent(_rview, anEvent, View.KeyRelease, null);
    _rview.dispatchEvent(event);
    anEvent.stopPropagation();
}

/**
 * Returns the RootView for an event.
 */
public RootView getRootView(MouseEvent anEvent)  { return getRootView(anEvent.getClientX(), anEvent.getClientY()); }

/**
 * Returns the RootView for an event.
 */
public RootView getRootView(int aX, int aY)
{
    for(int i=_windows.size()-1;i>=0;i--) { WindowView wview = _windows.get(i);
        if(wview.contains(aX - wview.getX(), aY - wview.getY()))
            return wview.getRootView(); }
    return _rview;
}

/**
 * Called when screen (browser window) size changes.
 */
public void boundsChanged()
{
    for(WindowView win : _windows)
        if(win.isGrowWidth())
            win.setBounds(getBounds());
}

/**
 * Returns the shared screen.
 */
public static TVScreen get()  { return _screen; }

}