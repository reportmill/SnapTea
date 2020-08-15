package snaptea;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.canvas.CanvasGradient;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.events.MouseEvent;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.xml.Node;
import org.teavm.jso.dom.xml.NodeList;
import org.teavm.jso.typedarrays.Int8Array;
import snap.geom.Point;
import snap.geom.Rect;
import snap.gfx.*;
import snap.util.SnapUtils;

/**
 * Utility methods for SnapKit+TeaVM.
 */
public class TV {

    /**
     * Returns TVM color for snap color.
     */
    public static String get(Color aColor)
    {
        if (aColor==null) return null;
        int r = aColor.getRedInt(), g = aColor.getGreenInt(), b = aColor.getBlueInt(), a = aColor.getAlphaInt();
        StringBuffer sb = new StringBuffer(a==255? "rgb(" : "rgba(");
        sb.append(r).append(',').append(g).append(',').append(b);
        if (a==255) sb.append(')');
        else sb.append(',').append(a/255d).append(')');
        return sb.toString();
    }

    /**
     * Returns TVM color for snap color.
     */
    public static CanvasGradient get(GradientPaint aGP, CanvasRenderingContext2D aRC)
    {
        CanvasGradient cg = aRC.createLinearGradient(aGP.getStartX(), aGP.getStartY(), aGP.getEndX(), aGP.getEndY());
        for (int i=0,iMax=aGP.getStopCount();i<iMax;i++)
            cg.addColorStop(aGP.getStopOffset(i), get(aGP.getStopColor(i)));
        return cg;
    }

    /**
     * Returns TVM font for snap font.
     */
    public static String get(Font aFont)
    {
        String str = "";
        if (aFont.isBold()) str += "Bold ";
        if (aFont.isItalic()) str += "Italic ";
        str += ((int)aFont.getSize()) + "px ";
        str += aFont.getFamily();
        return str;
    }

    /**
     * Find a child with given id.
     */
    public static HTMLElement findElement(HTMLElement anEmt, String aId)
    {
        NodeList <Node> childEmts = anEmt.getChildNodes();
        for (int i=0; i<childEmts.getLength(); i++) { HTMLElement emt = (HTMLElement)childEmts.get(i);
            String id = emt.getAttribute("id");
            if (id!=null && id.equals(aId))
                return emt;
        }
        for (int i=0; i<childEmts.getLength(); i++) { HTMLElement emt = (HTMLElement)childEmts.get(i);
            HTMLElement emt2 = findElement(emt, aId);
            if (emt2!=null)
                return emt2;
        }
        return null;
    }

    public static Point getOffsetAll(HTMLElement anEmt)
    {
        //TextRectangle rect = anEmt.getBoundingClientRect(); return new Point(rect.getLeft(), rect.getTop());

        // Update window location
        int top = 0, left = 0; HTMLDocument doc = HTMLDocument.current();
        for (HTMLElement emt=anEmt;emt!=doc;emt=(HTMLElement)emt.getParentNode()) {
            top += TV.getOffsetTop(emt); left += TV.getOffsetLeft(emt); }
        return new Point(left,top);
    }

    @JSBody(params={ "anEmt" }, script = "return anEmt.offsetTop;")
    public static native int getOffsetTop(HTMLElement anEmt);

    @JSBody(params={ "anEmt" }, script = "return anEmt.offsetLeft;")
    public static native int getOffsetLeft(HTMLElement anEmt);

    /**
     * Viewport X/Y. 3 options:
     *
     *     - window.pageYOffset (works in S, C, FF)
     *     - window.scrollY (works in S, C, FF)
     *     - document.documentElement.scrollTop (works in C. not S. returns value+1 in FF)
     */
    @JSBody(params={ }, script = "return window.pageYOffset;")
    public static native int getViewportX();

    @JSBody(params={ }, script = "return window.pageYOffset;")
    public static native int getViewportY();

    /**
     * Viewport width/height.
     *
     * Web suggested: window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth
     */
    @JSBody(params={ }, script = "return document.documentElement.clientWidth;")
    public static native int getViewportWidth();

    @JSBody(params={ }, script = "return document.documentElement.clientHeight;")
    public static native int getViewportHeight();

    public static Rect getViewportBounds()
    {
        double x = 0, y = 0; //double x = getViewportX(), y = getViewportY();
        double w = getViewportWidth(), h = getViewportHeight();
        return new Rect(x,y,w,h);
    }

    @JSBody(params={ "aME" }, script = "return aME.pageX;")
    public static native int getPageX(MouseEvent aME);

    @JSBody(params={ "aME" }, script = "return aME.pageY;")
    public static native int getPageY(MouseEvent aME);

    @JSBody(params = { }, script = "return window.devicePixelRatio;")
    public static native double getDevicePixelRatio();

    /**
     * Creates a JavaScript File from given bytes in Java.
     */
    public static File createFile(byte theBytes[], String aName, String aType)
    {
        Int8Array bytesJS = getBytesJS(theBytes);
        File file = createFile(bytesJS, aName, aType);
        return file;
    }

    /**
     * Creates a File from given bytes in JS.
     */
    @JSBody(params={ "theBytes", "aName", "aType" }, script = "return new File([theBytes], aName, aType? { type:aType } : null);")
    static native File createFile(Int8Array theBytes, String aName, String aType);

    /**
     * Creates a Blob from given bytes in Java.
     */
    public static Blob createBlob(byte theBytes[], String aType)
    {
        Int8Array bytesJS = getBytesJS(theBytes);
        Blob blob = createBlob(bytesJS, aType);
        return blob;
    }

    /**
     * Creates a Blob from given bytes in JS.
     */
    @JSBody(params={ "theBytes", "aType" }, script = "return new Blob([theBytes], aType? { type:aType } : null);")
    static native Blob createBlob(Int8Array theBytes, String aType);

    /**
     * Creates a URL from given blob.
     */
    @JSBody(params={ "theBlob" }, script = "return URL.createObjectURL(theBlob);")
    static native String createURL(Blob theBlob);

    /**
     * Log given object.
     */
    @JSBody(params={ "anObj" }, script = "console.log(anObj);")
    public static native void log(JSObject anObj);

    /**
     * Creates an array of bytes in JS from given bytes in Java.
     */
    public static Int8Array getBytesJS(byte theBytes[])
    {
        Int8Array bytesJS = Int8Array.create(theBytes.length);
        for (int i=0; i<theBytes.length; i++) bytesJS.set(i, theBytes[i]);
        return bytesJS;
    }

    /**
     * Sets the TeaVM environment.
     */
    public static void set()
    {
        if (SnapUtils.isTeaVM) TVViewEnv.set();
    }
}