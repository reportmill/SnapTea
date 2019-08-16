package snaptea;
import java.util.*;
import org.teavm.jso.browser.Window;
import snap.gfx.*;
import snap.util.FileUtils;
import snap.web.*;

/**
 * A GFXEnv implementation for TeaVM.
 */
public class TVEnv extends GFXEnv {
    
    // The app thread
    static EventThread         _appThread;
    
    // The runs array and start/end
    static Runnable            _theRuns[] = new Runnable[100];
    static int                 _runStart, _runEnd;

    // The shared AWTEnv
    static TVEnv               _shared;

/**
 * Creates a TVEnv.
 */
public TVEnv()
{
    startNewAppThread();
}

/**
 * Returns a list of all system fontnames (excludes any that don't start with capital A-Z).
 */
public String[] getFontNames()  { return _fontNames; }
private static String _fontNames[] = { "Arial", "Arial Bold" };

/**
 * Returns a list of all system family names.
 */
public String[] getFamilyNames()  { return _famNames; }
private static String _famNames[] = { "Arial" };

/**
 * Returns a list of all font names for a given family name.
 */
public String[] getFontNames(String aFamilyName)  { return _fontNames; }

/**
 * Returns a font file for given name.
 */
public FontFile getFontFile(String aName)  { return new TVFontFile(aName); }

/**
 * Creates image from source.
 */
public Image getImage(Object aSource)  { return new TVImage(aSource); }

/**
 * Creates image for width, height and alpha and dpi scale (0 = screen dpi, 1 = 72 dpi, 2 = 144 dpi).
 */
public Image getImageForSizeAndScale(double aWidth, double aHeight, boolean hasAlpha, double aScale)
{
    double scale = aScale<=0? getScreenScale() : aScale;
    return new TVImage(aWidth, aHeight, hasAlpha, scale);
}

/**
 * Returns a sound for given source.
 */
public SoundClip getSound(Object aSource)  { return new TVSoundClip(aSource); }

/**
 * Creates a sound for given source.
 */
public SoundClip createSound()  { return null; }

/**
 * Returns the screen resolution.
 */
public double getScreenResolution()  { return 72; }

/**
 * Returns the screen scale. Usually 1, but could be 2 for HiDPI/Retina displays.
 */
public double getScreenScale()  { return TV.getDevicePixelRatio(); }

/**
 * Tries to open the given file name with the platform reader.
 */
public void openFile(Object aSource)
{
    // Get Java File for source
    if(aSource instanceof WebFile) aSource = ((WebFile)aSource).getJavaFile();
    if(aSource instanceof WebURL) aSource = ((WebURL)aSource).getJavaURL();
    java.io.File file = FileUtils.getFile(aSource);
    
    // Get file name, type, bytes
    String name = file.getName().toLowerCase();
    String type = name.endsWith("pdf")? "application/pdf" : name.endsWith("html")? "text/html" : null;
    byte bytes[] = FileUtils.getBytes(file);
    
    // Create file and URL string
    File fileJS = TV.createFile(bytes, name, type);
    String urls = TV.createURL(fileJS);
    
    // Open
    Window.current().open(urls, "_blank");
    //HTMLAnchorElement anchor = HTMLDocument.current().createElement("a").cast();
    //anchor.setHref(urls); setDownload(anchor, name); anchor.click();
}

//@org.teavm.jso.JSBody(params={ "anAnchor", "aStr" }, script = "anAnchor.download = aStr;")
//static native int setDownload(HTMLAnchorElement anAnchor, String aStr);

/**
 * Tries to open the given URL with the platform reader.
 */
public void openURL(Object aSource)
{
    WebURL url = WebURL.getURL(aSource);
    String urls = url!=null? url.getString() : null; if(urls!=null) urls = urls.replace("!", "");
    System.out.println("Open URL: " + urls);
    Window.current().open(urls, "_blank", "menubar=no");
}

/**
 * Plays a beep.
 */
public void beep()  { }

/**
 * Starts a new app event thread.
 */
public void startNewAppThread()
{
    _appThread = new EventThread();
    _appThread.start();
}

/**
 * Returns the next run from event queue.
 */
static synchronized Runnable getNextEventQueueRun()
{
    // Get next run - if none, reset array start/end vars
    Runnable run = _runEnd>_runStart? _theRuns[_runStart++] : null;
    if(run==null) _runStart = _runEnd = 0;
    return run;
}

/**
 * Adds given run to the event queue.
 */
public static synchronized void runOnAppThread(Runnable aRun)
{
    _theRuns[_runEnd++] = aRun;
    if(_runEnd==1)
        _appThread.wakeUp();
    else if(_runEnd>=_theRuns.length) {
        if(_theRuns.length>500) {
            System.err.println("TVEnv.addToEventQueue: To many events in queue - somthing is broken");
            _runStart = _runEnd = 0; return; }
        System.out.println("TVEnv.addToEventQueue: Increasing runs array to len " + _theRuns.length*2);
        _theRuns = Arrays.copyOf(_theRuns, _theRuns.length*2);
    }
}

/**
 * Returns a shared instance.
 */
public static TVEnv get()
{
    if(_shared!=null) return _shared;
    return _shared = new TVEnv();
}

/**
 * A Thread subclass to run event queue runs.
 */
private static class EventThread extends Thread {
    
    /** Gets a run from event queue and runs it. */
    public synchronized void run()
    {
        // Queue runs forever
        while(true) {
            
            // Get next run, if found, just run
            Runnable run = getNextEventQueueRun();
            if(run!=null) {
                 run.run();
                 if(_appThread!=this)
                     break;
             }
            
            // Otherwise, wait till new run added to queue
            else {
                try { wait(); }
                catch(Exception e) { throw new RuntimeException(e); }
            }
        }
    }
    
    /** Wake up called when event is added to empty queue. */
    public synchronized void wakeUp()  { notify(); }
}

}