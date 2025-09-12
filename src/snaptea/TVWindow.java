package snaptea;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.html.*;
import snap.geom.Point;
import snap.geom.Rect;
import snap.gfx.*;
import snap.props.PropChange;
import snap.props.PropChangeListener;
import snap.view.*;

/**
 * A class to represent the WindowView in the browser page.
 */
public class TVWindow {

    // The Window View
    protected WindowView  _win;

    // The element to represent the window
    protected HTMLElement  _winEmt;

    // The RootView
    protected RootView  _rootView;

    // The HTML document element
    protected HTMLDocument  _doc;

    // The HTML body element
    protected HTMLBodyElement  _body;

    // The parent element
    protected HTMLElement  _parent;

    // The HTMLCanvas
    protected HTMLCanvasElement _canvas;

    // Painter
    private Painter _painter;

    // A listener for hide
    protected PropChangeListener  _hideLsnr;

    // A listener for browser window resize
    protected EventListener<?> _resizeLsnr = null;

    // The body overflow value
    protected String  _bodyMargin = "undefined", _bodyOverflow;

    // Whether content is editable
    private boolean _contentEditable;

    // The last top window
    protected static int  _topWin;

    // The paint scale
    public static int PIXEL_SCALE = TV.getDevicePixelRatio() == 2 ? 2 : 1;

    /**
     * Constructor.
     */
    public TVWindow(WindowView aWin)
    {
        _win = aWin;
        ViewUtils.setNative(_win, this);

        // Start listening to bounds, Maximized and ActiveCursor changes
        _win.addPropChangeListener(pc -> handleSnapWindowFocusViewChange(), WindowView.FocusView_Prop);
        _win.addPropChangeListener(pc -> handleSnapWindowMaximizedChange(), WindowView.Maximized_Prop);
        _win.addPropChangeListener(pc -> handleSnapWindowBoundsChange(pc), View.X_Prop, View.Y_Prop,
                View.Width_Prop, View.Height_Prop);
        _win.addPropChangeListener(pc -> handleSnapWindowActiveCursorChange(), WindowView.ActiveCursor_Prop);

        // Get Doc and body elements
        _doc = HTMLDocument.current();
        _body = _doc.getBody();

        // Create/configure WinEmt, the HTMLElement to hold window and canvas
        _winEmt = _doc.createElement("div");
        _winEmt.getStyle().setProperty("box-sizing", "border-box");
        _winEmt.getStyle().setProperty("background", "#F4F4F4CC");

        // Get RootView and TVRootView
        _rootView = _win.getRootView();

        // Create canvas and configure to totally fill window element (minus padding insets)
        _canvas = (HTMLCanvasElement)HTMLDocument.current().createElement("canvas");
        _canvas.getStyle().setProperty("width", "100%");
        _canvas.getStyle().setProperty("height", "100%");
        _canvas.getStyle().setProperty("box-sizing", "border-box");

        // Add RootView listener to propagate size changes to canvas
        _rootView.addPropChangeListener(pc -> handleRootViewSizeChange(), View.Width_Prop, View.Height_Prop);
        handleRootViewSizeChange();

        // Have to do this so TouchEvent.preventDefault doesn't complain and iOS doesn't scroll doc
        _canvas.getStyle().setProperty("touch-action", "none");
        _canvas.setAttribute("touch-action", "none");
        _canvas.addEventListener("touchstart", e -> e.preventDefault());
        _canvas.addEventListener("touchmove", e -> e.preventDefault());
        _canvas.addEventListener("touchend", e -> e.preventDefault());
        _canvas.addEventListener("wheel", e -> e.preventDefault());

        // Create painer
        _painter = new TVPainter(_canvas, PIXEL_SCALE);

        // Register for drop events
        EventListener<?> dragLsnr = e -> handleDragEvent((DragEvent)e);
        _canvas.addEventListener("dragenter", dragLsnr);
        _canvas.addEventListener("dragover", dragLsnr);
        _canvas.addEventListener("dragexit", dragLsnr);
        _canvas.addEventListener("drop", dragLsnr);
        _canvas.setAttribute("draggable", "true");

        // Register for drag start event
        _canvas.addEventListener("dragstart", e -> handleDragGesture((DragEvent)e));
        _canvas.addEventListener("dragend", e -> handleDragEnd((DragEvent)e));

        // Get RootView canvas and add to WinEmt
        HTMLCanvasElement canvas = getCanvas();
        _winEmt.appendChild(canvas);
    }

