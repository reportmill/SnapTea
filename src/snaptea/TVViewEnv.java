package snaptea;
import java.util.*;
import org.teavm.jso.ajax.XMLHttpRequest;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLSourceElement;
import org.teavm.jso.dom.xml.Element;
import org.teavm.jso.dom.xml.NodeList;
import snap.geom.Rect;
import snap.view.*;

/**
 * A ViewEnv implementation for TeaVM.
 */
public class TVViewEnv extends ViewEnv {
    
    // The clipboard
    private TVClipboard  _clipboard;
    
    // A map of window.setIntervals() return ids
    private Map <Runnable,Integer>  _intervalIds = new HashMap();
    
    // The URLs 
    private static String  _scriptURL, _scriptURLs[];
    
    // A shared instance.
    private static TVViewEnv  _shared;

    /**
     * Constructor.
     */
    public TVViewEnv()
    {
        if (_env==null) {
            _env = _shared = this;
        }
    }

    /**
     * Returns whether current thread is event thread.
     */
    public boolean isEventThread()  { return true; }

    /**
     * Run later.
     */
    public void runLater(Runnable aRun)
    {
        Window.setTimeout(() -> TVEnv.runOnAppThread(aRun), 10);
    }

    /**
     * Runs given runnable after delay.
     */
    public void runDelayed(Runnable aRun, int aDelay, boolean inAppThread)
    {
        Window.setTimeout(() -> TVEnv.runOnAppThread(aRun), aDelay);
    }

    /**
     * Runs given runnable for given period after given delay with option to run once for every interval, even under load.
     */
    public void runIntervals(Runnable aRun, int aPeriod, int aDelay, boolean doAll, boolean inAppThread)
    {
        int id = Window.setInterval(() -> TVEnv.runOnAppThread(aRun), aPeriod);
        _intervalIds.put(aRun, id);
    }

    /**
     * Runs given runnable for given period after given delay with option to run once for every interval, even under load.
     */
    public void stopIntervals(Runnable aRun)
    {
        Integer id = _intervalIds.get(aRun);
        if (id!=null)
            Window.clearInterval(id);
    }

    /**
     * Returns the system clipboard.
     */
    public Clipboard getClipboard()
    {
        if (_clipboard!=null) return _clipboard;
        return _clipboard = TVClipboard.get();
    }

    /**
     * Returns a new ViewHelper for given native component.
     */
    public WindowView.WindowHpr createHelper(View aView)  { return new TVWindow.TVWindowHpr(); }

    /**
     * Creates an event for a UI view.
     */
    public ViewEvent createEvent(View aView, Object anEvent, ViewEvent.Type aType, String aName)
    {
        Event eobj = (Event)anEvent;
        if (eobj==null && aType==null) aType = View.Action; //eobj=new ActionEvent(this,ACTION_PERFORMED,"DefAct")

        // Create event, configure and send
        ViewEvent event = new TVEvent();
        event.setView(aView);
        event.setEvent(eobj);
        event.setType(aType);
        event.setName(aName!=null ? aName : aView!=null? aView.getName() : null);
        return event;
    }

    /**
     * Returns the screen bounds inset to usable area.
     */
    public Rect getScreenBoundsInset()  { return TV.getViewportBounds(); }

    /**
     * Returns the URL string for script.
     */
    public static String[] getScriptRoots()
    {
        // If already set, just return
        if(_scriptURLs!=null) return _scriptURLs;

        // Iterate over script tags
        HTMLDocument doc = HTMLDocument.current();
        NodeList <Element> scripts = doc.getElementsByTagName("script");
        List <String> urls = new ArrayList();
        for (int i=0; i<scripts.getLength(); i++ ) { HTMLSourceElement s = (HTMLSourceElement)scripts.get(i);
            String urlAll = s.getSrc(); if (urlAll==null || urlAll.length()==0) continue;
            int ind = urlAll.lastIndexOf('/'); if (ind<0) continue;
            String url = urlAll.substring(0, ind); if (url.length()<10) continue;
            if (!urls.contains(url))
                urls.add(url);
        }

        // Return urls
        return _scriptURLs = urls.toArray(new String[0]);
    }

    /**
     * Returns the URL string for script.
     */
    public static String getScriptRoot()
    {
        // If already set, just return
        if (_scriptURL!=null) return _scriptURL;

        // Iterate over script roots
        String roots[] = getScriptRoots();
        for (String root : roots) { String url = root + "/index.txt";
            XMLHttpRequest req = XMLHttpRequest.create();
            req.open("GET", url, false);
            req.send();
            if (req.getStatus()==200)
                return _scriptURL = root;
        }

        // Return urls
        System.err.println("TVViewEnv.getScriptRoot: Can't determine root, settling for " + roots[0]);
        return _scriptURL = roots[0];
    }

    /**
     * Returns a shared instance.
     */
    public static TVViewEnv get()
    {
        if (_shared!=null) return _shared;
        return _shared = new TVViewEnv();
    }

    /**
     * Sets TVViewEnv as the ViewEnv.
     */
    public static void set()
    {
        String root = getScriptRoot();
        System.out.println("Script Root: " + root);

        // Set TV adapter classes for GFXEnv and ViewEnv
        TVEnv.get();
        get();
    }
}