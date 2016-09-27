package snaptea;
import org.teavm.jso.canvas.CanvasGradient;
import org.teavm.jso.canvas.CanvasImageSource;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import snap.gfx.*;

/**
 * A custom class.
 */
public class TVPainter extends PainterImpl {
    
    HTMLCanvasElement _canvas;
    CanvasRenderingContext2D _cntx;

/**
 * Creates a new painter for given canvas.
 */
public TVPainter(HTMLCanvasElement aCnvs)
{
    _canvas = aCnvs;
    _cntx = (CanvasRenderingContext2D)_canvas.getContext("2d");
}

/**
 * Sets the paint in painter.
 */
public void setPaint(Paint aPaint)
{
    super.setPaint(aPaint);
    if(aPaint instanceof Color) { String cstr = TV.get((Color)aPaint);
        _cntx.setFillStyle(cstr);
        _cntx.setStrokeStyle(cstr);
    }
}

/** Sets the current stroke. */
public void setStroke(Stroke aStroke)
{
    super.setStroke(aStroke);
    _cntx.setLineWidth(aStroke!=null? aStroke.getWidth() : 1);
}

/**
 * Sets the font in painter.
 */
public void setFont(Font aFont)
{
    super.setFont(aFont);
    _cntx.setFont(TV.get(aFont));
}

/**
 * Sets the current transform.
 */
public void setTransform(Transform aTrans)
{
    super.setTransform(aTrans);
    double m[] = aTrans.getMatrix();
    _cntx.setTransform(m[0], m[1], m[2], m[3], m[4], m[5]);
}

/**
 * Transform painter.
 */
public void transform(Transform aTrans)
{
    super.transform(aTrans);
    double m[] = aTrans.getMatrix();
    _cntx.transform(m[0], m[1], m[2], m[3], m[4], m[5]);
}

/**
 * Draws a shape in painter.
 */
public void draw(Shape aShape)
{
    if(getPaint() instanceof GradientPaint) { GradientPaint gpnt = (GradientPaint)getPaint();
        GradientPaint gpnt2 = gpnt.copyFor(aShape.getBounds());
        CanvasGradient cg = TV.get(gpnt2, _cntx);
        _cntx.setStrokeStyle(cg);
    }
    
    setShape(aShape);
    _cntx.stroke();
}

/**
 * Draws a shape in painter.
 */
public void fill(Shape aShape)
{
    if(getPaint() instanceof GradientPaint) { GradientPaint gpnt = (GradientPaint)getPaint();
        GradientPaint gpnt2 = gpnt.copyFor(aShape.getBounds());
        CanvasGradient cg = TV.get(gpnt2, _cntx);
        _cntx.setFillStyle(cg);
    }
    
    setShape(aShape);
    _cntx.fill();
}

/**
 * Clips a shape in painter.
 */
public void clip(Shape aShape)
{
    setShape(aShape);
    _cntx.clip();
}

/**
 * Sets a shape.
 */
public void setShape(Shape aShape)
{
    double pnts[] = new double[6];
    PathIter piter = aShape.getPathIter(null);
    _cntx.beginPath();
    while(piter.hasNext()) {
        switch(piter.getNext(pnts)) {
            case MoveTo: _cntx.moveTo(pnts[0], pnts[1]); break;
            case LineTo: _cntx.lineTo(pnts[0], pnts[1]); break;
            case CubicTo: _cntx.bezierCurveTo(pnts[0], pnts[1], pnts[2], pnts[3], pnts[4], pnts[5]); break;
            case Close: _cntx.closePath(); break;
        }
    }
}

/**
 * Draw image with transform.
 */
public void drawImage(Image anImg, Transform xform)
{
    CanvasImageSource img = anImg.getNative() instanceof CanvasImageSource? (CanvasImageSource)anImg.getNative() : null;
    save();
    transform(xform);
    _cntx.drawImage(img, 0, 0);
    restore();
}

/**
 * Draw image in rect.
 */
public void drawImage(Image anImg, double sx, double sy, double sw, double sh, double dx,double dy,double dw,double dh)
{
    // Correct source width/height for image dpi
    if(anImg.getWidthDPI()!=72) sw *= anImg.getWidthDPI()/72;
    if(anImg.getHeightDPI()!=72) sh *= anImg.getHeightDPI()/72;
    
    // Get points for corner as ints and draw image
    CanvasImageSource img = anImg instanceof TVImage? (CanvasImageSource)anImg.getNative() : null;
    _cntx.drawImage(img, sx, sy, sw, sh, dx, dy, dw, dh);
}

/**
 * Draw string at location.
 */
public void drawString(String aStr, double aX, double aY, double cs)
{
    // Handle no char spacing
    if(cs==0) _cntx.fillText(aStr, aX, aY);
        
    // Handle char spacing
    else {
        _cntx.fillText(aStr, aX, aY);
    }
}

/**
 * Clears a rect.
 */
public void clearRect(double aX, double aY, double aW, double aH)  { _cntx.clearRect(aX,aY,aW,aH); }

/**
 * Standard clone implementation.
 */
public void save()  { super.save(); _cntx.save(); }

/**
 * Disposes of the painter.
 */
public void restore()  { super.restore(); _cntx.restore(); }

}