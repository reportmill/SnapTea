package snaptea;
import java.util.*;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.events.*;
import org.teavm.jso.dom.html.*;
import snap.view.*;

/**
 * A class to work with the browser web page.
 */
public class TVScreen {

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
    // Get Doc and body
    HTMLDocument doc = HTMLDocument.current();
    HTMLBodyElement body = doc.getBody();
    
    // Add Mouse listeners
    body.addEventListener("mousedown", e -> mouseDown((MouseEvent)e));
    body.addEventListener("mousemove", e -> mouseMove((MouseEvent)e));
    body.addEventListener("mouseup", e -> mouseUp((MouseEvent)e));
    body.addEventListener("wheel", e -> mouseWheel((WheelEvent)e));
    
    // Add Key Listeners
    body.addEventListener("keydown", e -> keyDown((KeyboardEvent)e));
    body.addEventListener("keypress", e -> keyPress((KeyboardEvent)e));
    body.addEventListener("keyup", e -> keyUp((KeyboardEvent)e));
    
    // Add Touch Listeners
    body.addEventListener("touchstart", e -> touchStart((TouchEvent)e));
    body.addEventListener("touchmove", e -> touchMove((TouchEvent)e));
    body.addEventListener("touchend", e -> touchEnd((TouchEvent)e));
    
    // Add bounds listener
    Window.current().addEventListener("resize", e -> windowSizeChanged());
}

/**
 * Returns the list of visible windows.
 */
public List <WindowView> getWindows()  { return _windows; }

/**
 * Called when a window is ordered onscreen.
 */
public void addWindow(WindowView aWin)
{
    // Add to list
    _windows.add(aWin);
    
    // If not Popup, make window main window
    if(!(aWin instanceof PopupWindow)) {
        _win = aWin; _rview = aWin.getRootView(); }
}

/**
 * Called when a window is hidden.
 */
public void removeWindow(WindowView aWin)
{
    // Remove window from list
    _windows.remove(aWin);
    
    // Make next window in list main window
    _win = null;
    for(int i=_windows.size()-1;i>=0;i--) { WindowView win = _windows.get(i);
        if(!(win instanceof PopupWindow)) {
            _win = win; break; }}
    _rview = _win!=null? _win.getRootView() : null;
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
    dispatchEvent(rview, event);
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
    dispatchEvent(_mouseDownView, event);
}

/**
 * Called when body gets mouseMove with MouseDown.
 */
public void mouseDrag(MouseEvent anEvent)
{
    ViewEvent event = TVViewEnv.get().createEvent(_mouseDownView, anEvent, View.MouseDrag, null);
    ((TVEvent)event)._ccount = _clicks;
    dispatchEvent(_mouseDownView, event);
}

/**
 * Called when body gets mouseUp.
 */
public void mouseUp(MouseEvent anEvent)
{
    RootView mouseDownView = _mouseDownView; _mouseDownView = null;
    ViewEvent event = TVViewEnv.get().createEvent(mouseDownView, anEvent, View.MouseRelease, null);
    ((TVEvent)event)._ccount = _clicks;
    dispatchEvent(mouseDownView, event);
}

/* Only Y Axis Scrolling has been implemented */
public void mouseWheel(WheelEvent anEvent)
{
    // Get RootView for WheelEvent
    RootView rview = getRootView(anEvent);

    // Dispatch WheelEvent event
    ViewEvent event = TVViewEnv.get().createEvent(rview, anEvent, View.Scroll, null);
    dispatchEvent(rview, event);
    anEvent.stopPropagation();
    anEvent.preventDefault();
}

/**
 * Called when body gets keyDown.
 */
public void keyDown(KeyboardEvent anEvent)
{
    ViewEvent event = TVViewEnv.get().createEvent(_rview, anEvent, View.KeyPress, null);
    dispatchEvent(_rview, event);
    anEvent.stopPropagation();
}

/**
 * Called when body gets keyPress.
 */
public void keyPress(KeyboardEvent anEvent)
{
    ViewEvent event = TVViewEnv.get().createEvent(_rview, anEvent, View.KeyType, null);
    dispatchEvent(_rview, event);
    anEvent.stopPropagation();
}

