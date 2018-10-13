package snaptea;
import org.teavm.jso.browser.Window;
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
 * Sets the window.
 */
public void setView(WindowView aWin)
{
    _win = aWin;
    _win.addPropChangeListener(pc -> windowViewMaximizedChanged(), WindowView.Maximized_Prop);
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

    // Look for container in doc
    HTMLDocument doc = HTMLDocument.current();
    _container = doc.getElementById("container");
    
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
 * Shows window.
 */
public void show()
{
    if(_win.isModal()) showModal();
    else showImpl();
}

/**
 * Shows modal window.
 */
protected synchronized void showModal()
{
    // Do normal show
    showImpl();
    
    // Register listener to activate current thread on window not showing
    _hideLsnr = pce -> { if(_win.isShowing()) return;
        _win.removePropChangeListener(_hideLsnr);
        synchronized(TVWindow.this) { TVWindow.this.notify(); }
    };
    _win.addPropChangeListener(_hideLsnr, View.Showing_Prop);
    
    // Wait until window is hidden
    try { wait(); }
    catch(Exception e) { throw new RuntimeException(e); }
}

/**
 * Shows window.
 */
public void showImpl()
{
    // Get canvas
    HTMLCanvasElement canvas = getCanvas();
    
    // Silly stuff
    RootView rview = _win.getRootView(); View c = rview.getContent();
    if(c instanceof Label || c instanceof ButtonBase) { c.setPadding(4,6,4,6); c.setFont(c.getFont().deriveFont(14));
        BoxView box = new BoxView(c); box.setPadding(4,4,4,4); rview.setContent(box); }

    // Add canvas to container element
    HTMLElement containerEmt = getContainer();
    containerEmt.appendChild(canvas);
    
    // Handle Floating Window: Configure canvas with absolute postion above and listen for WindowView bounds changes
    if(isFloating()) {
        canvas.getStyle().setCssText("position:absolute;border:1px solid #EEEEEE;");
        canvas.getStyle().setProperty("z-index", String.valueOf(_topWin++));
        windowViewSizeChanged(null);
        _win.addPropChangeListener(pce -> windowViewXYChanged(), View.X_Prop, View.Y_Prop);
        _win.addPropChangeListener(pce -> windowViewSizeChanged(pce), View.Width_Prop, View.Height_Prop);
    }
    
    // Handle Not Floating (tied to container content): Size canvas to 100% of container and listen for emt bnds changes
    else {
        
        // Set canvas to always match size of its container
        canvas.getStyle().setProperty("width", "100%");
        canvas.getStyle().setProperty("height", "100%");
        
        // Resize canvas pixel size and window (do whenever window is resized)
        containerResized();
        Window.current().addEventListener("resize", e -> containerResized());
    }
    
    // Add to Screen.Windows
    TVScreen screen = TVScreen.get();
    screen.addWindow(_win);

    // If Maximized, resize to bounds
    if(_win.isMaximized()) {
        _win.setPadding(5,5,5,5);
        _win.setBounds(screen.getBounds());
    }
    windowViewXYChanged();

    // Set Window showing    
    _win.setShowing(true);
}

/**
 * Hides window.
 */
public void hide()
{
    // Get canvas
    HTMLCanvasElement canvas = getCanvas();
    
    // Remove canvas
    HTMLElement container = getContainer();
    container.removeChild(canvas);
    
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
    HTMLCanvasElement canvas = getCanvas();
    canvas.getStyle().setProperty("z-index", String.valueOf(_topWin++));
}

/**
 * Called when Container element is provided and it is resized by web page.
 */
void containerResized()
{
    // Update window location
    HTMLElement container = getContainer();
    Point off = TV.getOffsetAll(container);
    _win.setXY(off.x, off.y);
    
    // Get container width/height (just return if Window already matches)
    int w = container.getClientWidth(), h = container.getClientHeight();
    if(w==(int)_win.getWidth() && h==(int)_win.getHeight()) return;
    
    // Reset canvas and window size
    HTMLCanvasElement canvas = getCanvas();
    canvas.setWidth(w*TVWindow.scale); canvas.setHeight(h*TVWindow.scale);
    _win.setSize(w,h);
}

/**
 * Called when WindowView bounds changes to sync win size to RootView and win location to RootView.Canvas.
 */
public void windowViewXYChanged()
{
    // Get Canvas
    HTMLCanvasElement canvas = getCanvas();
    
    // Get canvas x/y
    Insets ins = _win.getInsetsAll();
    int x = (int)Math.round(ins.left + _win.getX());
    int y = (int)Math.round(ins.top + _win.getY());

    // Set RootView position full-screen
    canvas.getStyle().setProperty("left", String.valueOf(x) + "px");
    canvas.getStyle().setProperty("top", String.valueOf(y) + "px");
}

/**
 * Called when WindowView properties change to sync RootView size to canvas.
 */
public void windowViewSizeChanged(PropChange aPC)
{
    // Get Canvas
    HTMLCanvasElement canvas = getCanvas();
    
    // Handle Width change
    String pname = aPC!=null? aPC.getPropName() : null;
    if(pname==null || pname==View.Width_Prop) {
        int w = (int)Math.round(_win.getWidth()) - (int)_win.getInsetsAll().getWidth();
        canvas.setWidth(w*TVWindow.scale);
        canvas.getStyle().setProperty("width", w + "px");
    }
    
    // Handle Height change
    if(pname==null || pname==View.Height_Prop) {
        int h = (int)Math.round(_win.getHeight()) - (int)_win.getInsetsAll().getHeight();
        canvas.setHeight(h*TVWindow.scale);
        canvas.getStyle().setProperty("height", h + "px");
    }
}

/**
 * Called when WindowView.Maximized is changed.
 */
void windowViewMaximizedChanged()
{
    // Handle Maximized on
    if(_win.isMaximized()) {
        _win.setPadding(5,5,5,5); _win.setBounds(TVScreen.get().getBounds()); }
    
    // Handle Maximized off
    else _win.setPadding(0,0,0,0);
}

}