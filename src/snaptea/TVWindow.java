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
 * Creates a TVWindow.
 */
public TVWindow()
{
    _winEmt = HTMLDocument.current().createElement("div");
    _winEmt.getStyle().setProperty("box-sizing", "border-box");
    _winEmt.getStyle().setProperty("background", "#F4F4F4CC");
}

/**
 * Sets the window.
 */
public void setView(WindowView aWin)
{
    _win = aWin;
    _win.addPropChangeListener(pc -> snapWindowMaximizedChanged(), WindowView.Maximized_Prop);
    _win.addPropChangeListener(pce -> snapWindowBoundsChanged(pce), View.X_Prop, View.Y_Prop,
        View.Width_Prop, View.Height_Prop);
}

/**
 * Initializes window.
 */
public void initWindow()
{
    RootView rview = _win.getRootView();
    if(rview.getFill()==null) rview.setFill(ViewUtils.getBackFill());
    if(rview.getBorder()==null) rview.setBorder(Color.GRAY, 1);
}

/**
 * Returns the body element.
 */
HTMLBodyElement getBody()  { return HTMLDocument.current().getBody(); }

/**
 * Returns the parent DOM element of this window.
 */
public HTMLElement getParent()  { return _parent; }

/**
 * Sets the parent DOM element of this window.
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
        HTMLHtmlElement html = HTMLDocument.current().getDocumentElement();
        HTMLBodyElement body = getBody();
        html.getStyle().setProperty("height", "100%");
        body.getStyle().setProperty("min-height", "100%");

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
 * Resets the parent DOM element and Window/WinEmt bounds.
 */
protected void resetParentAndBounds()
{
    // Get proper parent node and set
    HTMLElement par = getParentForWin();
    setParent(par);

    // If window floating in body, set WinEmt bounds from Window
    if(par==getBody()) {
        if(_win.isMaximized()) _win.setBounds(getMaximizedBounds());
        snapWindowBoundsChanged(null);
    }
    
    // If window in DOM container element
    else browserWindowSizeChanged();
}

/**
 * Returns the canvas for the window.
 */
public HTMLCanvasElement getCanvas()
{
    RootView rview = _win.getRootView();
    TVRootView rviewNtv = (TVRootView)rview.getNative();
    return rviewNtv._canvas;
}

/**
 * Returns whether window is child of body.
 */
public boolean isChildOfBody()  { return getParent()==getBody(); }

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
    // Silly stuff
    RootView rview = _win.getRootView(); View c = rview.getContent();
    if(c instanceof Label || c instanceof ButtonBase) { c.setPadding(4,6,4,6); c.setFont(c.getFont().deriveFont(14));
        BoxView box = new BoxView(c); box.setPadding(4,4,4,4); rview.setContent(box); }

    // Make sure canvas is inside WinEmt
    HTMLCanvasElement canvas = getCanvas();
    if(canvas.getParentNode()==null)
        _winEmt.appendChild(canvas);
        
    // Make sure WinEmt is in proper parent node with proper bounds
    resetParentAndBounds();
    
    // Add to Screen.Windows
    TVScreen screen = TVScreen.get();
    screen.addWindow(_win);

    // Set Window showing    
    _win.setShowing(true);
    
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
    _win.setShowing(false);
    
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
            _win.setBounds(getMaximizedBounds());
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
 * Returns the bounds for a maximized window.
 */
private Rect getMaximizedBounds()
{
    int w = TV.getBrowserWindowWidth();
    int h = TV.getBrowserWindowHeight();
    return new Rect(0,0,w,h);
}

}