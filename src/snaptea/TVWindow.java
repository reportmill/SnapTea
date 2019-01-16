package snaptea;
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
    
    // The container element
    HTMLElement           _container;
    
    // Whether window canvas is floating above web page (container element not specified)
    boolean               _floating;
    
    // A listener for hide
    PropChangeListener    _hideLsnr;
    
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
    _winEmt.getStyle().setProperty("background", "#BBBBBBCC");
}

/**
 * Sets the window.
 */
public void setView(WindowView aWin)
{
    _win = aWin;
    _win.addPropChangeListener(pc -> snapWindowMaximizedChanged(), WindowView.Maximized_Prop);
    _win.addPropChangeListener(pce -> snapWindowXYChanged(), View.X_Prop, View.Y_Prop);
    _win.addPropChangeListener(pce -> snapWindowSizeChanged(pce), View.Width_Prop, View.Height_Prop);
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
 * Returns the container element for this window.
 */
public HTMLElement getContainer()
{
    // If already set, just return
    if(_container!=null) return _container;

    // Look for container in doc for WindowView name
    HTMLDocument doc = HTMLDocument.current();
    String cname = _win.getName();
    if(cname!=null)
        _container = doc.getElementById(cname);
    
    // If not found, use body
    if(_container==null) { _container = doc.getBody(); _floating = true; }
    return _container;
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
 * Returns whether window canvas floats above web page (container element not specified).
 */
public boolean isFloating()  { return _floating; }

/**
 * Sets whether window canvas floats above web page (container element not specified).
 */
public void setFloating(boolean aValue)
{
    // If value already set, just return
    if(aValue==_floating) return;
    
    // Set value
    _floating = aValue;
    
    // Get body
    HTMLBodyElement body = HTMLDocument.current().getBody();
    
    // If turning on
    if(aValue) {
        _winEmt.getStyle().setProperty("position", _win.isMaximized()? "fixed" : "absolute");
        if(_winEmt.getParentNode()!=body)
            body.appendChild(_winEmt);
        _container = body;
        _winEmt.getStyle().setProperty("z-index", String.valueOf(_topWin++));
        snapWindowXYChanged();
        snapWindowSizeChanged(null);
    }
    
    // If turning off
    else {
        _container = null;
        HTMLElement container = getContainer();
        _winEmt.getStyle().setProperty("position", "static");
        _winEmt.getStyle().setProperty("width", "100%");
        _winEmt.getStyle().setProperty("height", "100%");
        if(container!=body)
            container.appendChild(_winEmt);
    }
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
    // Make sure canvas is inside WinEmt
    HTMLCanvasElement canvas = getCanvas();
    if(canvas.getParentNode()==null)
        _winEmt.appendChild(canvas);
    
    // Silly stuff
    RootView rview = _win.getRootView(); View c = rview.getContent();
    if(c instanceof Label || c instanceof ButtonBase) { c.setPadding(4,6,4,6); c.setFont(c.getFont().deriveFont(14));
        BoxView box = new BoxView(c); box.setPadding(4,4,4,4); rview.setContent(box); }

    // Add canvas to container element
    HTMLElement containerEmt = getContainer();
    containerEmt.appendChild(_winEmt);
    
    // Handle Floating Window: Configure canvas with absolute postion above and listen for WindowView bounds changes
    if(isFloating()) {
        
        // Set WinEmt CSS props for floating
        _winEmt.getStyle().setProperty("position", _win.isMaximized()? "fixed" : "absolute");
        _winEmt.getStyle().setProperty("z-index", String.valueOf(_topWin++));
        
        // Update canvas location/size
        if(_win.isMaximized()) _win.setBounds(getMaximizedBounds());
        snapWindowXYChanged();
        snapWindowSizeChanged(null);
    }
    
    // Handle Not Floating (tied to container content): Size canvas to 100% of container and listen for emt bnds changes
    else {
        
        // Set canvas to always match size of its container
        _winEmt.getStyle().setProperty("width", "100%");
        _winEmt.getStyle().setProperty("height", "100%");
        _winEmt.getStyle().setProperty("box-sizing", "border-box");
        
        // Resize snap window to container size
        browserWindowSizeChanged();
    }
    
    // Add to Screen.Windows
    TVScreen screen = TVScreen.get();
    screen.addWindow(_win);

    // Set Window showing    
    _win.setShowing(true);
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
    // Remove canvas
    HTMLElement container = getContainer();
    container.removeChild(_winEmt);
    
    // Add to screen
    TVScreen screen = TVScreen.get();
    screen.removeWindow(_win);
    
    // Set Window not showing
    _win.setShowing(false);
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
    // If Window.Maximized, reset bounds and return
    if(_win.isMaximized()) {
        _win.setBounds(getMaximizedBounds()); return; }
        
    // If Window floating, just return
    if(isFloating()) return;
        
    // Reset window location
    HTMLElement container = getContainer();
    Point off = TV.getOffsetAll(container);
    _win.setXY(off.x, off.y);
    
    // Reset window size
    int w = container.getClientWidth(), h = container.getClientHeight();
    _win.setSize(w,h);
    _win.repaint();
}

/**
 * Called when WindowView bounds changes to sync win size to RootView and win location to RootView.Canvas.
 */
public void snapWindowXYChanged()
{
    // If not floating, just return (container changes go to win, not win to container)
    if(!isFloating()) return;
    
    // Get WinEmt x/y and set
    int x = (int)Math.round(_win.getX()), y = (int)Math.round(_win.getY());
    _winEmt.getStyle().setProperty("left", String.valueOf(x) + "px");
    _winEmt.getStyle().setProperty("top", String.valueOf(y) + "px");
}

/**
 * Called when WindowView properties change to sync RootView size to canvas.
 */
void snapWindowSizeChanged(PropChange aPC)
{
    // If not floating, just return (container changes go to win, not win to container)
    if(!isFloating()) return;
    
    // Handle Width change
    String pname = aPC!=null? aPC.getPropName() : null;
    if(pname==null || pname==View.Width_Prop) {
        int w = (int)Math.round(_win.getWidth());
        _winEmt.getStyle().setProperty("width", w + "px");
    }
    
    // Handle Height change
    if(pname==null || pname==View.Height_Prop) {
        int h = (int)Math.round(_win.getHeight());
        _winEmt.getStyle().setProperty("height", h + "px");
    }
}

/**
 * Called when WindowView.Maximized is changed.
 */
void snapWindowMaximizedChanged()
{
    // Handle Maximized on
    if(_win.isMaximized()) {
        _win.setPadding(5,5,5,5);
        _winEmt.getStyle().setProperty("padding", "5px");
        getCanvas().getStyle().setProperty("box-shadow", "1px 1px 8px grey");
        setFloating(true);
        _win.setBounds(getMaximizedBounds());
        snapWindowXYChanged();
    }
    
    // Handle Maximized off
    else {
        _win.setPadding(0,0,0,0);
        _winEmt.getStyle().setProperty("padding", null);
        getCanvas().getStyle().setProperty("box-shadow", null);
        setFloating(false);
        browserWindowSizeChanged();
    }
}

/**
 * Returns the bounds for a maximized window.
 */
Rect getMaximizedBounds()
{
    int w = TV.getBrowserWindowWidth();
    int h = TV.getBrowserWindowHeight();
    return new Rect(0,0,w,h);
}

}