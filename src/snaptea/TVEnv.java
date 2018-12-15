package snaptea;
import java.util.*;
import org.teavm.jso.browser.Window;
import snap.gfx.*;
import snap.web.*;

/**
 * A GFXEnv implementation for TeaVM.
 */
public class TVEnv extends GFXEnv {
    
    // The app thread
    Thread            _appThread;
    
    // The runs array and start/end
    Runnable            _theRuns[] = new Runnable[100];
    int                 _runStart, _runEnd;

    // The shared AWTEnv
    static TVEnv             _shared;

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
 * Creates a new image from source.
 */
public Image getImage(Object aSource)  { return new TVImage(aSource); }

/**
 * Creates a new image for width, height and alpha.
 */
public Image getImage(int aWidth, int aHeight, boolean hasAlpha)  { return new TVImage(aWidth,aHeight,hasAlpha); }

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
 * Tries to open the given file name with the platform reader.
 */
public void openFile(Object aSource)  { }

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
 * Runs the event queue.
 */
synchronized void runEventQueue()
{
    // Queue runs forever
    while(true) {
        
        // Get next run, if found, just run
        Runnable run = _runEnd>_runStart? _theRuns[_runStart++] : null; //_theRuns.poll();
        if(run!=null) {
             run.run();
             if(Thread.currentThread()!=_appThread)
                 break;
         }
        
        // Otherwise, wait till new run added to queue
        else {
            _runStart = _runEnd = 0;
            try { wait(); }
            catch(Exception e) { throw new RuntimeException(e); }
        }
    }
}

/**
 * Adds to the event queue.
 */
synchronized void addToEventQueue(Runnable aRun)
{
    _theRuns[_runEnd++] = aRun;
    if(_runEnd==1)
        notify();
    else if(_runEnd>=_theRuns.length) {
        System.out.println("TVEnv.addToEventQueue: Increasing runs array to len " + _theRuns.length*2);
        _theRuns = Arrays.copyOf(_theRuns, _theRuns.length*2);
    }
}

/**
 * Starts a new app thread.
 */
public void startNewAppThread()
{
    _appThread = new Thread(() -> runEventQueue());
    _appThread.start();
}

/**
 * Runs a runnable on app thread.
 */
public static void runOnAppThread(Runnable aRun)  { get().addToEventQueue(aRun); }

/**
 * Returns a shared instance.
 */
public static TVEnv get()
{
    if(_shared!=null) return _shared;
    return _shared = new TVEnv();
}

}