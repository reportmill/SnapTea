package snaptea;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.html.*;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;

/**
 * A class to represent the WindowView in the browser page.
 */
public class TVWindow {

    // The Window View
    WindowView            _win;
    
    // The element to represent the window
    HTMLElement           _winEmt;
    
    // The RootView
    RootView              _rview;
    
    // The native RootView
    TVRootView            _rviewNtv;
    
    // The parent element
    HTMLElement           _parent;
    
    // A listener for hide
    PropChangeListener    _hideLsnr;
    
    // A listener for browser window resize
    EventListener         _resizeLsnr = null;
    
    // The body overflow value
    String                _bodyOverflow;
    
    // The last top window
    static int            _topWin;
    
    // The paint scale
    public static int     scale = TV.getDevicePixelRatio()==2? 2 : 1;

/**
 * Sets the snap window.
 */
public void setWindow(WindowView aWin)
{
    // Set window and start listening to bounds, Maximized and ActiveCursor changes
    _win = aWin;
    _win.addPropChangeListener(pc -> snapWindowMaximizedChanged(), WindowView.Maximized_Prop);
    _win.addPropChangeListener(pce -> snapWindowBoundsChanged(pce), View.X_Prop, View.Y_Prop,
        View.Width_Prop, View.Height_Prop);
    _win.addPropChangeListener(pc -> snapWindowActiveCursorChanged(), WindowView.ActiveCursor_Prop);
    
    // Create/configure WinEmt, the HTMLElement to hold window and canvas
    _winEmt = HTMLDocument.current().createElement("div");
    _winEmt.getStyle().setProperty("box-sizing", "border-box");
    _winEmt.getStyle().setProperty("background", "#F4F4F4CC");
    
    // Get RootView and TVRootView
    _rview = _win.getRootView();
    _rviewNtv = new TVRootView(); _rviewNtv.setView(_rview);
    
    // Get RootView canvas and add to WinEmt
    HTMLCanvasElement canvas = getCanvas();
    _winEmt.appendChild(canvas);
}

/**
 * Initializes window.
 */
public void initWindow()
{
    if(_rview.getFill()==null) _rview.setFill(ViewUtils.getBackFill());
    if(_rview.getBorder()==null) _rview.setBorder(Color.GRAY, 1);
}

/**
 * Returns the body element.
 */
HTMLBodyElement getBody()  { return HTMLDocument.current().getBody(); }

/**
 * Returns the canvas for the window.
 */
public HTMLCanvasElement getCanvas()  { return _rviewNtv._canvas; }

/**
 * Returns the parent DOM element of this window (WinEmt).
 */
public HTMLElement getParent()  { return _parent; }

/**
 * Sets the parent DOM element of this window (WinEmt).
 */
protected void setParent(HTMLElement aNode)
{
    // If already set, just return
    if(aNode==_parent) return;
    
    // Set new value
    HTMLElement par = _parent; _parent = aNode;
    
    // If null, just remove from old parent and return
    if(aNode==null) {
        if(par!=null) par.removeChild(_winEmt); return; }
    
    // Add WinEmt to given node
    aNode.appendChild(_winEmt);
    
    // If body, configure special
    if(aNode==getBody()) {
        
        // Set body and html height so that document covers the whole browser page
        //HTMLHtmlElement html = HTMLDocument.current().getDocumentElement();
        //HTMLBodyElement body = getBody();
        //html.getStyle().setProperty("height", "100%");
        //body.getStyle().setProperty("min-height", "100%");

        // Configure WinEmt for body
        _winEmt.getStyle().setProperty("position", _win.isMaximized()? "fixed" : "absolute");
        _winEmt.getStyle().setProperty("z-index", String.valueOf(_topWin++));
    }
    
    // If arbitrary element
    else {
        _winEmt.getStyle().setProperty("position", "static");
        _winEmt.getStyle().setProperty("width", "100%");
        _winEmt.getStyle().setProperty("height", "100%");
    }
}

/**
 * Returns the parent DOM element of this window.
 */
private HTMLElement getParentForWin()
{
    // If window is maximized, parent should always be body
    if(_win.isMaximized()) return getBody();
    
    // If window has named element, return that
    String pname = _win.getName();
    if(pname!=null) {
        HTMLDocument doc = HTMLDocument.current();
        HTMLElement par = doc.getElementById(pname);
        if(par!=null)
            return par;
    }
    
    // Default to body
    return getBody();
}

/**
 * Returns whether window is child of body.
 */
private boolean isChildOfBody()  { return getParent()==getBody(); }

/**
 * Resets the parent DOM element and Window/WinEmt bounds.
 */
protected void resetParentAndBounds()
{
    // Get proper parent node and set
    HTMLElement par = getParentForWin();
    setParent(par);

    // If window floating in body, set WinEmt bounds from Window
    if(par==getBody()) {
        if(_win.isMaximized()) _win.setBounds(TV.getViewportBounds());
        snapWindowBoundsChanged(null);
    }
    
    // If window in DOM container element
    else browserWindowSizeChanged();
}

/**
 * Shows window.
 */
public void show()
{
    if(_win.isModal()) showModal();
    else showImpl();
}

/**
 * Shows window.
 */
public void showImpl()
{
    // Make sure WinEmt is in proper parent node with proper bounds
    resetParentAndBounds();
    
    // Add to Screen.Windows
    TVScreen screen = TVScreen.get();
    screen.addWindow(_win);

    // Set Window showing    
    ViewUtils.setShowing(_win, true);
    
    // Start listening to browser window resizes
    if(_resizeLsnr==null) _resizeLsnr = e -> TVEnv.runOnAppThread(() -> browserWindowSizeChanged());
    Window.current().addEventListener("resize", _resizeLsnr);
}

/**
 * Shows modal window.
 */
protected synchronized void showModal()
{
    // Do normal show
    showImpl();
    
    // Register listener to activate current thread on window not showing
    _win.addPropChangeListener(_hideLsnr = pce -> snapWindowShowingChanged(), View.Showing_Prop);
    
    // Start new app thread, since this thread is now tied up until window closes
    TVEnv.get().startNewAppThread();
    
    // Wait until window is hidden
    try { wait(); }
    catch(Exception e) { throw new RuntimeException(e); }
}

/**
 * Called when window changes showing.
 */
synchronized void snapWindowShowingChanged()
{
    _win.removePropChangeListener(_hideLsnr); _hideLsnr = null;
    notify();
}

/**
 * Hides window.
 */
public void hide()
{
    // Remove WinEmt from parent
    setParent(null);
    
    // Remove Window from screen
    TVScreen screen = TVScreen.get();
    screen.removeWindow(_win);
    
    // Set Window not showing
    ViewUtils.setShowing(_win, false);
    
    // Stop listening to browser window resizes
    Window.current().removeEventListener("resize", _resizeLsnr); _resizeLsnr = null;
}

/**
 * Window/Popup method: Order window to front.
 */
public void toFront()
{
    _winEmt.getStyle().setProperty("z-index", String.valueOf(_topWin++));
}

/**
 * Called when browser window resizes.
 */
void browserWindowSizeChanged()
{
    // If Window is child of body, just return
    if(isChildOfBody()) {
        if(_win.isMaximized())
            _win.setBounds(TV.getViewportBounds());
        return;
    }
        
    // Reset window location
    HTMLElement parent = getParent();
    Point off = TV.getOffsetAll(parent);
    _win.setXY(off.x, off.y);
    
    // Reset window size
    int w = parent.getClientWidth(), h = parent.getClientHeight();
    _win.setSize(w,h);
    _win.repaint();
}

/**
 * Called when WindowView has bounds change to sync to WinEmt.
 */
void snapWindowBoundsChanged(PropChange aPC)
{
    // If Window not child of body, just return (parent node changes go to win, not win to parent)
    if(!isChildOfBody()) return;
    
    // Get bounds x, y, width, height and PropChange name
    int x = (int)Math.round(_win.getX()), y = (int)Math.round(_win.getY());
    int w = (int)Math.round(_win.getWidth()), h = (int)Math.round(_win.getHeight());
    String pname = aPC!=null? aPC.getPropName() : null;
    
    // Handle changes
    if(pname==null || pname==View.X_Prop)
        _winEmt.getStyle().setProperty("left", String.valueOf(x) + "px");
    if(pname==null || pname==View.Y_Prop)
        _winEmt.getStyle().setProperty("top", String.valueOf(y) + "px");
    if(pname==null || pname==View.Width_Prop)
        _winEmt.getStyle().setProperty("width", w + "px");
    if(pname==null || pname==View.Height_Prop)
        _winEmt.getStyle().setProperty("height", h + "px");
}

/**
 * Called when WindowView.Maximized is changed.
 */
void snapWindowMaximizedChanged()
{
    // Get body and canvas
    HTMLBodyElement body = getBody();
    HTMLCanvasElement canvas = getCanvas();

    // Handle Maximized on
    if(_win.isMaximized()) {
        
        // Set body overflow to hidden (to get rid of scrollbars)
        _bodyOverflow = body.getStyle().getPropertyValue("overflow");
        body.getStyle().setProperty("overflow", "hidden");
        
        // Set Window/WinEmt padding
        _win.setPadding(5,5,5,5);
        _winEmt.getStyle().setProperty("padding", "5px");
        
        // Add a shadow to canvas
        canvas.getStyle().setProperty("box-shadow", "1px 1px 8px grey");
    }
    
    // Handle Maximized off
    else {
        
        // Restore body overflow
        body.getStyle().setProperty("overflow", _bodyOverflow);
        
        // Clear Window/WinEmt padding
        _win.setPadding(0,0,0,0);
        _winEmt.getStyle().setProperty("padding", null);
        
        // Remove shadow from canvas
        canvas.getStyle().setProperty("box-shadow", null);
    }
    
    // Reset parent and Window/WinEmt bounds
    resetParentAndBounds();
}

/**
 * Sets the cursor.
 */
void snapWindowActiveCursorChanged()
{
    Cursor aCursor = _win.getActiveCursor();
    String cstr = "default";
    if(aCursor==Cursor.DEFAULT) cstr = "default";
    if(aCursor==Cursor.CROSSHAIR) cstr = "crosshair";
    if(aCursor==Cursor.HAND) cstr = "pointer";
    if(aCursor==Cursor.MOVE) cstr = "move";
    if(aCursor==Cursor.TEXT) cstr = "text";
    if(aCursor==Cursor.NONE) cstr = "none";
    if(aCursor==Cursor.N_RESIZE) cstr = "n-resize";
    if(aCursor==Cursor.S_RESIZE) cstr = "s-resize";
    if(aCursor==Cursor.E_RESIZE) cstr = "e-resize";
    if(aCursor==Cursor.W_RESIZE) cstr = "w-resize";
    if(aCursor==Cursor.NE_RESIZE) cstr = "ne-resize";
    if(aCursor==Cursor.NW_RESIZE) cstr = "nw-resize";
    if(aCursor==Cursor.SE_RESIZE) cstr = "se-resize";
    if(aCursor==Cursor.SW_RESIZE) cstr = "sw-resize";
    getCanvas().getStyle().setProperty("cursor", cstr);
}

/**
 * A WindowHpr to map WindowView to TVWindow.
 */
public static class TVWindowHpr extends WindowView.WindowHpr <TVWindow> {

    // The snap Window and TVWindow
    WindowView     _win;
    TVWindow       _winNtv;
    
    /** Creates the native. */
    public WindowView getWindow()  { return _win; }
    
    /** Override to set snap Window in TVWindow. */
    public void setWindow(WindowView aWin)
    {
        _win = aWin;
        _winNtv = new TVWindow(); _winNtv.setWindow(aWin);
    }
        
    /** Returns the native. */
    public TVWindow getNative()  { return _winNtv; }
    
    /** Window method: initializes native window. */
    public void initWindow()  { _winNtv.initWindow(); }

    /** Window/Popup method: Shows the window. */
    public void show()  { _winNtv.show(); }
    
    /** Window/Popup method: Hides the window. */
    public void hide()  { _winNtv.hide(); }
    
    /** Window/Popup method: Order window to front. */
    public void toFront()  { _winNtv.toFront(); }
    
    /** Registers a view for repaint. */
    public void requestPaint(Rect aRect)  { _winNtv._rviewNtv.repaint(aRect); }
}

}