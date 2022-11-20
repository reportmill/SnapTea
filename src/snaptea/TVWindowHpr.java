package snaptea;
import snap.geom.Rect;
import snap.view.TextArea;
import snap.view.TextField;
import snap.view.View;
import snap.view.WindowView;

/**
 * A WindowHpr to map WindowView to TVWindow.
 */
public class TVWindowHpr extends WindowView.WindowHpr<TVWindow> {

    // The snap Window
    protected WindowView  _win;

    // The snap TVWindow
    protected TVWindow  _winNtv;

    // Whether content is editable
    private boolean _contentEditable;

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
        _winNtv = new TVWindow();
        _winNtv.setWindow(aWin);
    }

    /**
     * Returns the native.
     */
    public TVWindow getNative()  { return _winNtv; }

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

    /**
     * Notifies that focus changed.
     */
    public void focusDidChange(View aView)
    {
        boolean isText = aView instanceof TextArea || aView instanceof TextField;
        setContentEditable(isText);
    }

    /**
     * Sets ContentEditable on canvas.
     */
    public void setContentEditable(boolean aValue)
    {
        // If already set, just return
        if (aValue == _contentEditable) return;

        // Set value
        _contentEditable = aValue;

        // Update Canvas.ContentEditable and TabIndex
        TVRootView rootViewTV = _winNtv._rootViewNtv;
        TV.setContentEditable(rootViewTV._canvas, aValue);

        // Focus element
        rootViewTV._canvas.focus();
    }
}
