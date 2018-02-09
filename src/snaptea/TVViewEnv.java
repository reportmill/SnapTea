package snaptea;
import java.util.*;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.events.Event;
import snap.gfx.*;
import snap.view.*;

/**
 * A ViewEnv implementation for TeaVM.
 */
public class TVViewEnv extends ViewEnv {
    
    // The clipboard
    TVClipboard               _clipboard;
    
    // A map of window.setIntervals() return ids
    Map <Runnable,Integer>    _intervalIds = new HashMap();
    
    // A shared instance.
    static TVViewEnv          _shared = new TVViewEnv();

/**
 * Returns whether current thread is event thread.
 */
public boolean isEventThread()  { return true; }

/**
 * Run later.
 */
public void runLater(Runnable aRunnable)
{
    Window.setTimeout(() -> aRunnable.run(), 10);
}

/**
 * Runs given runnable after delay.
 */
public void runDelayed(Runnable aRun, int aDelay, boolean inAppThread)
{
    Window.setTimeout(() -> aRun.run(), aDelay);
}

/**
 * Runs given runnable for given period after given delay with option to run once for every interval, even under load.
 */
public void runIntervals(Runnable aRun, int aPeriod, int aDelay, boolean doAll, boolean inAppThread)
{
    int id = Window.setInterval(() -> aRun.run(), aPeriod);
    _intervalIds.put(aRun, id);
}

/**
 * Runs given runnable for given period after given delay with option to run once for every interval, even under load.
 */
public void stopIntervals(Runnable aRun)
{
    Integer id = _intervalIds.get(aRun);
    if(id!=null)
        Window.clearInterval(id);
}

/**
 * Returns the system clipboard.
 */
public Clipboard getClipboard()  { return _clipboard!=null? _clipboard : (_clipboard=TVClipboard.get()); }

/**
 * Returns a new ViewHelper for given native component.
 */
public ViewHelper createHelper(View aView)
{
    if(aView instanceof RootView) return new TVRootViewHpr();
    if(aView instanceof WindowView) return new TVWindowHpr();
    return null;
}

/**
 * Creates an event for a UI view.
 */
public ViewEvent createEvent(View aView, Object anEvent, ViewEvent.Type aType, String aName)
{
    Event eobj = (Event)anEvent;
    if(eobj==null && aType==null) aType = View.Action; //eobj=new ActionEvent(this,ACTION_PERFORMED,"DefAct")
    
    // Create event, configure and send
    ViewEvent event = new TVEvent(); event.setView(aView); event.setEvent(eobj); event.setType(aType);
    event.setName(aName!=null? aName : aView!=null? aView.getName() : null);
    return event;
}

/**
 * Returns the screen bounds inset to usable area.
 */
public Rect getScreenBoundsInset()  { return new Rect(0,0,1000,1000); }
    
/**
 * Returns a shared instance.
 */
public static TVViewEnv get()  { return _shared; }

/**
 * Sets TVViewEnv as the ViewEnv.
 */
public static void set()  { snap.gfx.GFXEnv.setEnv(TVEnv.get()); ViewEnv.setEnv(get()); }

/**
 * A ViewHelper for RootView + TVRootView.
 */
public static class TVRootViewHpr <T extends TVRootView> extends ViewHelper <T> {

    /** Creates the native. */
    protected T createNative()  { return (T)new TVRootView(); }

    /** Override to set view in RootView. */
    public void setView(View aView)  { super.setView(aView); get().setView(aView); }
    
    /** Sets the cursor. */
    public void setCursor(Cursor aCursor)  { get().setCursor(aCursor); }
    
    /** Registers a view for repaint. */
    public void requestPaint(Rect aRect)  { get().repaint(aRect); }
}

/**
 * A ViewHelper for WindowView + TVWindow.
 */
public static class TVWindowHpr <T extends TVWindow> extends ViewHelper <T> {

    /** Creates the native. */
    protected T createNative()  { return (T)new TVWindow(); }
    
    /** Override to get view as WindowView. */
    public WindowView getView()  { return (WindowView)super.getView(); }
        
    /** Override to set view in RootView. */
    public void setView(View aView)  { super.setView(aView); get().setView((WindowView)aView); }
        
    /** Window method: initializes native window. */
    public void initWindow()  { get().initWindow(); }

    /** Window/Popup method: Shows the window. */
    public void show()  { get().show(); }
    
    /** Window/Popup method: Hides the window. */
    public void hide()  { get().hide(); }
    
    /** Window/Popup method: Order window to front. */
    public void toFront()  { get().toFront(); }
}

}