    /**
     * Initializes window.
     */
    public void initWindow()
    {
        if (_rootView.getFill() == null)
            _rootView.setFill(ViewUtils.getBackFill());
        if (_rootView.getBorder() == null)
            _rootView.setBorder(Color.GRAY, 1);
    }

    /**
     * Returns the canvas for the window.
     */
    public HTMLCanvasElement getCanvas()  { return _canvas; }

    /**
     * Returns the parent DOM element of this window (WinEmt).
     */
    public HTMLElement getParent()  { return _parent; }

    /**
     * Sets the parent DOM element of this window (WinEmt).
     */
    protected void setParent(HTMLElement aNode)
    {
        // If already set, just return
        if (aNode == _parent) return;

        // Set new value
        HTMLElement par = _parent;
        _parent = aNode;

        // If null, just remove from old parent and return
        if (aNode == null) {
            par.removeChild(_winEmt);
            return;
        }

        // Add WinEmt to given node
        aNode.appendChild(_winEmt);

        // If body, configure special
        if (aNode == _body) {

            // Set body and html height so that document covers the whole browser page
            HTMLHtmlElement html = _doc.getDocumentElement();
            html.getStyle().setProperty("height", "100%");
            _body.getStyle().setProperty("min-height", "100%");
            _bodyMargin = _body.getStyle().getPropertyValue("margin");
            _body.getStyle().setProperty("margin", "0");

            // Configure WinEmt for body
            _winEmt.getStyle().setProperty("position", _win.isMaximized() ? "fixed" : "absolute");
            _winEmt.getStyle().setProperty("z-index", String.valueOf(_topWin++));

            // If not maximized, clear background and add drop shadow
            if (!_win.isMaximized()) {
                _winEmt.getStyle().setProperty("background", null);
                _winEmt.getStyle().setProperty("box-shadow", "1px 1px 8px grey");
            }
        }

        // If arbitrary element
        else {
            if (_bodyMargin != "undefined")
                _body.getStyle().setProperty("margin", _bodyMargin);
            _winEmt.getStyle().setProperty("position", "static");
            _winEmt.getStyle().setProperty("width", "100%");
            _winEmt.getStyle().setProperty("height", "100%");
        }
    }

    /**
     * Returns the parent DOM element of this window.
     */
    private HTMLElement getParentForWin()
    {
        // If window is maximized, parent should always be body
        if (_win.isMaximized())
            return _body;

        // If window has named element, return that
        String parentName = _win.getName();
        if (parentName != null) {
            HTMLElement par = _doc.getElementById(parentName);
            if (par != null)
                return par;
        }

        // Default to body
        return _body;
    }

    /**
     * Returns whether window is child of body.
     */
    private boolean isChildOfBody()
    {
        return getParent() == _body;
    }

    /**
     * Resets the parent DOM element and Window/WinEmt bounds.
     */
    protected void resetParentAndBounds()
    {
        // Get proper parent node and set
        HTMLElement par = getParentForWin();
        setParent(par);

        // If window floating in body, set WinEmt bounds from Window
        if (par == _body) {
            if (_win.isMaximized())
                _win.setBounds(TV.getViewportBounds());
            handleSnapWindowBoundsChange(null);
        }

        // If window in DOM container element
        else handleBrowserWindowSizeChange();
    }

    /**
     * Shows window.
     */
    public void show()
    {
        if (_win.isModal())
            showModal();
        else showImpl();
    }

    /**
     * Shows window.
     */
    public void showImpl()
    {
        // Make sure WinEmt is in proper parent node with proper bounds
        resetParentAndBounds();

        // Add to Screen.Windows
        TVScreen screen = TVScreen.get();
        screen.addWindow(_win);

        // Set Window showing
        ViewUtils.setShowing(_win, true);
        ViewUtils.setFocused(_win, true);

        // Start listening to browser window resizes
        if (_resizeLsnr == null)
            _resizeLsnr = e -> TVEnv.runOnAppThread(this::handleBrowserWindowSizeChange);
        Window.current().addEventListener("resize", _resizeLsnr);
    }

