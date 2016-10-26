package snaptea;
import org.teavm.jso.dom.events.KeyboardEvent;
import org.teavm.jso.dom.events.MouseEvent;
import org.teavm.jso.dom.html.*;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;

/**
 * A custom class.
 */
public class TVWindow implements PropChangeListener {

    // The HTMLDocument
    HTMLDocument          _doc = HTMLDocument.current();
    
    // The HTMLDocument
    HTMLBodyElement       _body = _doc.getBody();
    
    // The HTMLCanvas
    HTMLCanvasElement     _canvas;
    
    // The Window View
    WindowView            _wview;
    
    // The root view
    RootView              _rview;
    
    // Painter
    Painter               _pntr;
    
    // The last mouse down x/y
    double                _mdx, _mdy;
    
    // Time of last mouse release
    long                  _lastReleaseTime;
    
    // Last number of clicks
    int                   _clicks;
    
/**
 * Creates a new TVWindow.
 */
public TVWindow()
{
    _canvas = HTMLDocument.current().createElement("canvas").withAttr("width", "20").withAttr("height", "20")
        .withAttr("style", "border:1px solid #EEEEEE;").cast();

    // Add Mouse listeners
    _body.addEventListener("mousedown", e -> mouseDown((MouseEvent)e));
    _body.addEventListener("mousemove", e -> mouseMove((MouseEvent)e));
    _body.addEventListener("mouseup", e -> mouseUp((MouseEvent)e));
    
    // Add Key Listeners
    _body.addEventListener("keydown", e -> keyDown((KeyboardEvent)e));
    _body.addEventListener("keypress", e -> keyPress((KeyboardEvent)e));
    _body.addEventListener("keyup", e -> keyUp((KeyboardEvent)e));
    _pntr = new TVPainter(_canvas);
}

/**
 * Sets the window.
 */
public void setView(WindowView aWin)
{
    _wview = aWin;
    _wview.addPropChangeListener(this);
    
    // Set RootView native stuff
    _rview = aWin.getRootView();
    TVRootView rview = (TVRootView)_rview.getNative();
    rview._canvas = _canvas;
    rview._pntr = _pntr;
}

public void show()
{
    // Center window
    //_wview.setX((int)(_doc.getDocumentElement().getClientWidth() - _wview.getWidth())/2);
    _wview.setXY(10,10);
    
    // Add canvas
    _body.appendChild(_canvas);
}

public void mouseDown(MouseEvent anEvent)
{
    long time = System.currentTimeMillis();
    _clicks = time - _lastReleaseTime<400? (_clicks+1) : 1; _lastReleaseTime = time;
    ViewEvent event = TVViewEnv.get().createEvent(_rview, anEvent, View.MousePressed, null);
    ((TVEvent)event)._ccount = _clicks;
    _mdx = event.getX(); _mdy = event.getY();
    _rview.dispatchEvent(event);
}

public void mouseMove(MouseEvent anEvent)
{
    ViewEvent.Type type = ViewUtils.isMouseDown()? View.MouseDragged : View.MouseMoved;
    ViewEvent event = TVViewEnv.get().createEvent(_rview, anEvent, type, null);
    ((TVEvent)event)._ccount = _clicks;
    if(Math.abs(_mdx-event.getX())>4 || Math.abs(_mdx-event.getY())>4) _mdx = _mdy = -9999;
    _rview.dispatchEvent(event);
}

public void mouseUp(MouseEvent anEvent)
{
    ViewEvent event = TVViewEnv.get().createEvent(_rview, anEvent, View.MouseReleased, null);
    ((TVEvent)event)._ccount = _clicks;
    _rview.dispatchEvent(event);
    
    // If mouse up not far from mouse down, post click event
    if(Math.abs(_mdx-event.getX())<=4 && Math.abs(_mdy-event.getY())<=4) {
        ViewEvent ev2 = TVViewEnv.get().createEvent(_rview, anEvent, View.MouseClicked, null);
        ((TVEvent)ev2)._ccount = _clicks;
        _rview.dispatchEvent(ev2);
    }
}

public void keyDown(KeyboardEvent anEvent)
{
    ViewEvent event = TVViewEnv.get().createEvent(_rview, anEvent, View.KeyPressed, null);
    _rview.dispatchEvent(event);
    anEvent.stopPropagation();
}

public void keyPress(KeyboardEvent anEvent)
{
    ViewEvent event = TVViewEnv.get().createEvent(_rview, anEvent, View.KeyTyped, null);
    _rview.dispatchEvent(event);
    anEvent.stopPropagation();
}

public void keyUp(KeyboardEvent anEvent)
{
    ViewEvent event = TVViewEnv.get().createEvent(_rview, anEvent, View.KeyReleased, null);
    _rview.dispatchEvent(event);
    anEvent.stopPropagation();
}

public void propertyChange(PropChange aPC)
{
    String pname = aPC.getPropertyName();
    switch(pname) {
        case View.X_Prop: case View.Y_Prop: case View.Width_Prop: case View.Height_Prop: boundsChanged(); break;
    }
}

public void boundsChanged()
{
    _canvas.setWidth((int)Math.round(_wview.getWidth()));
    _canvas.setHeight((int)Math.round(_wview.getHeight()));
    _rview.setWidth(_wview.getWidth());
    _rview.setHeight(_wview.getHeight());
    int x = (int)_wview.getX(), y = (int)_wview.getY();
    _canvas.getStyle().setCssText("position:absolute;left:" + x + "px;top:" + y + "px;");
}

/**
 * Console support.
 * <textarea id="console" name="console" type="text" style="width:800px;height:120px;">
 */
public void println(String aStr)
{
    HTMLInputElement console = _doc.getElementById("console").cast();
    String str = console.getValue(); str += aStr + '\n';
    console.setValue(str);
}

}