package snaptea;
import org.teavm.jso.dom.html.*;
import snap.gfx.Color;
import snap.gfx.Insets;
import snap.util.*;
import snap.view.*;

/**
 * A class to represent the WindowView in the browser page.
 */
public class TVWindow implements PropChangeListener {

    // The Window View
    WindowView            _win;
    
    // The last top window
    static int            _topWin;
    
    // The paint scale
    public static int     scale = 1; //Window.current().getDevicePixelRatio()==2? 2 : 1;
    
/**
 * Sets the window.
 */
public void setView(WindowView aWin)  { _win = aWin; _win.addPropChangeListener(this); }

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
 * Shows window.
 */
public void show()
{
    //if(_win.isModal()) showModal(); else
    showImpl();
}

/**
 * Shows window.
 */
synchronized void showModal()
{
    // Do normal show
    showImpl();
    
    // Register listener to activate current thread on window not showing
    PropChangeListener hideLsnr = pce -> {
        if(_win.isShowing()) return;
        _win.removePropChangeListener(this, View.Showing_Prop);
        notify();
    };
    _win.addPropChangeListener(hideLsnr);
    
    // Wait until window is hidden
    System.out.println("WillWait");
    try { wait(); }
    catch(Exception e) { throw new RuntimeException(e); }
    System.out.println("DidWait");
}

/**
 * Shows window.
 */
public void showImpl()
{
    // Get root view and canvas
    RootView rview = _win.getRootView();
    TVRootView rviewNtv = (TVRootView)rview.getNative();
    HTMLCanvasElement canvas = rviewNtv._canvas;
    
    // Silly stuff
    View c = rview.getContent();
    if(c instanceof Label || c instanceof ButtonBase) { c.setPadding(4,6,4,6); c.setFont(c.getFont().deriveFont(14));
        BoxView box = new BoxView(c); box.setPadding(4,4,4,4); rview.setContent(box); }

    // Set PrefSize
    _win.pack();
    
    // Position window
    _win.setXY(10,10);
    
    // Add canvas
    HTMLDocument doc = HTMLDocument.current();
    HTMLBodyElement body = doc.getBody();
    body.appendChild(canvas);
    canvas.getStyle().setProperty("z-index", String.valueOf(_topWin++));
    
    // Set FullScreen from RootView.Content
    if(rview.getContent().isGrowWidth()) _win.setGrowWidth(true);
    if(_win.isGrowWidth()) {
        _win.setPadding(5,5,5,5); _win.setXY(0,0); }
    boundsChanged();
    
    // Add to screen
    TVScreen screen = TVScreen.get();
    screen.showWindow(_win);

    // Set Window showing    
    _win.setShowing(true);
}

/**
 * Hides window.
 */
public void hide()
{
    // Get root view and canvas
    RootView rview = _win.getRootView();
    TVRootView rviewNtv = (TVRootView)rview.getNative();
    HTMLCanvasElement canvas = rviewNtv._canvas;
    
    // Add canvas
    HTMLDocument doc = HTMLDocument.current();
    HTMLBodyElement body = doc.getBody();
    body.removeChild(canvas);
    
    // Add to screen
    TVScreen screen = TVScreen.get();
    screen.hideWindow(_win);
    
    // Set Window not showing
    _win.setShowing(false);
}

/**
 * Window/Popup method: Order window to front.
 */
public void toFront()
{
    RootView rview = _win.getRootView();
    TVRootView rviewNtv = (TVRootView)rview.getNative();
    HTMLCanvasElement canvas = rviewNtv._canvas;
    canvas.getStyle().setProperty("z-index", String.valueOf(_topWin++));
}

/**
 * Called when WindowView properties change.
 */
public void propertyChange(PropChange aPC)
{
    String pname = aPC.getPropertyName();
    switch(pname) {
        case View.X_Prop: case View.Y_Prop: boundsChanged(); }
}

/**
 * Called when WindowView bounds changes to sync win size to RootView and win location to RootView.Canvas.
 */
public void boundsChanged()
{
    // Get Canvas
    RootView rview = _win.getRootView();
    TVRootView rviewNtv = (TVRootView)rview.getNative();
    HTMLCanvasElement canvas = rviewNtv._canvas;
    
    // Get canvas x/y
    Insets ins = _win.getInsetsAll();
    int x = (int)Math.round(ins.left + _win.getX());
    int y = (int)Math.round(ins.top + _win.getY());

    // Set RootView position full-screen
    canvas.getStyle().setProperty("left", String.valueOf(x) + "px");
    canvas.getStyle().setProperty("top", String.valueOf(y) + "px");
}

}