    /**
     * Shows modal window.
     */
    protected synchronized void showModal()
    {
        // Do normal show
        showImpl();

        // Register listener to activate current thread on window not showing
        _win.addPropChangeListener(_hideLsnr = pc -> handleSnapWindowShowingChange(), View.Showing_Prop);

        // Start new app thread, since this thread is now tied up until window closes
        TVEnv.get().startNewAppThread();

        // Wait until window is hidden
        try { wait(); }
        catch(Exception e) { throw new RuntimeException(e); }
    }

    /**
     * Hides window.
     */
    public void hide()
    {
        // Remove WinEmt from parent
        setParent(null);

        // Remove Window from screen
        TVScreen screen = TVScreen.get();
        screen.removeWindow(_win);

        // Set Window not showing
        ViewUtils.setShowing(_win, false);
        ViewUtils.setFocused(_win, false);

        // Stop listening to browser window resizes
        Window.current().removeEventListener("resize", _resizeLsnr);
        _resizeLsnr = null;

        // Send WinClose event
        if (_win.getEventAdapter().isEnabled(ViewEvent.Type.WinClose)) {
            ViewEvent event = ViewEvent.createEvent(_win, null, ViewEvent.Type.WinClose, null);
            _win.dispatchEventToWindow(event);
        }
    }

    /**
     * Window/Popup method: Order window to front.
     */
    public void toFront()
    {
        _winEmt.getStyle().setProperty("z-index", String.valueOf(_topWin++));
    }

    /**
     * Called to register for repaint.
     */
    public void paintViews(Rect aRect)
    {
        _painter.setTransform(1,0,0,1,0,0); // I don't know why I need this!
        ViewUpdater updater = _rootView.getUpdater();
        updater.paintViews(_painter, aRect);
    }

    /**
     * Called when browser window resizes.
     */
    private void handleBrowserWindowSizeChange()
    {
        // If Window is child of body, just return
        if (isChildOfBody()) {
            if (_win.isMaximized())
                _win.setBounds(TV.getViewportBounds());
            return;
        }

        // Reset window location
        HTMLElement parent = getParent();
        Point off = TV.getOffsetAll(parent);
        _win.setXY(off.x, off.y);

        // Reset window size
        int parW = parent.getClientWidth();
        int parH = parent.getClientHeight();
        _win.setSize(parW, parH);
        _win.repaint();
    }

    /**
     * Called when window changes showing.
     */
    private synchronized void handleSnapWindowShowingChange()
    {
        _win.removePropChangeListener(_hideLsnr);
        _hideLsnr = null;
        notify();
    }

    /**
     * Notifies that focus changed.
     */
    private void handleSnapWindowFocusViewChange()
    {
        View focusView = _win.getFocusedView();
        boolean isText = focusView instanceof TextArea || focusView instanceof TextField;
        setContentEditable(isText);
    }

    /**
     * Called when WindowView has bounds change to sync to WinEmt.
     */
    private void handleSnapWindowBoundsChange(PropChange propChange)
    {
        // If Window not child of body, just return (parent node changes go to win, not win to parent)
        if (!isChildOfBody()) return;

        // Get bounds x, y, width, height and PropChange name
        int x = (int) Math.round(_win.getX());
        int y = (int) Math.round(_win.getY());
        int w = (int) Math.round(_win.getWidth());
        int h = (int) Math.round(_win.getHeight());
        String propName = propChange != null ? propChange.getPropName() : null;

        // Handle changes
        if (propName == null || propName == View.X_Prop)
            _winEmt.getStyle().setProperty("left", x + "px");
        if (propName == null || propName == View.Y_Prop)
            _winEmt.getStyle().setProperty("top", y + "px");
        if (propName == null || propName == View.Width_Prop)
            _winEmt.getStyle().setProperty("width", w + "px");
        if (propName == null || propName == View.Height_Prop)
            _winEmt.getStyle().setProperty("height", h + "px");
    }

