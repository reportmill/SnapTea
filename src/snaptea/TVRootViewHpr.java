package snaptea;
import snap.gfx.Rect;
import snap.view.*;

/**
 * A custom class.
 */
public class TVRootViewHpr <T extends TVRootView> extends ViewHelper <T> {

    /** Creates the native. */
    protected T createNative()  { return (T)new TVRootView(); }

    /** Override to set view in RootView. */
    public void setView(View aView)  { super.setView(aView); get().setView(aView); }
    
    /** Sets the cursor. */
    public void setCursor(Cursor aCursor)  { } //get().setCursor(AWT.get(aCursor)); }
    
    /** Registers a view for repaint. */
    public void requestPaint(Rect aRect)  { get().repaint(aRect); }
}