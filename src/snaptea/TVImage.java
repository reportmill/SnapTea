package snaptea;
import java.io.InputStream;
import org.teavm.jso.canvas.CanvasImageSource;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.canvas.ImageData;
import org.teavm.jso.dom.html.*;
import org.teavm.jso.typedarrays.Int8Array;
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
    
    // Whether image has transparency
    boolean                  _hasAlpha = true;
    
/**
 * Creates a TVImage for given size.
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
 * Creates a TVImage from given source.
 */
public TVImage(Object aSource)
{
    // Set source
    setSource(aSource);
    
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
        byte bytes[] = getBytes();
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

/** Returns whether URL can be fetched by browser. */
boolean isBrowsable(WebURL aURL)
{
    String scheme = aURL.getScheme();
    return scheme.equals("http") || scheme.equals("https") || scheme.equals("data") || scheme.equals("blob");
}

/**
 * Returns the width of given image in pixels.
 */
public int getPixWidth()  { return _pw; }

/**
 * Returns the height of given image in pixels.
 */
public int getPixHeight()  { return _ph; }

/**
 * Returns the width of given image.
 */
public double getDPIX()  { return _img!=null? 72 : 72*TVWindow.scale; }

/**
 * Returns the height of given image.
 */
public double getDPIY()  { return _img!=null? 72 : 72*TVWindow.scale; }

/**
 * Returns whether image has alpha.
 */
public boolean hasAlpha()  { return _hasAlpha; }

/** Implement to avoid errors. */
protected int getPixWidthImpl() { System.err.println("TVImage.getPixWidthImpl: WTF"); return 0; }
protected int getPixHeightImpl() { System.err.println("TVImage.getPixHeightImpl: WTF"); return 0; }
protected double getDPIXImpl() { System.err.println("TVImage.getDPIXImpl: WTF"); return 0; }
protected double getDPIYImpl() { System.err.println("TVImage.getDPIYImpl: WTF"); return 0; }
protected boolean hasAlphaImpl() { System.err.println("TVImage.hasAlphaImpl: WTF"); return false; }

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

/**
 * Returns the decoded RGB bytes of this image.
 */
protected byte[] getBytesRGBImpl()
{
    // If HTMLImageElement, convert to canvas
    if(_img!=null) convertToCanvas();
    
    // Get image data and convert to bytes
    CanvasRenderingContext2D cntx = (CanvasRenderingContext2D)_canvas.getContext("2d");
    ImageData idata = cntx.getImageData(0, 0, getPixWidth(), getPixHeight());
    Uint8ClampedArray ary8C = idata.getData();
    Int8Array ary8 = Int8Array.create(ary8C.getBuffer());
    int len0 = ary8.getLength(), plen = len0/4, len2 = plen*3;
    byte bytes[] = new byte[len2];
    for(int i=0; i<plen; i++) { int x0 = i*3, x1 = i*4;
        bytes[x0] = ary8.get(x1); bytes[x0+1] = ary8.get(x1+1); bytes[x0+2] = ary8.get(x1+2); }
    return bytes;
}

/**
 * Returns the decoded RGBA bytes of this image.
 */
protected byte[] getBytesRGBAImpl()
{
    // If HTMLImageElement, convert to canvas
    if(_img!=null) convertToCanvas();
    
    // Get image data and convert to bytes
    CanvasRenderingContext2D cntx = (CanvasRenderingContext2D)_canvas.getContext("2d");
    ImageData idata = cntx.getImageData(0, 0, getPixWidth(), getPixHeight());
    Uint8ClampedArray ary8C = idata.getData();
    Int8Array ary8 = Int8Array.create(ary8C.getBuffer());
    byte bytes[] = new byte[ary8.getLength()]; for(int i=0; i<bytes.length; i++) bytes[i] = ary8.get(i);
    return bytes;
}

/** Returns the JPEG bytes for image. */
public byte[] getBytesJPEG()  { System.err.println("Image.getBytesJPEG: Not impl"); return null; }

/** Returns the PNG bytes for image. */
public byte[] getBytesPNG()  { System.err.println("Image.getBytesPNG: Not impl"); return null; }

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
public void blur(int aRad, Color aColor)
{
    // If HTMLImageElement, convert to canvas
    if(_img!=null) convertToCanvas();
    
    // Create new canvas to do blur
    HTMLCanvasElement canvas = (HTMLCanvasElement)HTMLDocument.current().createElement("canvas");
    canvas.setWidth(_pw); canvas.setHeight(_ph);
    canvas.getStyle().setProperty("width", (_pw/TVWindow.scale) + "px");
    canvas.getStyle().setProperty("height", (_ph/TVWindow.scale) + "px");
    
    // Paint image into new canvas with ShadowBlur, offset so that only shadow appears
    TVPainter pntr = new TVPainter(canvas);
    pntr._cntx.setShadowBlur(aRad);
    if(aColor!=null) pntr._cntx.setShadowColor(TV.get(aColor));
    pntr._cntx.setShadowOffsetX(-_pw);
    pntr._cntx.setShadowOffsetY(-_ph);
    pntr.drawImage(this, getWidth(), getHeight());
   
    _canvas = canvas;
}

/**
 * Returns the native object.
 */
public CanvasImageSource getNative()  { return _img!=null? _img : _canvas; }

}