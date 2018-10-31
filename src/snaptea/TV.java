package snaptea;
import org.teavm.jso.JSBody;
import org.teavm.jso.canvas.CanvasGradient;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.xml.Node;
import org.teavm.jso.dom.xml.NodeList;
import snap.gfx.*;
import snap.util.SnapUtils;

/**
 * A custom class.
 */
public class TV {

/** Returns TVM color for snap color. */
public static String get(Color aColor)
{
    if(aColor==null) return null;
    int r = aColor.getRedInt(), g = aColor.getGreenInt(), b = aColor.getBlueInt(), a = aColor.getAlphaInt();
    StringBuffer sb = new StringBuffer(a==255? "rgb(" : "rgba(");
    sb.append(r).append(',').append(g).append(',').append(b);
    if(a==255) sb.append(')'); else sb.append(',').append(a/255d).append(')');
    return sb.toString();
}

/** Returns TVM color for snap color. */
public static CanvasGradient get(GradientPaint aGP, CanvasRenderingContext2D aRC)
{
    CanvasGradient cg = aRC.createLinearGradient(aGP.getStartX(), aGP.getStartY(), aGP.getEndX(), aGP.getEndY());
    for(int i=0,iMax=aGP.getStopCount();i<iMax;i++)
        cg.addColorStop(aGP.getStopOffset(i), get(aGP.getStopColor(i)));
    return cg;
}

/** Returns TVM font for snap font. */
public static String get(Font aFont)
{
    String str = ""; if(aFont.isBold()) str += "Bold "; if(aFont.isItalic()) str += "Italic ";
    str += ((int)aFont.getSize()) + "px "; str += aFont.getFamily();
    return str;
}

/**
 * Find a child with given id.
 */
public static HTMLElement findElement(HTMLElement anEmt, String aId)
{
    NodeList <Node> childEmts = anEmt.getChildNodes();
    for(int i=0; i<childEmts.getLength(); i++) { HTMLElement emt = (HTMLElement)childEmts.get(i);
        String id = emt.getAttribute("id");
        if(id!=null && id.equals(aId))
            return emt;
    }
    for(int i=0; i<childEmts.getLength(); i++) { HTMLElement emt = (HTMLElement)childEmts.get(i);
        HTMLElement emt2 = findElement(emt, aId);
        if(emt2!=null)
            return emt2;
    }
    return null;
}

public static Point getOffsetAll(HTMLElement anEmt)
{
    //TextRectangle rect = anEmt.getBoundingClientRect(); return new Point(rect.getLeft(), rect.getTop());

    // Update window location
    int top = 0, left = 0; HTMLDocument doc = HTMLDocument.current();
    for(HTMLElement emt=anEmt;emt!=doc;emt=(HTMLElement)emt.getParentNode()) {
        top += TV.getOffsetTop(emt); left += TV.getOffsetLeft(emt); }
    return new Point(left,top);
}

@JSBody(params={ "anEmt" }, script = "return anEmt.offsetTop;")
public static native int getOffsetTop(HTMLElement anEmt);

@JSBody(params={ "anEmt" }, script = "return anEmt.offsetLeft;")
public static native int getOffsetLeft(HTMLElement anEmt);

@JSBody(params={ }, script = "return window.scrollX;")
public static native int getWindowScrollX();

@JSBody(params={ }, script = "return window.scrollY;")
public static native int getWindowScrollY();

//return window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth
@JSBody(params={ }, script = "return document.documentElement.clientWidth;")
public static native int getBrowserWindowWidth();

//return window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight
@JSBody(params={ }, script = "return document.body.clientHeight;")
public static native int getBrowserWindowHeight();

@JSBody(params = { }, script = "return window.devicePixelRatio;")
public static native double getDevicePixelRatio();
    
/**
 * Sets the TeaVM environment.
 */
public static void set()  { if(SnapUtils.isTeaVM) TVViewEnv.set(); }

}