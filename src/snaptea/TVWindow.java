package snaptea;
import org.teavm.jso.dom.html.*;
import snap.util.*;
import snap.view.*;

/**
 * A custom class.
 */
public class TVWindow implements PropChangeListener {

    // The Window View
    WindowView            _win;
    
/**
 * Sets the window.
 */
public void setView(WindowView aWin)  { _win = aWin; _win.addPropChangeListener(this); }

/**
 * Shows window.
 */
public void show()
{
    // Position window
    _win.setXY(10,10);
    
    // Get root view and canvas
    RootView rview = _win.getRootView();
    TVRootView rviewNtv = (TVRootView)rview.getNative();
    HTMLCanvasElement canvas = rviewNtv._canvas;
    
    // Add canvas
    HTMLDocument doc = HTMLDocument.current();
    HTMLBodyElement body = doc.getBody();
    body.appendChild(canvas);
    
    // Set Win.GrowWidth from RootView.Content
    _win.setGrowWidth(rview.getContent().isGrowWidth());
    
    // Add to screen
    TVScreen screen = TVScreen.get();
    screen.showWindow(_win);
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
}

/**
 * Called when WindowView properties change.
 */
public void propertyChange(PropChange aPC)
{
    String pname = aPC.getPropertyName();
    switch(pname) {
        case View.X_Prop: case View.Y_Prop: case View.Width_Prop: case View.Height_Prop: boundsChanged(); }
}

/**
 * Called when WindowView bounds changes to sync win size to RootView and win location to RootView.Canvas.
 */
public void boundsChanged()
{
    // Set RootView size
    double w = _win.getWidth(), h = _win.getHeight();
    RootView rview = _win.getRootView();
    rview.setSize(w, h);
    
    // Get Canvas
    TVRootView rviewNtv = (TVRootView)rview.getNative();
    HTMLCanvasElement canvas = rviewNtv._canvas;
    
    // Set RootView position full-screen
    if(_win.isGrowWidth())
        canvas.getStyle().setCssText("position:absolute;left:0px;top:0px;");

    // Set RootView position normal
    else {
        int x = (int)_win.getX(), y = (int)_win.getY();
        canvas.getStyle().setCssText("position:absolute;left:" + x + "px;top:" + y + "px;");
    }
}

}