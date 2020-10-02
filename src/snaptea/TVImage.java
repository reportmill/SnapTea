package snaptea;
import java.io.InputStream;
import org.teavm.jso.canvas.CanvasImageSource;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.canvas.ImageData;
import org.teavm.jso.dom.html.*;
import org.teavm.jso.typedarrays.Uint8ClampedArray;
import snap.gfx.*;
import snap.util.ASCIICodec;
import snap.web.WebURL;

/**
 * An Image subclass for TeaVM.
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
    
    // The dpi scale (1 = normal, 2 = retina/hidpi)
    int                      _scale = 1;        
    
    // Whether image has transparency
    boolean                  _hasAlpha = true;
    
    /**
     * Creates a TVImage for given size.
     */
    public TVImage(double aWidth, double aHeight, boolean hasAlpha, double aScale)
    {
        // Get scale (complain if not 1 or 2)
        _scale = (int)Math.round(aScale);
        if (_scale!=1 && _scale!=2) System.out.println("TVImage.init: Odd scale" + _scale);

        // Get image size, pixel size
        int w = (int)Math.round(aWidth);
        int h = (int)Math.round(aHeight);
        _pw = w*_scale;
        _ph = h*_scale;

        // Create canvas for pixel width/height, image width/height
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
        if (aSource instanceof byte[] || aSource instanceof InputStream) {
            byte bytes[] = getBytes();
            Blob blob = TV.createBlob(bytes, null);
            String urls = TV.createURL(blob);
            return urls;
        }

        // Get URL
        WebURL url = WebURL.getURL(aSource);
        if (url==null)
            return null;

        // If URL can't be fetched by browser, load from bytes
        if (!isBrowsable(url))
            return getSourceURL(url.getBytes());

        // Return URL string
        String urls = url.getString().replace("!", "");
        return urls;
    }

    /** Called when image has finished load. */
    void didFinishLoad()
    {
        _pw = _img.getWidth(); _ph = _img.getHeight();  //_loaded = true; notifyAll();
        if (_src.toLowerCase().endsWith(".jpg")) _hasAlpha = false;
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
    public double getDPIX()  { return 72*_scale; }

    /**
     * Returns the height of given image.
     */
    public double getDPIY()  { return 72*_scale; }

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
     * Returns an RGB integer for given x, y.
     */
    public int getRGB(int aX, int aY)
    {
        // If HTMLImageElement, convert to canvas
        if (_img!=null) convertToCanvas();

        // Get image data and return rgb at point
        CanvasRenderingContext2D cntx = (CanvasRenderingContext2D)_canvas.getContext("2d");
        ImageData idata = cntx.getImageData(aX*_scale, aY*_scale, 1, 1);
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
        if (_img!=null) convertToCanvas();

        // Get image data and convert to bytes
        CanvasRenderingContext2D cntx = (CanvasRenderingContext2D)_canvas.getContext("2d");
        ImageData idata = cntx.getImageData(0, 0, getPixWidth(), getPixHeight());
        Uint8ClampedArray ary8C = idata.getData();
        int len0 = ary8C.getLength(), plen = len0/4, len2 = plen*3;
        byte bytes[] = new byte[len2];
        for (int i=0; i<plen; i++) {
            int x0 = i*3, x1 = i*4;
            bytes[x0] = (byte)ary8C.get(x1);
            bytes[x0+1] = (byte)ary8C.get(x1+1);
            bytes[x0+2] = (byte)ary8C.get(x1+2);
        }
        return bytes;
    }

    /**
     * Returns the decoded RGBA bytes of this image.
     */
    protected byte[] getBytesRGBAImpl()
    {
        // If HTMLImageElement, convert to canvas
        if (_img!=null) convertToCanvas();

        // Get image data and convert to bytes
        CanvasRenderingContext2D cntx = (CanvasRenderingContext2D)_canvas.getContext("2d");
        ImageData idata = cntx.getImageData(0, 0, getPixWidth(), getPixHeight());
        Uint8ClampedArray ary8C = idata.getData();
        byte bytes[] = new byte[ary8C.getLength()];
        for (int i=0; i<bytes.length; i++)
            bytes[i] = (byte) ary8C.get(i);
        return bytes;
    }

    /** Returns the JPEG bytes for image. */
    public byte[] getBytesJPEG()
    {
        // If HTMLImageElement, convert to canvas
        if (_img!=null) convertToCanvas();

        // Get image bytes
        String url = _canvas.toDataURL("image/jpeg");
        int index = url.indexOf("base64,") + "base64,".length();
        String base64 = url.substring(index);
        byte bytes[] = ASCIICodec.decodeBase64(base64);
        return bytes;
    }

    /** Returns the PNG bytes for image. */
    public byte[] getBytesPNG()
    {
        // If HTMLImageElement, convert to canvas
        if (_img!=null) convertToCanvas();

        // Get image bytes
        String url = _canvas.toDataURL("image/png");
        int index = url.indexOf("base64,") + "base64,".length();
        String base64 = url.substring(index);
        byte bytes[] = ASCIICodec.decodeBase64(base64);
        return bytes;
    }

    /**
     * Returns a painter to mark up image.
     */
    public Painter getPainter()
    {
        // If HTMLImageElement, convert to canvas
        if (_img!=null) convertToCanvas();

        // Return painter for canvas
        return new TVPainter(_canvas, _scale);
    }

    /**
     * Converts to canvas.
     */
    protected void convertToCanvas()
    {
        // Get canvas size and pixel size (might be 2x if HiDpi display)
        int w = getPixWidth();
        int h = getPixHeight();
        int scale = TVWindow.scale;
        int pw = w*scale, ph = h*scale;

        // Create new canvas for image size and pixel size
        HTMLCanvasElement canvas = (HTMLCanvasElement)HTMLDocument.current().createElement("canvas");
        canvas.setWidth(pw);
        canvas.setHeight(ph);
        canvas.getStyle().setProperty("width", w + "px");
        canvas.getStyle().setProperty("height", h + "px");

        // Copy ImageElement to Canvas
        Painter pntr = new TVPainter(canvas, scale);
        pntr.drawImage(this, 0, 0);

        // Swap in canvas for image element
        _canvas = canvas; _img = null;
        _pw = pw; _ph = ph; _scale = scale;
    }

    /**
     * Blurs the image by mixing pixels with those around it to given radius.
     */
    public void blur(int aRad, Color aColor)
    {
        // If HTMLImageElement, convert to canvas
        if (_img!=null) convertToCanvas();

        // Create new canvas to do blur
        HTMLCanvasElement canvas = (HTMLCanvasElement)HTMLDocument.current().createElement("canvas");
        canvas.setWidth(_pw); canvas.setHeight(_ph);
        canvas.getStyle().setProperty("width", (_pw/_scale) + "px");
        canvas.getStyle().setProperty("height", (_ph/_scale) + "px");

        // Paint image into new canvas with ShadowBlur, offset so that only shadow appears
        TVPainter pntr = new TVPainter(canvas, _scale);
        pntr._cntx.setShadowBlur(aRad*_scale);
        if (aColor!=null)
            pntr._cntx.setShadowColor(TV.get(aColor));
        else pntr._cntx.setShadowColor("gray");
        pntr._cntx.setShadowOffsetX(-_pw);
        pntr._cntx.setShadowOffsetY(-_ph);
        pntr.drawImage(this, getWidth(), getHeight());

        _canvas = canvas;
    }

    /**
     * Embosses the image by mixing pixels with those around it to given radius.
     */
    public void emboss(double aRadius, double anAzi, double anAlt)
    {
        // Get basic info
        int w = (int)getWidth(), h = (int)getHeight();
        int pw = getPixWidth(), ph = getPixHeight();
        int radius = (int)Math.round(aRadius), rad = Math.abs(radius);

        // Create bump map: original graphics offset by radius, blurred. Color doesn't matter - only alpha channel used.
        TVImage bumpImg = (TVImage)Image.get(w+rad*2, h+rad*2, true);
        Painter ipntr = bumpImg.getPainter(); //ipntr.setImageQuality(1); ipntr.clipRect(0, 0, width, height);
        ipntr.drawImage(this, rad, rad, w, h);
        bumpImg.blur(rad, null);

        // Get source and bump pixels as short arrays
        short spix[] = TVImageUtils.getShortsRGBA(this);
        short bpix[] = TVImageUtils.getShortsAlpha(bumpImg);

        // Call emboss method and reset pix
        TVImageUtils.emboss(spix, bpix, pw, ph, radius*_scale, anAzi*Math.PI/180, anAlt*Math.PI/180);
        TVImageUtils.putShortsRGBA(this, spix);
    }

    /**
     * Returns the native object.
     */
    public CanvasImageSource getNative()  { return _img!=null ? _img : _canvas; }
}