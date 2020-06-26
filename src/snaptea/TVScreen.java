package snaptea;
import java.util.*;

import org.teavm.jso.JSBody;
import org.teavm.jso.dom.events.*;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.html.*;
import snap.view.*;

/**
 * A class to work with the browser web page.
 */
public class TVScreen {

    // The Window hit by last MouseDown
    private WindowView  _mousePressWin;
    
    // The Window hit by last MouseMove (if mouse still down)
    private WindowView  _mouseDownWin;

    // Time of last mouse release
    private long  _lastReleaseTime;
    
    // Last number of clicks
    private int  _clicks;
    
    // The list of open windows
    private List <WindowView>  _windows = new ArrayList<>();
    
    // The current main window
    private WindowView  _win;
    
    // The shared screen object
    private static TVScreen _screen;
    
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

        // Add pointerdown: Used to keep getting events when mousedown goes outside window
        body.addEventListener("pointerdown", lsnr);

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
        // Vars
        Runnable run = null;
        boolean stopProp = false, prevDefault = false;

        // Handle event types
        switch(e.getType()) {
            case "mousedown":
                run = () -> mouseDown((MouseEvent)e);
                _mousePressWin = _mouseDownWin = getWindow((MouseEvent)e);
                if (_mousePressWin==null) return; //stopProp = prevDefault = true;
                break;
            case "mousemove":
                if (_mouseDownWin!=null) run = () -> mouseDrag((MouseEvent)e);
                else run = () -> mouseMove((MouseEvent)e);
                break;
            case "mouseup":
                run = () -> mouseUp((MouseEvent)e);
                if (_mousePressWin==null) return; //stopProp = prevDefault = true;
                break;
            case "click":
            case "contextmenu":
                if (_mousePressWin==null) return;
                stopProp = prevDefault = true;
                break;
            case "wheel":
                if (getWindow((WheelEvent)e)==null) return;
                run = () -> mouseWheel((WheelEvent)e);
                stopProp = prevDefault = true;
                break;
            case "keydown":
                if (_mousePressWin==null) return;
                run = () -> keyDown((KeyboardEvent)e);
                stopProp = prevDefault = true;
                break;
            case "keyup":
                if (_mousePressWin==null) return;
                run = () -> keyUp((KeyboardEvent)e);
                stopProp = prevDefault = true;
                break;
            case "touchstart":
                run = () -> touchStart((TouchEvent)e);
                _mousePressWin = _mouseDownWin = getWindow((TouchEvent)e);
                if (_mousePressWin==null) return;
                stopProp = prevDefault = true;
                break;
            case "touchmove":
                if (_mousePressWin==null) return;
                run = () -> touchMove((TouchEvent)e);
                stopProp = prevDefault = true;
                break;
            case "touchend":
                if (_mousePressWin==null) return;
                run = () -> touchEnd((TouchEvent)e);
                stopProp = prevDefault = true;
                break;
            case "pointerdown":
                setPointerCapture(e);
                break;

            // Unknown
            default: System.err.println("TVScreen.handleEvent: Not handled: " + e.getType()); return;
        }

        // Handle StopPropagation and PreventDefault
        if (stopProp)
            e.stopPropagation();
        if (prevDefault)
            e.preventDefault();

