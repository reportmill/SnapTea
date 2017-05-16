package snaptea;
import snap.gfx.Size;
import snap.view.*;

/**
 * A custom class.
 */
public class TVWindowHpr <T extends TVWindow> extends ViewHelper <T> {

/** Creates the native. */
protected T createNative()  { return (T)new TVWindow(); }

/** Override to get view as WindowView. */
public WindowView getView()  { return (WindowView)super.getView(); }
    
/** Override to set view in RootView. */
public void setView(View aView)  { super.setView(aView); get().setView((WindowView)aView); }
    
/** Window/Popup method: Shows the window at given point relative to given view. */
public void show(View aView, double aX, double aY)
{
    WindowView wview = getView();
    
    View c = wview.getRootView().getContent();
    if(c instanceof Label || c instanceof ButtonBase) { c.setPadding(4,6,4,6); c.setFont(c.getFont().deriveFont(14));
        Box box = new Box(c); box.setPadding(4,4,4,4); wview.getRootView().setContent(box); }

    setPrefSize();
    
    TVWindow win = (TVWindow)wview.getNative();
    win.show();
    
    wview.setShowing(true);
}

/** Window/Popup method: Hides the window. */
public void hide()
{
    WindowView wview = getView();
    TVWindow win = (TVWindow)wview.getNative();
    win.hide();
}

/** Window/Popup method: Sets the window size to preferred size. */
public void setPrefSize()
{
    WindowView wview = getView();
    Size size = wview.getRootView().getPrefSize();
    wview.setSize(size.width, size.height);
}

}