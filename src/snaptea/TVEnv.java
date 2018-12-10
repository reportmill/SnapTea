package snaptea;
import org.teavm.jso.browser.Window;
import snap.gfx.*;
import snap.web.*;

/**
 * A GFXEnv implementation for TeaVM.
 */
public class TVEnv extends GFXEnv {

    // The shared AWTEnv
    static TVEnv     _shared;

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
 * Returns a shared instance.
 */
public static TVEnv get()
{
    if(_shared!=null) return _shared;
    return _shared = new TVEnv();
}

}