        // Run event
        if (run!=null)
            TVEnv.runOnAppThread(run);
    }

    /**
     * This is used to keep getting events even when mousedown goes outside window.
     */
    @JSBody(params={ "anEvent" }, script = "document.body.setPointerCapture(anEvent.pointerId);")
    public static native void setPointerCapture(Event anEvent);

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
        if (!(aWin instanceof PopupWindow))
            _win = _mousePressWin = aWin;
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
        for (int i=_windows.size()-1;i>=0;i--) { WindowView win = _windows.get(i);
            if (!(win instanceof PopupWindow)) {
                _win = win; break; }}
    }

    /**
     * Called when body gets mouseMove.
     */
    public void mouseMove(MouseEvent anEvent)
    {
        // Get window for MouseEvent
        WindowView win = getWindow(anEvent);
        if (win==null) win = _win;
        if (win==null) return;

        // Dispatch MouseMove event
        ViewEvent event = createEvent(win, anEvent, View.MouseMove, null);
        event.setClickCount(_clicks);
        win.dispatchEvent(event);
    }

    /**
     * Called when body gets MouseDown.
     */
    public void mouseDown(MouseEvent anEvent)
    {
        // Get Click count and set MouseDown
        long time = System.currentTimeMillis();
        _clicks = time - _lastReleaseTime<400 ? (_clicks+1) : 1;
        _lastReleaseTime = time;

        // Get MouseDownWin for event
        _mouseDownWin = getWindow(anEvent);
        if (_mouseDownWin==null) return;

        // Dispatch MousePress event
        ViewEvent event = createEvent(_mouseDownWin, anEvent, View.MousePress, null);
        event.setClickCount(_clicks);
        _mouseDownWin.dispatchEvent(event);
    }

    /**
     * Called when body gets mouseMove with MouseDown.
     */
    public void mouseDrag(MouseEvent anEvent)
    {
        if (_mouseDownWin==null) return;
        ViewEvent event = createEvent(_mouseDownWin, anEvent, View.MouseDrag, null);
        event.setClickCount(_clicks);
        _mouseDownWin.dispatchEvent(event);
    }

    /**
     * Called when body gets mouseUp.
     */
    public void mouseUp(MouseEvent anEvent)
    {
        if (_mouseDownWin==null) return;
        WindowView mouseDownWin = _mouseDownWin; _mouseDownWin = null;
        ViewEvent event = createEvent(mouseDownWin, anEvent, View.MouseRelease, null);
        event.setClickCount(_clicks);
        mouseDownWin.dispatchEvent(event);
    }

    /* Only Y Axis Scrolling has been implemented */
    public void mouseWheel(WheelEvent anEvent)
    {
        // Get window for WheelEvent and dispatch WheelEvent event
        WindowView win = getWindow(anEvent); if (win==null) return;
        ViewEvent event = createEvent(win, anEvent, View.Scroll, null);
        win.dispatchEvent(event); //if (event.isConsumed()) { anEvent.stopPropagation(); anEvent.preventDefault(); }
    }

    /**
     * Called when body gets keyDown.
     */
    public void keyDown(KeyboardEvent anEvent)
    {
        ViewEvent event = createEvent(_win, anEvent, View.KeyPress, null);
        _win.dispatchEvent(event); //anEvent.stopPropagation();

        String str = anEvent.getKey();
        if (str==null || str.length()==0) return;
        if (str.equals("Control") || str.equals("Alt") || str.equals("Meta") || str.equals("Shift")) return;
        if (str.equals("ArrowUp") || str.equals("ArrowDown") || str.equals("ArrowLeft") || str.equals("ArrowRight")) return;
        if (str.equals("Enter") || str.equals("Backspace") || str.equals("Escape")) return;
        keyPress(anEvent);
    }

    /**
     * Called when body gets keyPress.
     */
    public void keyPress(KeyboardEvent anEvent)
    {
        ViewEvent event = createEvent(_win, anEvent, View.KeyType, null);
        _win.dispatchEvent(event); //anEvent.stopPropagation();
    }

    /**
     * Called when body gets keyUp.
     */
    public void keyUp(KeyboardEvent anEvent)
    {
        ViewEvent event = createEvent(_win, anEvent, View.KeyRelease, null);
        _win.dispatchEvent(event); //anEvent.stopPropagation();
    }

    /**
     * Called when body gets TouchStart.
     */
    public void touchStart(TouchEvent anEvent)
    {
        Touch touches[] = anEvent.getTouches(); if (touches==null || touches.length==0) return;
        Touch touch = touches[0];

        // Get Click count and set MouseDown
        long time = System.currentTimeMillis();
        _clicks = time - _lastReleaseTime<400 ? (_clicks+1) : 1; _lastReleaseTime = time;

        // Get MouseDownWin for event
        _mouseDownWin = getWindow(touch);
        if (_mouseDownWin==null) return; //anEvent.preventDefault();

        // Dispatch MousePress event
        ViewEvent event = createEvent(_mouseDownWin, touch, View.MousePress, null);
        event.setClickCount(_clicks);
        _mouseDownWin.dispatchEvent(event);
    }

    /**
     * Called when body gets touchMove.
     */
    public void touchMove(TouchEvent anEvent)
    {
        if (_mouseDownWin==null) return; //anEvent.preventDefault();

        Touch touches[] = anEvent.getTouches(); if (touches==null || touches.length==0) return;
        Touch touch = touches[0];

        ViewEvent event = createEvent(_mouseDownWin, touch, View.MouseDrag, null);
        event.setClickCount(_clicks);
        _mouseDownWin.dispatchEvent(event);
    }

    /**
     * Called when body gets touchEnd.
     */
    public void touchEnd(TouchEvent anEvent)
    {
        if (_mouseDownWin==null) return; //anEvent.preventDefault();

        Touch touches[] = anEvent.getChangedTouches(); if (touches==null || touches.length==0) return;
        Touch touch = touches[0];

        WindowView mouseDownWin = _mouseDownWin; _mouseDownWin = null;
        ViewEvent event = createEvent(mouseDownWin, touch, View.MouseRelease, null);
        event.setClickCount(_clicks);
        mouseDownWin.dispatchEvent(event);
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
        if (type.equals("cut") || type.equals("copy")) {
            dtrans.clearData(null);
            for (ClipboardData cdata : cb.getClipboardDatas().values())
                if (cdata.isString())
                    dtrans.setData(cdata.getMIMEType(), cdata.getString());
        }

        // Handle paste: Update Clipboard.ClipboardDatas from DataTransfer
        else if (type.equals("paste")) {
            cb.clearData();
            for (String typ : dtrans.getTypes())
                cb.addData(typ,dtrans.getData(typ));
        }

        // Needed to push changes to system clipboard
        anEvent.preventDefault();
    }*/

    /**
     * Returns the WindowView for an event.
     */
    public WindowView getWindow(MouseEvent anEvent)  { return getWindow(TV.getPageX(anEvent), TV.getPageY(anEvent)); }

    /**
     * Returns the WindowView for an event.
     */
    public WindowView getWindow(Touch anEvent)  { return getWindow(anEvent.getPageX(), anEvent.getPageY()); }

    /**
     * Returns the WindowView for an event.
     */
    public WindowView getWindow(TouchEvent anEvent)
    {
        Touch touches[] = anEvent.getTouches(); if (touches==null || touches.length==0) return null;
        return getWindow(touches[0]);
    }

    /**
     * Returns the WindowView for an event.
     */
    public WindowView getWindow(int aX, int aY)
    {
        for (int i=_windows.size()-1;i>=0;i--) { WindowView win = _windows.get(i);
            if (win.isMaximized() || win.contains(aX - win.getX(), aY - win.getY()))
                return win; }
        return null;
    }

    /**
     * Creates an Event.
     */
    ViewEvent createEvent(WindowView aWin, Object anEvent, ViewEvent.Type aType, String aName)
    {
        View rootView = aWin.getRootView();
        ViewEvent event = ViewEvent.createEvent(rootView, anEvent, aType, aName);
        return event;
    }

    /**
     * Returns the shared screen.
     */
    public static TVScreen get()
    {
        if (_screen!=null) return _screen;
        return _screen = new TVScreen();
    }
}