/**
 * Called when body gets keyUp.
 */
public void keyUp(KeyboardEvent anEvent)
{
    ViewEvent event = TVViewEnv.get().createEvent(_rview, anEvent, View.KeyRelease, null);
    dispatchEvent(_rview, event);
    anEvent.stopPropagation();
}

/**
 * Called when body gets TouchStart.
 */
public void touchStart(TouchEvent anEvent)
{
    anEvent.preventDefault();
    
    Touch touches[] = anEvent.getTouches(); if(touches==null || touches.length==0) return;
    Touch touch = touches[0];
    
    // Get Click count and set MouseDown
    long time = System.currentTimeMillis();
    _clicks = time - _lastReleaseTime<400? (_clicks+1) : 1; _lastReleaseTime = time;
    
    // Get MouseDownView for event
    _mouseDownView = getRootView(touch);
    
    // Dispatch MousePress event
    ViewEvent event = TVViewEnv.get().createEvent(_mouseDownView, touch, View.MousePress, null);
    ((TVEvent)event)._ccount = _clicks;
    dispatchEvent(_mouseDownView, event);
}

/**
 * Called when body gets touchMove.
 */
public void touchMove(TouchEvent anEvent)
{
    anEvent.preventDefault();
    
    Touch touches[] = anEvent.getTouches(); if(touches==null || touches.length==0) return;
    Touch touch = touches[0];
    
    ViewEvent event = TVViewEnv.get().createEvent(_mouseDownView, touch, View.MouseDrag, null);
    ((TVEvent)event)._ccount = _clicks;
    dispatchEvent(_mouseDownView, event);
}

/**
 * Called when body gets touchEnd.
 */
public void touchEnd(TouchEvent anEvent)
{
    anEvent.preventDefault();

    Touch touches[] = anEvent.getChangedTouches(); if(touches==null || touches.length==0) return;
    Touch touch = touches[0];
    
    RootView mouseDownView = _mouseDownView; _mouseDownView = null;
    ViewEvent event = TVViewEnv.get().createEvent(mouseDownView, touch, View.MouseRelease, null);
    ((TVEvent)event)._ccount = _clicks;
    dispatchEvent(mouseDownView, event);
}

/**
 * Called when body gets cut/copy/paste.
 */
/*public void cutCopyPaste(ClipboardEvent anEvent)
{
    String type = anEvent.getType();
    CJClipboard cb = (CJClipboard)Clipboard.get();
    DataTransfer dtrans = anEvent.getClipboardData();
    
    // Handle cut/copy: Load DataTransfer from Clipboard.ClipboardDatas
    if(type.equals("cut") || type.equals("copy")) {
        dtrans.clearData(null);
        for(ClipboardData cdata : cb.getClipboardDatas().values())
            if(cdata.isString())
                dtrans.setData(cdata.getMIMEType(), cdata.getString());
    }
    
    // Handle paste: Update Clipboard.ClipboardDatas from DataTransfer
    else if(type.equals("paste")) {
        cb.clearData();
        for(String typ : dtrans.getTypes())
            cb.addData(typ,dtrans.getData(typ));
    }
    
    // Needed to push changes to system clipboard
    anEvent.preventDefault();
}*/

/**
 * Returns the RootView for an event.
 */
public RootView getRootView(MouseEvent anEvent)  { return getRootView(anEvent.getClientX(), anEvent.getClientY()); }

/**
 * Returns the RootView for an event.
 */
public RootView getRootView(Touch anEvent)  { return getRootView(anEvent.getClientX(), anEvent.getClientY()); }

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
 * Called when screen (browser window) size changes to notify windows.
 */
public void windowSizeChanged()
{
    for(WindowView win : _windows) {
        TVWindow winNtv = (TVWindow)win.getNative();
        winNtv.windowSizeChanged();
    }
}

/**
 * Dispatches an event to given view.
 */
void dispatchEvent(RootView aView, ViewEvent anEvent)
{
    // We really need a proper event queue - but in the meantime, create thread in case it gets blocked by modal
    new Thread() {
        public void run() { aView.dispatchEvent(anEvent); }
    }.start();
}

/**
 * Returns the shared screen.
 */
public static TVScreen get()  { return _screen; }

}