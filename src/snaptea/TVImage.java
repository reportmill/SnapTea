package snaptea;
import java.io.InputStream;
import org.teavm.jso.canvas.CanvasImageSource;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.canvas.ImageData;
import org.teavm.jso.dom.html.*;
import org.teavm.jso.typedarrays.Uint8ClampedArray;
import snap.gfx.*;
import snap.util.SnapUtils;
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
    
    // Whether image has transparency
    boolean                  _hasAlpha = true;
    
/**
 * Creates a new TVImage from source.
 */
public TVImage(Object aSource)
{
    // Get Src URL string
    _src = getSourceURL(aSource);
    
    // Create image    
    _img = HTMLDocument.current().createElement("img").cast();
    _pw = _ph = 20;
    
    // Set src and wait till loaded
    setLoaded(false);
    _img.listenLoad(e -> didFinishLoad());
    _img.setSrc(_src);
}

/**
 * Returns a Source URL from source object.
 */
String getSourceURL(Object aSource)
{
    // Handle byte[] and InputStream
    if(aSource instanceof byte[] || aSource instanceof InputStream) {
        byte bytes[] = SnapUtils.getBytes(aSource);
        readBasicInfo(bytes);
        Blob blob = TV.createBlob(bytes, null);
        String urls = TV.createURL(blob);
        return urls;
    }
    
    // Get URL
    WebURL url = WebURL.getURL(aSource);
    if(url==null)
        return null;
        
    // If URL can't be fetched by browser, load from bytes
    if(!isBrowsable(url))
        return getSourceURL(url.getBytes());
        
    // Return URL string
    String urls = url.getString().replace("!", "");
    return urls;
}

/** Called when image has finished load. */
void didFinishLoad()
{
    _pw = _img.getWidth(); _ph = _img.getHeight();  //_loaded = true; notifyAll();
    if(_src.toLowerCase().endsWith(".jpg")) _hasAlpha = false;
    snap.view.ViewUtils.runLater(() -> setLoaded(true));
}

/**
 * Returns whether URL can be fetched by browser.
 */
boolean isBrowsable(WebURL aURL)
{
    String scheme = aURL.getScheme();
    return scheme.equals("http") || scheme.equals("https") || scheme.equals("data") || scheme.equals("blob");
}

/**
 * Read basic info if bytes.
 */
void readBasicInfo(byte theBytes[])
{
    String type = ImageUtils.getImageType(theBytes);
    if(type.equals("jpg")) {
        ImageUtils.ImageInfo info = ImageUtils.getInfoJPG(theBytes);
        _pw = info.width; _ph = info.height;
    }
}

/**
 * Creates a new TVImage for size.
 */
public TVImage(double aWidth, double aHeight, boolean hasAlpha)
{
    int w = (int)aWidth, h = (int)aHeight;
    _pw = w*TVWindow.scale; _ph = h*TVWindow.scale;
    _canvas = (HTMLCanvasElement)HTMLDocument.current().createElement("canvas");
    _canvas.setWidth(_pw); _canvas.setHeight(_ph);
    _canvas.getStyle().setProperty("width", w + "px");
    _canvas.getStyle().setProperty("height", h + "px");
    _hasAlpha = hasAlpha;
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
 * Returns the width of given image.
 */
public double getWidthDPI()  { return _img!=null? 72 : 72*TVWindow.scale; }

/**
 * Returns the height of given image.
 */
public double getHeightDPI()  { return _img!=null? 72 : 72*TVWindow.scale; }

/**
 * Returns whether image has alpha.
 */
public boolean hasAlpha()  { return _hasAlpha; }

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
    // If HTMLImageElement, convert to canvas
    if(_img!=null) convertToCanvas();
    
    // Get image data and return rgb at point
    CanvasRenderingContext2D cntx = (CanvasRenderingContext2D)_canvas.getContext("2d");
    ImageData idata = cntx.getImageData(aX*TVWindow.scale, aY*TVWindow.scale, 1, 1);
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
public int getBitsPerSample()  { System.err.println("Image.getBitsPerSample: Not implemented"); return 8; }

/** Returns the ARGB array of this image. */
public int getSamplesPerPixel()  { System.err.println("Image.getSamplesPerPixel: Not implemented"); return 4; }

/** Returns the JPEG bytes for image. */
public byte[] getBytesJPEG()  { return null; }

/** Returns the PNG bytes for image. */
public byte[] getBytesPNG()  { return null; }

/**
 * Returns a painter to mark up image.
 */
public Painter getPainter()
{
    // If HTMLImageElement, convert to canvas
    if(_img!=null) convertToCanvas();
    
    // Return painter for canvas
    return new TVPainter(_canvas);
}

/**
 * Converts to canvas.
 */
protected void convertToCanvas()
{
    int w = getPixWidth(), h = getPixHeight(); _pw *= TVWindow.scale; _ph *= TVWindow.scale;
    _canvas = (HTMLCanvasElement)HTMLDocument.current().createElement("canvas");
    _canvas.setWidth(_pw); _canvas.setHeight(_ph);
    _canvas.getStyle().setProperty("width", w + "px");
    _canvas.getStyle().setProperty("height", h + "px");
    Painter pntr = new TVPainter(_canvas);
    pntr.drawImage(this, 0, 0); _img = null;
    //CanvasRenderingContext2D cntx = (CanvasRenderingContext2D)_canvas.getContext("2d"); cntx.drawImage(_img, 0, 0);
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
 * Blurs the image by mixing pixels with those around it to given radius.
 */
public void blur(int aRad)
{
    // If HTMLImageElement, convert to canvas
    if(_img!=null) convertToCanvas();
    
    // Create new canvas to do blur
    HTMLCanvasElement canvas = (HTMLCanvasElement)HTMLDocument.current().createElement("canvas");
    canvas.setWidth(_pw); canvas.setHeight(_ph);
    canvas.getStyle().setProperty("width", (_pw/TVWindow.scale) + "px");
    canvas.getStyle().setProperty("height", (_ph/TVWindow.scale) + "px");
    
    // Paint image into new canvas with ShadowBlur
    TVPainter pntr = new TVPainter(canvas);
    pntr._cntx.setShadowBlur(aRad);
    pntr._cntx.setShadowColor("black");
    pntr.drawImage(this, 0, 0);
    pntr._cntx.setShadowBlur(0);
    _canvas = canvas;
}

/**
 * Returns the native object.
 */
public CanvasImageSource getNative()  { return _img!=null? _img : _canvas; }

}