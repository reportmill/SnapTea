package snaptea;
import org.teavm.jso.JSBody;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.canvas.TextMetrics;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.gfx.*;

import java.util.Arrays;

/**
 * A FontFile subclass for TeaVM.
 */
public class TVFontFile extends FontFile {
    
    // The name
    private String  _name = "Arial";
    
    // The Family name
    private String  _fname = "Arial";
    
    // The char advance cache array
    private double  _adv[];

    // The canvas
    private static HTMLCanvasElement _canvas;

    // The RenderContext2D
    private static CanvasRenderingContext2D  _cntx;

    /**
     * Creates a new TVFontFile for given name.
     */
    public TVFontFile(String aName)
    {
        // Set name and family
        _name = aName;
        _fname = _name.replace("Bold","").replace("Italic","").trim();

        // Create/init advance cache array
        _adv = new double[255];
        Arrays.fill(_adv, -1);

        // Initialize Canvas/Context
        if (_canvas==null) {
            _canvas = (HTMLCanvasElement) HTMLDocument.current().createElement("canvas");
            _cntx = (CanvasRenderingContext2D) _canvas.getContext("2d");
        }
    }

    /**
     * Returns the name of this font.
     */
    public String getName()  { return _name; }

    /**
     * Returns the name of this font in English.
     */
    public String getNameEnglish()  { return _name; }

    /**
     * Returns the family name of this font.
     */
    public String getFamily()  { return _fname; }

    /**
     * Returns the PostScript name of this font.
     */
    public String getPSName()  { return _name; }

    /**
     * Returns the family name of this font in English.
     */
    public String getFamilyEnglish()  { return _fname; }

    /**
     * Returns the font declaration string in JavaScript format.
     */
    public String getJSName()
    {
        String str = "";
        if (isBold()) str += "Bold ";
        if (isItalic()) str += "Italic ";
        str += "1000px ";
        str += getFamily();
        return str;
    }

    /**
     * Returns the char advance for the given char.
     */
    protected double charAdvanceImpl(char aChar)
    {
        // Handle basic range
        if (aChar<=255) {
            double adv = _adv[aChar];
            if (adv>=0)
                return adv;
            return _adv[aChar] = charAdvanceImplImpl(aChar);
        }

        // Extended chars
        return charAdvanceImplImpl(aChar);
    }

    /**
     * Returns the char advance for the given char.
     */
    private double charAdvanceImplImpl(char aChar)
    {
        _cntx.setFont(getJSName());
        TextMetrics metrics = _cntx.measureText(String.valueOf(aChar));
        return metrics.getWidth()/1000d;
    }

    /**
     * Returns the bounds rect for glyphs in given string.
     */
    public Rect getGlyphBounds(String aString)
    {
        _cntx.setFont(getJSName());
        TextMetrics metrics = _cntx.measureText(aString);
        double glyphW = metrics.getWidth();
        double glyphAsc = getMetricsActualAscent(metrics);
        double glyphDesc = getMetricsActualDescent(metrics);
        double glyphH = glyphAsc + glyphDesc;
        return new Rect(0, -glyphAsc, glyphW, glyphH);
    }

    /**
     * Returns the path for a given char (does the real work, but doesn't cache).
     */
    protected Shape getCharPathImpl(char c)
    {
        return new Rect(0,0,1000,1000);
    }

    /**
     * Returns the path for given string with character spacing.
     */
    public Shape getOutline(CharSequence aStr, double aSize, double aX, double aY, double aCharSpacing)
    {
        return new Rect(0,0,1000,1000);
    }

    /**
     * Returns the max distance above the baseline that this font goes.
     */
    public double getAscent()
    {
        _cntx.setFont(getJSName());
        TextMetrics metrics = _cntx.measureText("H");
        double ascent = getMetricsFontAscent(metrics)/1000;
        return ascent>0 ? ascent : .906;
    }

    /**
     * Returns the max distance below the baseline that this font goes.
     */
    public double getDescent()
    {
        _cntx.setFont(getJSName());
        TextMetrics metrics = _cntx.measureText("H");
        double desc = getMetricsFontDescent(metrics)/1000;
        return desc>0 ? desc : .212;
    }

    @JSBody(params = { "aTM" }, script = "return aTM.fontBoundingBoxAscent || 906;")
    public static native double getMetricsFontAscent(TextMetrics aTM);

    @JSBody(params = { "aTM" }, script = "return aTM.actualBoundingBoxAscent || 906;")
    public static native double getMetricsActualAscent(TextMetrics aTM);

    @JSBody(params = { "aTM" }, script = "return aTM.fontBoundingBoxDescent || 212;")
    public static native double getMetricsFontDescent(TextMetrics aTM);

    @JSBody(params = { "aTM" }, script = "return aTM.actualBoundingBoxDescent || 212;")
    public static native double getMetricsActualDescent(TextMetrics aTM);

    /**
     * Returns the default distance between lines for this font.
     */
    public double getLeading()
    {
        return .033;
    }

    /**
     * Returns if this font can display the given char.
     */
    protected boolean canDisplayImpl(char aChar)
    {
        return true;
    }

    /** Override to return TVM font. */
    public Object getNative()  { return getName() + ' ' + 1000; }

    /** Override to return TVM font. */
    public Object getNative(double aSize)  { return getName() + ' ' + aSize; }

    /** Override to return TVM font. */
    public String getNativeName()  { return getName(); }
}