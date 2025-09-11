package snaptea;
import snap.geom.Rect;
import snap.view.*;

/**
 * A WindowHpr to map WindowView to TVWindow.
 */
public class TVWindowHpr extends WindowView.WindowHpr {

    // The snap Window
    protected WindowView  _win;

    // The snap TVWindow
    protected TVWindow  _winNtv;

    /**
     * Creates the native.
     */
    public WindowView getWindow()
    {
        return _win;
    }

    /**
     * Override to set snap Window in TVWindow.
     */
    public void setWindow(WindowView aWin)
    {
        _win = aWin;
        _winNtv = new TVWindow(aWin);
    }

    /**
     * Window method: initializes native window.
     */
    public void initWindow()
    {
        _winNtv.initWindow();
    }

    /**
     * Window/Popup method: Shows the window.
     */
    public void show()
    {
        _winNtv.show();
    }

    /**
     * Window/Popup method: Hides the window.
     */
    public void hide()
    {
        _winNtv.hide();
    }

    /**
     * Window/Popup method: Order window to front.
     */
    public void toFront()
    {
        _winNtv.toFront();
    }

    /**
     * Registers a view for repaint.
     */
    public void requestPaint(Rect aRect)
    {
        _winNtv._rootViewNtv.paintViews(aRect);
    }
}