    /**
     * Called when WindowView.Maximized is changed.
     */
    private void handleSnapWindowMaximizedChange()
    {
        // Get canvas
        HTMLCanvasElement canvas = getCanvas();

        // Handle Maximized on
        if (_win.isMaximized()) {

            // Set body overflow to hidden (to get rid of scrollbars)
            _bodyOverflow = _body.getStyle().getPropertyValue("overflow");
            _body.getStyle().setProperty("overflow", "hidden");

            // Set Window/WinEmt padding
            _win.setPadding(5, 5, 5, 5);
            _winEmt.getStyle().setProperty("padding", "5px");

            // Add a shadow to canvas
            canvas.getStyle().setProperty("box-shadow", "1px 1px 8px grey");
        }

        // Handle Maximized off
        else {

            // Restore body overflow
            _body.getStyle().setProperty("overflow", _bodyOverflow);

            // Clear Window/WinEmt padding
            _win.setPadding(0, 0, 0, 0);
            _winEmt.getStyle().setProperty("padding", null);

            // Remove shadow from canvas
            canvas.getStyle().setProperty("box-shadow", null);
        }

        // Reset parent and Window/WinEmt bounds
        resetParentAndBounds();
    }

    /**
     * Sets the cursor.
     */
    private void handleSnapWindowActiveCursorChange()
    {
        Cursor aCursor = _win.getActiveCursor();
        String cstr = "default";
        if (aCursor == Cursor.DEFAULT) cstr = "default";
        if (aCursor == Cursor.CROSSHAIR) cstr = "crosshair";
        if (aCursor == Cursor.HAND) cstr = "pointer";
        if (aCursor == Cursor.MOVE) cstr = "move";
        if (aCursor == Cursor.TEXT) cstr = "text";
        if (aCursor == Cursor.NONE) cstr = "none";
        if (aCursor == Cursor.N_RESIZE) cstr = "n-resize";
        if (aCursor == Cursor.S_RESIZE) cstr = "s-resize";
        if (aCursor == Cursor.E_RESIZE) cstr = "e-resize";
        if (aCursor == Cursor.W_RESIZE) cstr = "w-resize";
        if (aCursor == Cursor.NE_RESIZE) cstr = "ne-resize";
        if (aCursor == Cursor.NW_RESIZE) cstr = "nw-resize";
        if (aCursor == Cursor.SE_RESIZE) cstr = "se-resize";
        if (aCursor == Cursor.SW_RESIZE) cstr = "sw-resize";
        getCanvas().getStyle().setProperty("cursor", cstr);
    }

    /**
     * Called when root view size changes.
     */
    private void handleRootViewSizeChange()
    {
        int rootW = (int) Math.ceil(_rootView.getWidth());
        int rootH = (int) Math.ceil(_rootView.getHeight());
        _canvas.setWidth(rootW * PIXEL_SCALE);
        _canvas.setHeight(rootH * PIXEL_SCALE);
    }

    /**
     * Called to handle a drag event.
     * Not called on app thread, because drop data must be processed when event is issued.
     * TVEnv.runOnAppThread(() -> handleDragEvent(anEvent));
     */
    private void handleDragEvent(DragEvent anEvent)
    {
        anEvent.preventDefault();
        ViewEvent event = ViewEvent.createEvent(_rootView, anEvent, null, null);
        _rootView.getWindow().dispatchEventToWindow(event);
    }

    /** Called to handle a drag event. */
    private void handleDragGesture(DragEvent anEvent)
    {
        ViewEvent event = ViewEvent.createEvent(_rootView, anEvent, null, null);
        _rootView.getWindow().dispatchEventToWindow(event);
        if (!TVDragboard.isDragging) {
            anEvent.preventDefault();
            anEvent.stopPropagation();
        }
    }

    /** Called to handle dragend event. */
    private void handleDragEnd(DragEvent anEvent)
    {
        ViewEvent event = ViewEvent.createEvent(_rootView, anEvent, null, null);
        _rootView.getWindow().dispatchEventToWindow(event);
    }

    /**
     * Sets ContentEditable on canvas.
     */
    private void setContentEditable(boolean aValue)
    {
        // If already set, just return
        if (aValue == _contentEditable) return;

        // Set value
        _contentEditable = aValue;

        // Update Body.ContentEditable and TabIndex
        HTMLDocument doc = HTMLDocument.current();
        HTMLBodyElement body = doc.getBody();
        TV.setContentEditable(body, aValue);

        // Focus element
        body.focus();
    }
}