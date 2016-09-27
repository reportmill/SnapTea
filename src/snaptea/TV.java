package snaptea;
import org.teavm.jso.canvas.CanvasGradient;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import snap.gfx.*;

/**
 * A custom class.
 */
public class TV {

/** Returns TVM color for snap color. */
public static String get(Color aColor)  { return aColor!=null? '#' + aColor.toHexString() : null; }

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