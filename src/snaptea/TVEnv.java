package snaptea;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import org.teavm.jso.browser.Window;
import snap.gfx.*;
import snap.util.FileUtils;
import snap.util.Prefs;
import snap.web.*;

/**
 * A GFXEnv implementation for TeaVM.
 */
public class TVEnv extends GFXEnv {
    
    // The app thread
    static TVEventThread  _appThread;
    
    // The shared AWTEnv
    static TVEnv               _shared;

    // Font names, Family names
    private static String _fontNames[] = {
        "Arial", "Arial Bold", "Arial Italic", "Arial Bold Italic",
        "Times New Roman", "Times New Roman Bold", "Times New Roman Italic", "Times New Roman Bold Italic",
    };
    private static String _famNames[] = { "Arial", "Times New Roman" };

    /**
     * Creates a TVEnv.
     */
    public TVEnv()
    {
        if (_env==null) {
            _env = _shared = this;
            startNewAppThread();
        }
    }

    /**
     * Returns resource for class and path.
     */
    public URL getResource(Class aClass, String aPath)
    {
        TVWebSite site = TVWebSite.get();
        return site.getJavaURL(aClass, aPath);
    }

    /**
     * Returns the root URL classes in Snap Jar as string.
     */
    public String getClassRoot()
    {
        return TVViewEnv.getScriptRoot();
    }

    /**
     * Returns a list of all system fontnames (excludes any that don't start with capital A-Z).
     */
    public String[] getFontNames()  { return _fontNames; }

    /**
     * Returns a list of all system family names.
     */
    public String[] getFamilyNames()  { return _famNames; }

    /**
     * Returns a list of all font names for a given family name.
     */
    public String[] getFontNames(String aFamilyName)
    {
        // Get system fonts and create new list for font family
        String fonts[] = getFontNames();
        List family = new ArrayList();

        // Iterate over fonts
        for(String name : fonts) {

            // If family name is equal to given family name, add font name
            if(name.contains(aFamilyName) && !family.contains(name))
                family.add(name);
        }

        // Get font names as array and sort
        String familyArray[] = (String[])family.toArray(new String[0]);
        Arrays.sort(familyArray);
        return familyArray;
    }

    /**
     * Returns a font file for given name.
     */
    public FontFile getFontFile(String aName)
    {
        return new TVFontFile(aName);
    }

    /**
     * Creates image from source.
     */
    public Image getImage(Object aSource)
    {
        return new TVImage(aSource);
    }

    /**
     * Creates image for width, height and alpha and dpi scale (0 = screen dpi, 1 = 72 dpi, 2 = 144 dpi).
     */
    public Image getImageForSizeAndScale(double aWidth, double aHeight, boolean hasAlpha, double aScale)
    {
        double scale = aScale<=0 ? getScreenScale() : aScale;
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
     * Returns TV prefs.
     */
    @Override
    public Prefs getPrefs(String aName)
    {
        return TVPrefs.get();
    }

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
        if (aSource instanceof WebFile) aSource = ((WebFile)aSource).getJavaFile();
        if (aSource instanceof WebURL) aSource = ((WebURL)aSource).getJavaURL();
        java.io.File file = FileUtils.getFile(aSource);

        // Get file name, type, bytes
        String name = file.getName().toLowerCase();
        String type = name.endsWith("pdf") ? "application/pdf" : name.endsWith("html") ? "text/html" : null;
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
        String urls = url!=null ? url.getString() : null;
        if (urls!=null) urls = urls.replace("!", "");
        System.out.println("Open URL: " + urls);
        Window.current().open(urls, "_blank", "menubar=no");
    }

    /**
     * Plays a beep.
     */
    public void beep()  { }

    /**
     * This is really just here to help with TeaVM.
     */
    public Method getMethod(Class aClass, String aName, Class ... theClasses) throws NoSuchMethodException
    {
        System.err.println("TVEnv.getMethod: Trying to call: " + aClass.getName() + " " + aName);
        return null;
    }

    /**
     * This is really just here to help with TeaVM.
     */
    public void exit(int aValue)  { }

    /**
     * Starts a new app event thread.
     */
    public void startNewAppThread()
    {
        _appThread = new TVEventThread();
        _appThread.start();
    }

    /**
     * Adds given run to the event queue.
     */
    public static final void runOnAppThread(Runnable aRun)
    {
        TVEventThread.runOnAppThread(aRun);
    }

    /**
     * Returns a shared instance.
     */
    public static TVEnv get()
    {
        if (_shared!=null) return _shared;
        return _shared = new TVEnv();
    }
}