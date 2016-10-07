package snaptea;
import snap.gfx.*;
import snap.util.*;
import snap.web.*;

/**
 * A custom class.
 */
public class TVEnv extends GFXEnv {

    // The shared AWTEnv
    static TVEnv     _shared = new TVEnv();
    
    // The shared website
    static TVWebSite  _site = new TVWebSite();
    
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
 * Returns a URL for given source.
 */
public WebURL getURL(Object aSource)
{
    if(aSource instanceof WebURL) return (WebURL)aSource;
    if(aSource instanceof String) return new WebURL(aSource, (String)aSource);
    System.out.println("No URL for Source: " + aSource);
    return null;
}

/**
 * Returns a URL for given class and name/path string.
 */
public WebURL getURL(Class aClass, String aName)
{
    String urls = aName;
    if(!urls.startsWith("/")) { urls = '/' + urls;
    //    String cpath = aClass.getName(), cname = aClass.getSimpleName();
    //    System.out.println("ClassPath: " + cpath);
    //    cpath = cpath.substring(0, cpath.length() - cname.length()).replace('.', '/');
    //    urls = cpath + '/' + urls;
    }
    return new WebURL(urls, "http://abc.com" + urls);
}

/**
 * Returns a site for given source URL.
 */
public WebSite getSite(WebURL aSiteURL)
{
    _site.setURL(aSiteURL);
    return _site;
}

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
public void openURL(Object aSource)  { }

/**
 * Plays a beep.
 */
public void beep()  { }

/**
 * Returns the platform preferences object.
 */
public Prefs getPrefs()  { return _prefs; }

/**
 * Sets this JVM to be headless.
 */
public void setHeadless()  { }

/**
 * Returns the platform.
 */
public SnapUtils.Platform getPlatform()  { return SnapUtils.Platform.UNKNOWN; }

/**
 * Returns a key value.
 */
public Object getKeyValue(Object anObj, String aKey)  { return null; }

/**
 * Sets a key value.
 */
public void setKeyValue(Object anObj, String aKey, Object aValue)  { }

/**
 * Returns a key chain value.
 */
public Object getKeyChainValue(Object anObj, String aKeyChain)  { return null; }

/**
 * Sets a key chain value.
 */
public void setKeyChainValue(Object anObj, String aKC, Object aValue)  { }

/**
 * Returns a shared instance.
 */
public static TVEnv get()  { return _shared; }

/**
 * Platform Prefs.
 */
static TVPrefs _prefs = new TVPrefs();
public static class TVPrefs extends Prefs {
    
    /** Returns a value for given string. */
    public String get(String aKey)  { return null; }

    /** Returns a value for given string and default. */
    public String get(String aKey, String aDefault)  { return aDefault; }

    /** Sets a value for given string. */
    public void set(String aKey, Object aValue)  { }
    
    /** Removes a value for given key. */
    public void remove(String aKey)  { }

    /** Returns an int value for given key. */
    public int getInt(String aKey, int aDefault)  { return aDefault; }
    
    /** Returns the currently set prefs keys. */
    public String[] getKeys()  { return new String[0]; }

    /** Returns a child prefs for given name. */
    public Prefs getChild(String aName)  { return this; }
}

}