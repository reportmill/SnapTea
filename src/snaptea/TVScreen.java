package snaptea;
import java.util.*;
import org.teavm.jso.dom.events.*;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.html.*;
import snap.view.*;

/**
 * A class to work with the browser web page.
 */
public class TVScreen {

    // The RootView hit by last MouseDown and MouseMove (if mouse still down)
    RootView              _mousePressView, _mouseDownView, _mouseMoveView;
    
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
    static TVScreen       _screen;
    
/**
 * Creates a TVScreen.
 */
private TVScreen()
{
    // Get Doc and body
    HTMLDocument doc = HTMLDocument.current();
    HTMLBodyElement body = doc.getBody();
    
    // Add Mouse listeners
    EventListener lsnr = e -> handleEvent(e);
    body.addEventListener("mousedown", lsnr);
    body.addEventListener("mousemove", lsnr);
    body.addEventListener("mouseup", lsnr);
    body.addEventListener("click", lsnr);
    body.addEventListener("contextmenu", lsnr);
    body.addEventListener("wheel", lsnr);
    
    // Add Key Listeners
    body.addEventListener("keydown", lsnr);
    body.addEventListener("keyup", lsnr);
    
    // Add Touch Listeners
    body.addEventListener("touchstart", lsnr);
    body.addEventListener("touchmove", lsnr);
    body.addEventListener("touchend", lsnr);
}

/**
 * Handles an event.
 */
void handleEvent(Event e)
{
    Runnable run = null;
    boolean stopProp = false, prevDefault = false;
    switch(e.getType()) {
        case "mousedown":
            run = () -> mouseDown((MouseEvent)e);
            _mousePressView = _mouseDownView = getRootView((MouseEvent)e);
            if(_mousePressView==null) return;
            stopProp = prevDefault = true; break;
        case "mousemove":
            run = () -> mouseMove((MouseEvent)e);
            _mouseMoveView = getRootView((MouseEvent)e); break;
        case "mouseup":
            run = () -> mouseUp((MouseEvent)e);
            if(_mousePressView==null) return;
            stopProp = prevDefault = true; break;
        case "click":
        case "contextmenu":
            if(_mousePressView==null) return;
            stopProp = prevDefault = true; break;
        case "wheel":
            if(_mouseMoveView==null) return;
            run = () -> mouseWheel((WheelEvent)e);
            stopProp = prevDefault = true; break;
        case "keydown":
            if(_mousePressView==null) return;
            run = () -> keyDown((KeyboardEvent)e);
            stopProp = prevDefault = true; break;
        case "keyup":
            if(_mousePressView==null) return;
            run = () -> keyUp((KeyboardEvent)e);
            stopProp = prevDefault = true; break;
        case "touchstart":
            run = () -> touchStart((TouchEvent)e);
            _mousePressView = _mouseDownView = getRootView((TouchEvent)e);
            if(_mousePressView==null) return;
            stopProp = prevDefault = true; break;
        case "touchmove":
            if(_mousePressView==null) return;
            run = () -> touchMove((TouchEvent)e);
            _mouseMoveView = getRootView((TouchEvent)e);
            stopProp = prevDefault = true; break;
        case "touchend":
            if(_mousePressView==null) return;
            run = () -> touchEnd((TouchEvent)e);
            stopProp = prevDefault = true; break;
        default:
            System.err.println("TVScreen.handleEvent: Not handled: " + e.getType()); return;
    }
    
    // Handle StopPropagation and PreventDefault
    if(stopProp)
        e.stopPropagation();
    if(prevDefault)
        e.preventDefault();
    
    // Run event
    if(run!=null)
        TVEnv.runOnAppThread(run);
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
    if(rview==null) rview = _rview; if(rview==null) return;

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
    if(_mouseDownView==null) return;
    
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
    if(_mouseDownView==null) return;
    ViewEvent event = TVViewEnv.get().createEvent(_mouseDownView, anEvent, View.MouseDrag, null);
    ((TVEvent)event)._ccount = _clicks;
    dispatchEvent(_mouseDownView, event);
}

/**
 * Called when body gets mouseUp.
 */
public void mouseUp(MouseEvent anEvent)
{
    if(_mouseDownView==null) return;
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
    if(rview==null) return;

    // Dispatch WheelEvent event
    ViewEvent event = TVViewEnv.get().createEvent(rview, anEvent, View.Scroll, null);
    dispatchEvent(rview, event); //if(event.isConsumed()) { anEvent.stopPropagation(); anEvent.preventDefault(); }
}

/**
 * Called when body gets keyDown.
 */
public void keyDown(KeyboardEvent anEvent)
{
    ViewEvent event = TVViewEnv.get().createEvent(_rview, anEvent, View.KeyPress, null);
    dispatchEvent(_rview, event); //anEvent.stopPropagation();
    
    String str = anEvent.getKey();
    if(str==null || str.length()==0 || str.equals("Control") || str.equals("Alt") ||
        str.equals("Meta") || str.equals("Shift")) return;
    keyPress(anEvent);
}

/**
 * Called when body gets keyPress.
 */
public void keyPress(KeyboardEvent anEvent)
{
    ViewEvent event = TVViewEnv.get().createEvent(_rview, anEvent, View.KeyType, null);
    dispatchEvent(_rview, event); //anEvent.stopPropagation();
}

/**
 * Called when body gets keyUp.
 */
public void keyUp(KeyboardEvent anEvent)
{
    ViewEvent event = TVViewEnv.get().createEvent(_rview, anEvent, View.KeyRelease, null);
    dispatchEvent(_rview, event); //anEvent.stopPropagation();
}

/**
 * Called when body gets TouchStart.
 */
public void touchStart(TouchEvent anEvent)
{
    Touch touches[] = anEvent.getTouches(); if(touches==null || touches.length==0) return;
    Touch touch = touches[0];
    
    // Get Click count and set MouseDown
    long time = System.currentTimeMillis();
    _clicks = time - _lastReleaseTime<400? (_clicks+1) : 1; _lastReleaseTime = time;
    
    // Get MouseDownView for event
    _mouseDownView = getRootView(touch);
    if(_mouseDownView==null) return; //anEvent.preventDefault();
    
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
    if(_mouseDownView==null) return; //anEvent.preventDefault();
    
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
    if(_mouseDownView==null) return; //anEvent.preventDefault();

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
public RootView getRootView(MouseEvent anEvent)  { return getRootView(TV.getPageX(anEvent), TV.getPageY(anEvent)); }

/**
 * Returns the RootView for an event.
 */
public RootView getRootView(Touch anEvent)  { return getRootView(anEvent.getPageX(), anEvent.getPageY()); }

/**
 * Returns the RootView for an event.
 */
public RootView getRootView(TouchEvent anEvent)
{
    Touch touches[] = anEvent.getTouches(); if(touches==null || touches.length==0) return null;
    return getRootView(touches[0]);
}

/**
 * Returns the RootView for an event.
 */
public RootView getRootView(int aX, int aY)
{
    for(int i=_windows.size()-1;i>=0;i--) { WindowView wview = _windows.get(i);
        if(wview.contains(aX - wview.getX(), aY - wview.getY()))
            return wview.getRootView(); }
    return null; //_rview;
}

/**
 * Dispatches an event to given view.
 */
void dispatchEvent(RootView aView, ViewEvent anEvent)
{
    // We really need a proper event queue - but in the meantime, create thread in case it gets blocked by modal
    //new Thread(() -> aView.dispatchEvent(anEvent)).start();
    aView.dispatchEvent(anEvent);
}

/**
 * Returns the shared screen.
 */
public static TVScreen get()
{
    if(_screen!=null) return _screen;
    return _screen = new TVScreen();
}

}