package snaptea;
import org.teavm.jso.ajax.XMLHttpRequest;
import org.teavm.jso.dom.html.*;
import snap.gfx.*;
import snap.view.*;

/**
 * A custom class.
 */
public class TVRootView {

    // The RootView
    RootView              _rview;
    
    // The HTMLCanvas
    HTMLCanvasElement     _canvas;
    
    // Painter
    Painter               _pntr;
    
    // Whether root view needs repaint
    boolean               _needsRepaint;

/**
 * Sets the view.
 */
public void setView(View aView)
{
    _rview = (RootView)aView;
    //_rview.setSize(_canvas.getWidth(), _canvas.getHeight());
    
    // Add Mouse listeners
    //_canvas.addEventListener("mousedown", e -> mouseDown((MouseEvent)e));
    //_canvas.addEventListener("mousemove", e -> mouseMove((MouseEvent)e));
    //_canvas.addEventListener("mouseup", e -> mouseUp((MouseEvent)e));
    
    // Add Key Listeners
    //HTMLElement body = HTMLDocument.current().getBody();
    //body.addEventListener("keydown", e -> keyDown((KeyboardEvent)e));
    //body.addEventListener("keypress", e -> keyPress((KeyboardEvent)e));
    //body.addEventListener("keyup", e -> keyUp((KeyboardEvent)e));
    //_pntr = new TVPainter(_canvas);
    
    // Read from server
    //println("TeaVM Console output");
    //new Thread(() -> watchServer()).start();
}

public void repaint(Rect aRect)
{
    if(!_needsRepaint)
        TVViewEnv.get().runLater(() -> paintNow());
    _needsRepaint = true;
}

public void paintNow()
{
    if(_rview.getFill()==null) _pntr.clearRect(0,0,_rview.getWidth(), _rview.getHeight());
    ViewUtils.paintAll(_rview, _pntr);
    _needsRepaint = false;
}

/*public void mouseDown(MouseEvent anEvent)
{
    ViewEvent nevent = TVViewEnv.get().createEvent(_rview, anEvent, View.MousePressed, null);
    _rview.dispatchEvent(nevent);
}

public void mouseMove(MouseEvent anEvent)
{
    ViewEvent.Type type = ViewUtils.isMouseDown()? View.MouseDragged : View.MouseMoved;
    ViewEvent nevent = TVViewEnv.get().createEvent(_rview, anEvent, type, null);
    _rview.dispatchEvent(nevent);
}

public void mouseUp(MouseEvent anEvent)
{
    ViewEvent nevent = TVViewEnv.get().createEvent(_rview, anEvent, View.MouseReleased, null);
    _rview.dispatchEvent(nevent);
}

public void keyDown(KeyboardEvent anEvent)
{
    ViewEvent nevent = TVViewEnv.get().createEvent(_rview, anEvent, View.KeyPressed, null);
    _rview.dispatchEvent(nevent);
    anEvent.stopPropagation();
}

public void keyPress(KeyboardEvent anEvent)
{
    ViewEvent nevent = TVViewEnv.get().createEvent(_rview, anEvent, View.KeyTyped, null);
    _rview.dispatchEvent(nevent);
    anEvent.stopPropagation();
}

public void keyUp(KeyboardEvent anEvent)
{
    ViewEvent nevent = TVViewEnv.get().createEvent(_rview, anEvent, View.KeyReleased, null);
    _rview.dispatchEvent(nevent);
    anEvent.stopPropagation();
}*/

public void watchServer()
{
    while(true) {
        try { Thread.sleep(1000); } catch(Exception e) { }
        XMLHttpRequest xhr = XMLHttpRequest.create();
        xhr.onComplete(() -> handleResponseText(xhr.getResponseText()));
        xhr.open("GET", "file:///private/tmp/draw.txt");
        xhr.setRequestHeader("If-Modified-Since", new java.util.Date().toString());
        xhr.send();
    }
}

public void handleResponseText(String aStr)
{
    if(aStr.equals(_lastStr)) return; _lastStr = aStr;
    String lines[] = aStr.split("\n");
    for(String line : lines) {
        String cmd[] = line.split("\\s"); if(cmd.length==0) continue;
        if(cmd[0].equals("FillRect")) {
            double x = Double.valueOf(cmd[1]), y = Double.valueOf(cmd[2]), w = Double.valueOf(cmd[3]), h = Double.valueOf(cmd[4]);
            _pntr.fillRect(x,y,w,h);
            System.out.println("FillRect " + (int)x + " " + (int)y + " " + (int)w + " " + (int)h);
        }
        else if(cmd[0].equals("SetColor")) {
            Color c = new Color(cmd[1]);
            _pntr.setPaint(c);
        }
        else if(cmd[0].equals("###"))
            break;
    }
}

String _lastStr = "";

}