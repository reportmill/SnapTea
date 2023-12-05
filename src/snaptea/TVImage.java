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
    private String _src;

    // The native object
    protected HTMLImageElement _img;

    // The canvas object
    protected HTMLCanvasElement _canvas;

    /**
     * Constructor for given size, alpha and dpi scale.
     */
    public TVImage(double aWidth, double aHeight, boolean hasAlpha, double dpiScale)
    {
        // Get scale (complain if not 1 or 2)
        _dpiScale = (int) Math.round(dpiScale);
        if (_dpiScale != 1 && _dpiScale != 2)
            System.out.println("TVImage.init: Odd scale" + _dpiScale);
        _dpiX = 72 * _dpiScale;
        _dpiY = 72 * _dpiScale;

        // Get image size, pixel size
        _width = (int) Math.round(aWidth);
        _height = (int) Math.round(aHeight);
        _pixW = (int) Math.round(_width * _dpiScale);
        _pixH = (int) Math.round(_height * _dpiScale);

        // Create canvas for pixel width/height, image width/height
        _canvas = (HTMLCanvasElement) HTMLDocument.current().createElement("canvas");
        _canvas.setWidth(_pixW);
        _canvas.setHeight(_pixH);
        _canvas.getStyle().setProperty("width", _width + "px");
        _canvas.getStyle().setProperty("height", _height + "px");
        _hasAlpha = hasAlpha;
    }

    /**
     * Constructor for given source.
     */
    public TVImage(Object aSource)
    {
        // Set source
        setSource(aSource);

        // Get Src URL string
        _src = getSourceURL(aSource);

        // Create image
        _img = HTMLDocument.current().createElement("img").cast();
        _img.setCrossOrigin("anonymous");
        _width = _height = 20;
        _pixW = _pixH = 20;

        // Set src and wait till loaded
        setLoaded(false);
        _img.listenLoad(e -> didFinishLoad());
        _img.setSrc(_src);
    }

    /**
     * Returns a Source URL from source object.
     */
    private String getSourceURL(Object aSource)
    {
        // Handle byte[] and InputStream
        if (aSource instanceof byte[] || aSource instanceof InputStream) {
            byte[] bytes = getBytes();
            Blob blob = TV.createBlob(bytes, null);
            String urls = TV.createURL(blob);
            return urls;
        }

        // Get URL
        WebURL url = WebURL.getURL(aSource);
        if (url == null)
            return null;

        // If URL can't be fetched by browser, load from bytes
        if (!isBrowsable(url))
            return getSourceURL(url.getBytes());

        // Return URL string
        String urls = url.getString().replace("!", "");
        return urls;
    }

    /**
     * Called when image has finished load.
     */
    private void didFinishLoad()
    {
        _width = _pixW = _img.getWidth();
        _height = _pixH = _img.getHeight();
        if (_src.toLowerCase().endsWith(".jpg"))
            _hasAlpha = false;
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
     * Returns an RGB integer for given x, y.
     */
    public int getRGB(int aX, int aY)
    {
        // If HTMLImageElement, convert to canvas
        if (_img != null) convertToCanvas();

        // Get image data and return rgb at point
        CanvasRenderingContext2D cntx = (CanvasRenderingContext2D) _canvas.getContext("2d");
        ImageData idata = cntx.getImageData(aX * _dpiScale, aY * _dpiScale, 1, 1);
        Uint8ClampedArray data = idata.getData();
        int d1 = data.get(0), d2 = data.get(1), d3 = data.get(2), d4 = data.get(3);
        return d4 << 24 | d1 << 16 | d2 << 8 | d3;
    }

    /**
     * Returns the decoded RGB bytes of this image.
     */
    protected byte[] getBytesRGBImpl()
    {
        // If HTMLImageElement, convert to canvas
        if (_img != null) convertToCanvas();

        // Get image data and convert to bytes
        CanvasRenderingContext2D cntx = (CanvasRenderingContext2D) _canvas.getContext("2d");
        ImageData idata = cntx.getImageData(0, 0, getPixWidth(), getPixHeight());
        Uint8ClampedArray ary8C = idata.getData();
        int len0 = ary8C.getLength(), plen = len0 / 4, len2 = plen * 3;
        byte[] bytesRGB = new byte[len2];
        for (int i = 0; i < plen; i++) {
            int x0 = i * 3, x1 = i * 4;
            bytesRGB[x0] = (byte) ary8C.get(x1);
            bytesRGB[x0 + 1] = (byte) ary8C.get(x1 + 1);
            bytesRGB[x0 + 2] = (byte) ary8C.get(x1 + 2);
        }

        // Return
        return bytesRGB;
    }

    /**
     * Returns the decoded RGBA bytes of this image.
     */
    protected byte[] getBytesRGBAImpl()
    {
        // If HTMLImageElement, convert to canvas
        if (_img != null) convertToCanvas();

        // Get image data and convert to bytes
        CanvasRenderingContext2D cntx = (CanvasRenderingContext2D) _canvas.getContext("2d");
        ImageData idata = cntx.getImageData(0, 0, getPixWidth(), getPixHeight());
        Uint8ClampedArray ary8C = idata.getData();
        byte[] bytesRGBA = new byte[ary8C.getLength()];
        for (int i = 0; i < bytesRGBA.length; i++)
            bytesRGBA[i] = (byte) ary8C.get(i);

        // Return
        return bytesRGBA;
    }

    /**
     * Returns the JPEG bytes for image.
     */
    public byte[] getBytesJPEG()
    {
        // If HTMLImageElement, convert to canvas
        if (_img != null) convertToCanvas();

        // Get image bytes
        String url = _canvas.toDataURL("image/jpeg");
        int index = url.indexOf("base64,") + "base64,".length();
        String base64 = url.substring(index);
        return ASCIICodec.decodeBase64(base64);
    }

    /**
     * Returns the PNG bytes for image.
     */
    public byte[] getBytesPNG()
    {
        // If HTMLImageElement, convert to canvas
        if (_img != null) convertToCanvas();

        // Get image bytes
        String url = _canvas.toDataURL("image/png");
        int index = url.indexOf("base64,") + "base64,".length();
        String base64 = url.substring(index);
        return ASCIICodec.decodeBase64(base64);
    }

    /**
     * Returns a painter to mark up image.
     */
    public Painter getPainter()
    {
        // If HTMLImageElement, convert to canvas
        if (_img != null)
            convertToCanvas();

        // Return painter for canvas
        return new TVPainter(_canvas, _dpiScale);
    }

    /**
     * Returns the canvas.
     */
    public HTMLCanvasElement getCanvas()
    {
        if (_img != null)
            convertToCanvas();
        return _canvas;
    }

    /**
     * Converts to canvas.
     */
    protected void convertToCanvas()
    {
        // Get canvas size and pixel size (might be 2x if HiDpi display)
        int imageW = getPixWidth();
        int imageH = getPixHeight();
        int dpiScale = TVWindow.scale;
        int pixW = imageW * dpiScale;
        int pixH = imageH * dpiScale;

        // Create new canvas for image size and pixel size
        HTMLCanvasElement canvas = (HTMLCanvasElement) HTMLDocument.current().createElement("canvas");
        canvas.setWidth(pixW);
        canvas.setHeight(pixH);
        canvas.getStyle().setProperty("width", imageW + "px");
        canvas.getStyle().setProperty("height", imageH + "px");

        // Copy ImageElement to Canvas
        Painter pntr = new TVPainter(canvas, dpiScale);
        pntr.drawImage(this, 0, 0);

        // Swap in canvas for image element
        _canvas = canvas;
        _img = null;
        _pixW = pixW;
        _pixH = pixH;
        _dpiScale = dpiScale;
        _dpiX = 72 * dpiScale;
        _dpiY = 72 * dpiScale;
    }

    /**
     * Blurs the image by mixing pixels with those around it to given radius.
     */
    public void blur(int aRad, Color aColor)
    {
        // If HTMLImageElement, convert to canvas
        if (_img != null)
            convertToCanvas();

        // Create new canvas to do blur
        HTMLCanvasElement canvas = (HTMLCanvasElement) HTMLDocument.current().createElement("canvas");
        canvas.setWidth(_pixW);
        canvas.setHeight(_pixH);
        canvas.getStyle().setProperty("width", (_pixW / _dpiScale) + "px");
        canvas.getStyle().setProperty("height", (_pixH / _dpiScale) + "px");

        // Paint image into new canvas with ShadowBlur, offset so that only shadow appears
        TVPainter pntr = new TVPainter(canvas, _dpiScale);
        pntr._cntx.setShadowBlur(aRad * _dpiScale);
        if (aColor != null)
            pntr._cntx.setShadowColor(TV.get(aColor));
        else pntr._cntx.setShadowColor("gray");
        pntr._cntx.setShadowOffsetX(-_pixW);
        pntr._cntx.setShadowOffsetY(-_pixH);
        pntr.drawImage(this, getWidth(), getHeight());

        _canvas = canvas;
    }

    /**
     * Embosses the image by mixing pixels with those around it to given radius.
     */
    public void emboss(double aRadius, double anAzi, double anAlt)
    {
        // Get basic info
        int imageW = (int) getWidth();
        int imageH = (int) getHeight();
        int pixW = getPixWidth();
        int pixH = getPixHeight();
        int radius = (int) Math.round(aRadius);
        int rad = Math.abs(radius);

        // Create bump map: original graphics offset by radius, blurred. Color doesn't matter - only alpha channel used.
        TVImage bumpImg = (TVImage) Image.getImageForSize(imageW + rad * 2, imageH + rad * 2, true);
        Painter ipntr = bumpImg.getPainter(); //ipntr.setImageQuality(1); ipntr.clipRect(0, 0, width, height);
        ipntr.drawImage(this, rad, rad, imageW, imageH);
        bumpImg.blur(rad, null);

        // Get source and bump pixels as short arrays
        short[] sourceImagePixels = TVImageUtils.getShortsRGBA(this);
        short[] bumpImagePixels = TVImageUtils.getShortsAlpha(bumpImg);

        // Call emboss method and reset pix
        TVImageUtils.emboss(sourceImagePixels, bumpImagePixels, pixW, pixH, radius * _dpiScale, anAzi * Math.PI / 180, anAlt * Math.PI / 180);
        TVImageUtils.putShortsRGBA(this, sourceImagePixels);
    }

    /**
     * Returns the native object.
     */
    public CanvasImageSource getNative()
    {
        return _img != null ? _img : _canvas;
    }
}