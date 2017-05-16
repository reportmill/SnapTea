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
    
    // The last mouse down x/y
    double                _mdx, _mdy;
    
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
    _win = aWin;
    _rview = aWin.getRootView();
}

/**
 * Called when a window is hidden.
 */
public void hideWindow(WindowView aWin)
{
    _windows.remove(aWin);
    _win = _windows.size()>0? _windows.get(_windows.size()-1) : null;
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
 * Called when body gets MouseDown.
 */
public void mouseDown(MouseEvent anEvent)
{
    long time = System.currentTimeMillis();
    _clicks = time - _lastReleaseTime<400? (_clicks+1) : 1; _lastReleaseTime = time;
    ViewEvent event = TVViewEnv.get().createEvent(_rview, anEvent, View.MousePress, null);
    ((TVEvent)event)._ccount = _clicks;
    _mdx = event.getX(); _mdy = event.getY();
    _rview.dispatchEvent(event);
}

/**
 * Called when body gets mouseMove.
 */
public void mouseMove(MouseEvent anEvent)
{
    ViewEvent.Type type = ViewUtils.isMouseDown()? View.MouseDrag : View.MouseMove;
    ViewEvent event = TVViewEnv.get().createEvent(_rview, anEvent, type, null);
    ((TVEvent)event)._ccount = _clicks;
    if(Math.abs(_mdx-event.getX())>4 || Math.abs(_mdx-event.getY())>4) _mdx = _mdy = -9999;
    _rview.dispatchEvent(event);
}

/**
 * Called when body gets mouseUp.
 */
public void mouseUp(MouseEvent anEvent)
{
    ViewEvent event = TVViewEnv.get().createEvent(_rview, anEvent, View.MouseRelease, null);
    ((TVEvent)event)._ccount = _clicks;
    _rview.dispatchEvent(event);
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