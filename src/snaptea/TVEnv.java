package snaptea;
import java.util.*;
import org.teavm.jso.browser.Window;
import snap.gfx.*;
import snap.util.*;
import snap.web.*;

/**
 * A GFXEnv implementation for TeaVM.
 */
public class TVEnv extends GFXEnv {

    // The shared AWTEnv
    static TVEnv     _shared = new TVEnv();
    
    // Map of sites
    Map <WebURL,WebSite> _sites = new HashMap();

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
public Image getImage(Object aSource)
{
    if(aSource instanceof byte[]) {
        System.err.println("TVEnv.getImage: Trying to load from bytes");
        return null;
    }
    
    WebURL url = getURL(aSource);
    if(url==null)
        return null;
        
    return new TVImage(url);
}

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
    // Handle URL
    if(aSource instanceof WebURL) return (WebURL)aSource;
    if(aSource instanceof WebFile) return ((WebFile)aSource).getURL();
    
    // Handle String
    if(aSource instanceof String) {
        String str = (String)aSource;
        String urls = str; if(!urls.startsWith("http")) urls = "http://abc.com" + urls;
        WebURL url = new WebURL(aSource, urls);
        String upath = url.getPath();
        if(upath!=null && upath.length()>0 && url.getSite() instanceof TVWebSite) {
            TVWebSite tsite = (TVWebSite)url.getSite();
            if(!tsite.isPath(upath))
                return null; } //System.out.println("TVEnv.getURL: Path doesn't exist: " + upath);
        return url;
    }
    
    // Complain and return
    System.out.println("No URL for Source: " + aSource);
    return null;
}

/**
 * Returns a URL for given class and name/path string.
 */
public WebURL getURL(Class aClass, String aName)
{
    // If name is absolute path, just forward on
    if(aName.startsWith("/"))
        return getURL(aName);
        
    // Otherwise get path for class name, add name and get URL
    String cname = aClass.getName(); int cind = cname.lastIndexOf('.');
    String cpath = ""; if(cind>0) cpath = '/' + cname.substring(0, cind).replace('.', '/');
    String upath = cpath + '/' + aName;
    return getURL(upath);
}

/**
 * Returns a site for given source URL.
 */
public synchronized WebSite getSite(WebURL aSiteURL)
{
    WebSite site = _sites.get(aSiteURL);
    if(site==null) _sites.put(aSiteURL, site = createSite(aSiteURL));
    return site;
}

/**
 * Creates a site for given URL.
 */
protected WebSite createSite(WebURL aSiteURL)
{
    WebURL parentSiteURL = aSiteURL.getSiteURL();
    String scheme = aSiteURL.getScheme(), path = aSiteURL.getPath(); if(path==null) path = "";
    WebSite site = null;
    
    // If url has path, see if it's jar or zip
    if(parentSiteURL!=null && path.length()>0) site = new DirSite();
    else if(scheme.equals("file")) site = new TVWebSite();
    else if(scheme.equals("http") || scheme.equals("https")) site = new TVWebSite();
    if(site!=null) WebUtils.setSiteURL(site, aSiteURL);
    return site;
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
 * Returns a key list value.
 */
public Object getKeyListValue(Object anObj, String aKey, int anIndex)  { return null; }

/**
 * Adds a key list value.
 */
public void setKeyListValue(Object anObj, String aKey, Object aValue, int anIndex)  { }

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