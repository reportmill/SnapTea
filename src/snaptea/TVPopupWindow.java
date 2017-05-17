package snaptea;
import org.teavm.jso.dom.html.*;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;

/**
 * A custom class.
 */
public class TVPopupWindow implements PropChangeListener {

    // The Window View
    PopupWindow            _win;
    
/**
 * Sets the window.
 */
public void setView(PopupWindow aWin)  { _win = aWin; _win.addPropChangeListener(this); }

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
    Size size = rview.getPrefSize();
    _win.setSize(size);
    
    // Position window
    Point pt = new Point(aX, aY);
    if(aView!=null) pt = aView.localToParent(null, aX, aY);
    _win.setXY(pt.x,pt.y);
    
    // Add canvas
    HTMLDocument doc = HTMLDocument.current();
    HTMLBodyElement body = doc.getBody();
    body.appendChild(canvas);
    
    // Set FullScreen from RootView.Content
    _win.setGrowWidth(rview.getContent().isGrowWidth());
    boundsChanged();
    
    // Add to screen
    TVScreen screen = TVScreen.get();
    screen.showWindow(_win);

    // Set Window showing    
    ViewUtils.setShowing(_win, true);
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
    
    // Set RootView position full-screen
    if(_win.isGrowWidth())
        canvas.getStyle().setCssText("position:absolute;left:4px;top:4px;");

    // Set RootView position normal
    else {
        int x = (int)_win.getX(), y = (int)_win.getY();
        canvas.getStyle().setCssText("position:absolute;left:" + x + "px;top:" + y + "px;");
    }
}

}