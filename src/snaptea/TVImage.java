package snaptea;
import org.teavm.jso.canvas.CanvasImageSource;
import org.teavm.jso.dom.html.*;
import snap.gfx.*;
import snap.web.WebURL;

/**
 * A custom class.
 */
public class TVImage extends Image {
    
    // The native object
    HTMLImageElement         _img;
    
    // The canvas object
    HTMLCanvasElement        _canvas;
    
    // The size
    int                      _pw = -1, _ph = -1;

/**
 * Creates a new TVImage from source.
 */
public TVImage(Object aSource)
{
    WebURL url = TVEnv.get().getURL(aSource);
    loadImage(url);
}

/**
 * Creates a new TVImage for size.
 */
public TVImage(double aWidth, double aHeight, boolean hasAlpha)
{
    _pw = (int)aWidth; _ph = (int)aHeight;
    _canvas = HTMLDocument.current().createElement("canvas").withAttr("width", String.valueOf(_pw))
        .withAttr("height", String.valueOf(_ph)).cast();
}

/**
 * Loads image synchronously with wait/notify.
 */
private synchronized void loadImage(WebURL aURL)
{
    //_img = HTMLDocument.current().createElement("img").withAttr("src", "images" + aURL.getPath()).cast();
    //_img.listenLoad(e -> { synchronized(TVImage.this) { TVImage.this.notify(); }});
    //try { wait(); } catch(Exception e) { throw new RuntimeException(e); }
}

/**
 * Returns the width of given image in pixels.
 */
public int getPixWidth()
{
    if(_pw>=0) return _pw;
    return _pw = _img.getWidth();
}

/**
 * Returns the height of given image in pixels.
 */
public int getPixHeight()
{
    if(_ph>=0) return _ph;
    return _ph = _img.getHeight();
}

/**
 * Returns whether image has alpha.
 */
public boolean hasAlpha()  { return false; }

/**
 * Returns number of components.
 */
public int getComponentCount()  { return hasAlpha()? 4 : 3; }

/**
 * Returns whether index color model.
 */
public boolean isIndexedColor()  { return false; }

/**
 * Returns an RGB integer for given x, y.
 */
public int getRGB(int aX, int aY)  { return 0; }

/**
 * Returns the ARGB array of this image.
 */
public int[] getArrayARGB()  { System.err.println("Image.getArrayARGB: Not implemented"); return null; }

/**
 * Returns the JPEG bytes for image.
 */
public byte[] getBytesJPEG()  { return null; }

/**
 * Returns the PNG bytes for image.
 */
public byte[] getBytesPNG()  { return null; }

/**
 * Returns a painter to mark up image.
 */
public Painter getPainter()
{
    if(_img!=null) {
        _canvas = HTMLDocument.current().createElement("canvas").withAttr("width", String.valueOf(getPixWidth()))
            .withAttr("height", String.valueOf(getPixHeight())).cast();
        Painter pntr = new TVPainter(_canvas);
        pntr.drawImage(this, 0, 0); _img = null;
        return pntr;
    }
    return new TVPainter(_canvas);
}

/**
 * Returns whether image data is premultiplied.
 */
public boolean isPremultiplied()  { return _pm; } boolean _pm;

/**
 * Sets whether image data is premultiplied.
 */
public void setPremultiplied(boolean aValue)  { _pm = aValue; }

/**
 * Returns the native object.
 */
public CanvasImageSource getNative()  { return _img!=null? _img : _canvas; }

}