package snaptea;
import org.teavm.jso.canvas.CanvasImageSource;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.canvas.ImageData;
import org.teavm.jso.dom.html.*;
import org.teavm.jso.typedarrays.Uint8ClampedArray;
import snap.gfx.*;
import snap.web.WebURL;

/**
 * A custom class.
 */
public class TVImage extends Image {
    
    // The source
    String                   _src;
    
    // The native object
    HTMLImageElement         _img;
    
    // The canvas object
    HTMLCanvasElement        _canvas;
    
    // The size
    int                      _pw = -1, _ph = -1;
    
    // Whether image is loaded
    boolean                  _loaded, _debug;

/**
 * Creates a new TVImage from source.
 */
public TVImage(Object aSource)
{
    // Get URL and image src from source
    WebURL url = TVEnv.get().getURL(aSource);
    _src = url.getString(); if(_src.startsWith("http://abc")) _src = url.getPath().substring(1);
    
    // Create image    
    _img = _img = HTMLDocument.current().createElement("img").cast(); //.withAttr("src", src)
    _pw = _ph = 20;
    
    // Set src and wait till loaded
    setLoaded(false);
    _img.listenLoad(e -> didFinishLoad());
    _img.setSrc(_src); //doWait();
}

/** Called to wait for image load. */
private synchronized void doWait()
{
    if(_debug) System.out.println("Waiting for " + _src);
    while(!_loaded) {
        try { wait(); }
        catch(InterruptedException e) { }
    }
}

/** Called when image has finished load. */
private synchronized void didFinishLoad()
{
    _pw = _img.getWidth(); _ph = _img.getHeight();  //_loaded = true; notifyAll();
    setLoaded(true);
    if(_debug) System.out.println("Waiting done " + _src);
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
public int getRGB(int aX, int aY)
{
    getPainter();
    CanvasRenderingContext2D cntx = (CanvasRenderingContext2D)_canvas.getContext("2d");
    ImageData idata = cntx.getImageData(aX, aY, 1, 1);
    Uint8ClampedArray data = idata.getData();
    int d1 = data.get(0), d2 = data.get(1), d3 = data.get(2), d4 = data.get(3);
    return d4<<24 | d1<<16 | d2<<8 | d3;
}

/** Returns the ARGB array of this image. */
public int[] getArrayARGB()  { System.err.println("Image.getArrayARGB: Not implemented"); return null; }

/** Returns the ARGB array of this image. */
public byte[] getBytesRGBA()  { System.err.println("Image.getBytesRGBA: Not implemented"); return null; }

/** Returns the ARGB array of this image. */
public int getAlphaColorIndex()  { System.err.println("Image.getAlphaColorIndex: Not implemented"); return 0; }

/** Returns the ARGB array of this image. */
public byte[] getColorMap()  { System.err.println("Image.getColorMap: Not implemented"); return null; }

/** Returns the ARGB array of this image. */
public int getBitsPerSample()  { System.err.println("Image.getBitsPerSample: Not implemented"); return 0; }

/** Returns the ARGB array of this image. */
public int getSamplesPerPixel()  { System.err.println("Image.getSamplesPerPixel: Not implemented"); return 0; }

/** Returns the JPEG bytes for image. */
public byte[] getBytesJPEG()  { return null; }

/** Returns the PNG bytes for image. */
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