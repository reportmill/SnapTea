package snaptea;
import org.teavm.jso.dom.html.*;
import snap.gfx.Insets;
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
public void show(View aView, double aX, double aY)
{
    // Get root view and canvas
    RootView rview = _win.getRootView();
    TVRootView rviewNtv = (TVRootView)rview.getNative();
    HTMLCanvasElement canvas = rviewNtv._canvas;
    
    // Silly stuff
    View c = rview.getContent();
    if(c instanceof Label || c instanceof ButtonBase) { c.setPadding(4,6,4,6); c.setFont(c.getFont().deriveFont(14));
        Box box = new Box(c); box.setPadding(4,4,4,4); rview.setContent(box); }

    // Set PrefSize
    _win.pack();
    
    // Position window
    _win.setXY(aX,aY);
    
    // Add canvas
    HTMLDocument doc = HTMLDocument.current();
    HTMLBodyElement body = doc.getBody();
    body.appendChild(canvas);
    
    // Set FullScreen from RootView.Content
    _win.setGrowWidth(rview.getContent().isGrowWidth());
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
    RootView rview = _win.getRootView();
    rview.setSize(_win.getWidth() - 8, _win.getHeight() - 8);
    
    // Get Canvas
    TVRootView rviewNtv = (TVRootView)rview.getNative();
    HTMLCanvasElement canvas = rviewNtv._canvas;
    
    // Get canvas x/y
    Insets ins = _win.getInsetsAll();
    int x = (int)Math.round(ins.left + _win.getX());
    int y = (int)Math.round(ins.top + _win.getY());
    
    // Set RootView position full-screen
    canvas.getStyle().setCssText("position:absolute;left:" + x + "px;top:" + y + "px;");
}

}