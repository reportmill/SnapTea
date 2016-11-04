package snaptea;
import org.teavm.jso.canvas.CanvasGradient;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import snap.gfx.*;

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
 * Sets the TeaVM environment.
 */
public static void set()  { TVViewEnv.set(); }

}