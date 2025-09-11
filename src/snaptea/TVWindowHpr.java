package snaptea;
import snap.geom.Rect;
import snap.view.*;

/**
 * A WindowHpr to map WindowView to TVWindow.
 */
public class TVWindowHpr extends WindowView.WindowHpr {

    // The snap TVWindow
    protected TVWindow  _winNtv;

    /**
     * Constructor.
     */
    public TVWindowHpr()
    {
        super();
    }

    /**
     * Initializes helper for given window.
     */
    @Override
    public void initForWindow(WindowView aWin)  { _winNtv = new TVWindow(aWin); }

    /**
     * Initializes native window.
     */
    public void initializeNativeWindow()  { _winNtv.initWindow(); }

    /**
     * Window/Popup method: Shows the window.
     */
    public void show()  { _winNtv.show(); }

    /**
     * Window/Popup method: Hides the window.
     */
    public void hide()  { _winNtv.hide(); }

    /**
     * Window/Popup method: Order window to front.
     */
    public void toFront()  { _winNtv.toFront(); }

    /**
     * Registers a view for repaint.
     */
    public void requestPaint(Rect aRect)  { _winNtv._rootViewNtv.paintViews(aRect); }
}
