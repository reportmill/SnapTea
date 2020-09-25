package snaptea;
import snap.geom.*;
import snap.gfx.*;
import snap.view.*;

/**
 * A View to paint a window bar.
 */
public class WindowBar extends ParentView {

    // The content
    private View  _content;
    
    // The title bar height
    private double  _titlebarHeight;
    
    // The ButtonArea
    private ButtonArea  _btnArea;
    
    // The buttons
    private Shape  _closeButton, _minButton, _maxButton;
    
    // The titlebar font
    private Font  _font;
    
    // For dragging
    private Point  _mpt;
    
    // Colors
    static final Color CLOSE_COLOR = new Color("#ED6B5F");
    static final Color CLOSE_COLOR2 = CLOSE_COLOR.blend(Color.BLACK,.2);
    static final Color MIN_COLOR = new Color("#F5BF4F");
    static final Color MIN_COLOR2 = MIN_COLOR.blend(Color.BLACK,.2);
    static final Color MAX_COLOR = new Color("#62C654");
    static final Color MAX_COLOR2 = MAX_COLOR.blend(Color.BLACK,.2);
    
    /**
     * Creates a WindowBar.
     */
    public WindowBar(View aView)
    {
        WindowView win = aView.getWindow();
        double tth = win.getType()==WindowView.TYPE_MAIN ? 24 : 18;
        setTitlebarHeight(tth);
        enableEvents(MousePress, MouseDrag, MouseRelease);
        setContent(aView);
    }

    /**
     * Returns the content.
     */
    public View getContent()  { return _content; }

    /**
     * Sets the content.
     */
    public void setContent(View aView)
    {
        _content = aView;
        addChild(_content);
    }

    /**
     * Sets the titlebar height.
     */
    public void setTitlebarHeight(double aValue)
    {
        _titlebarHeight = aValue;
        setPadding(_titlebarHeight,0,0,0);

        // Create ButtonArea to paint title bar
        _btnArea = new ButtonArea();
        _btnArea.setHeight(_titlebarHeight);
        _btnArea.setRadius(4);
        _btnArea.setPosition(Pos.TOP_CENTER);

        // Create buttons
        double y = 6, w = 12; if (_titlebarHeight!=24) { y = 4; w = 10; }
        _closeButton = new Arc(10,y,w,w,0,360);
        _minButton = new Arc(30,y,w,w,0,360);
        _maxButton = new Arc(50,y,w,w,0,360);
        double fontSize = _titlebarHeight==24 ? 14 : 11;
        _font = Font.Arial10.deriveFont(fontSize);
    }

    /**
     * Override to paint titlebar.
     */
    protected void paintFront(Painter aPntr)
    {
        if (_titlebarHeight==0) return;

        // Paint titlebar
        _btnArea.setWidth(getWidth());
        _btnArea.paint(aPntr);

        // Paint buttons
        aPntr.setStroke(Stroke.getStroke(.5));
        if (_closeButton!=null) {
            aPntr.setColor(CLOSE_COLOR); aPntr.fill(_closeButton);
            aPntr.setColor(CLOSE_COLOR2); aPntr.draw(_closeButton);
        }
        if (_minButton!=null) {
            aPntr.setColor(MIN_COLOR); aPntr.fill(_minButton);
            aPntr.setColor(MIN_COLOR2); aPntr.draw(_minButton);
        }
        if(_maxButton!=null) {
            aPntr.setColor(MAX_COLOR); aPntr.fill(_maxButton);
            aPntr.setColor(MAX_COLOR2); aPntr.draw(_maxButton);
        }
        aPntr.setStroke(Stroke.getStroke(1));

        // Paint title
        String title = getWindow().getTitle();
        if (title!=null) {
            double y = _titlebarHeight==24? 4 : 3;
            double strW = _font.getStringAdvance(title);
            double x = Math.round((getWidth() - strW)/2);
            aPntr.setColor(Color.DARKGRAY); aPntr.setFont(_font);
            aPntr.drawString(title, x, y + _font.getAscent());
        }
    }

    /**
     * Override to handle events.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        anEvent.consume();

        // Handle MousePress: Update MousePoint
        if (anEvent.isMousePress()) {
            _mpt = anEvent.getY()<=_titlebarHeight ? anEvent.getPoint(null) : null;
            return;
        }

        // Handle MouseDrag
        if (anEvent.isMouseDrag())
            mouseDrag(anEvent);

        // Handle MouseRelease: Clear MousePoint
        if (anEvent.isMouseRelease())
            mouseRelease(anEvent);
    }

    /**
     * Called on MouseDrag event.
     */
    private void mouseDrag(ViewEvent anEvent)
    {
        // If Maximized, just return
        WindowView win = getWindow();
        if (win.isMaximized()) return;

        // Update MousePoint
        Point mpt = _mpt; if (mpt==null) return;
        _mpt = anEvent.getPoint(null);

        // Update Window XY
        double x2 = win.getX() + (_mpt.x - mpt.x);
        double y2 = win.getY() + (_mpt.y - mpt.y);
        win.setXY(x2, y2);
    }

    /**
     * Called on MouseRelease event.
     */
    private void mouseRelease(ViewEvent anEvent)
    {
        // If click hit CloseButton, close window
        Point pnt = anEvent.getPoint();
        if (_closeButton.contains(pnt.x, pnt.y)) {
            WindowView win = getWindow();
            win.setVisible(false);
        }

        _mpt = null;
    }

    /**
     * Override to return preferred width of content.
     */
    protected double getPrefWidthImpl(double aH)
    {
        return BoxView.getPrefWidth(this, getContent(), aH);
    }

    /**
     * Override to return preferred height of content.
     */
    protected double getPrefHeightImpl(double aW)
    {
        return BoxView.getPrefHeight(this, getContent(), aW);
    }

    /**
     * Override to layout content.
     */
    protected void layoutImpl()
    {
        BoxView.layout(this, getContent(), null, true, true);
    }

    /**
     * Attaches a WindowBar to a view.
     */
    public static WindowBar attachWindowBar(View aView)
    {
        RootView rview = aView.getRootView();
        View content = rview.getContent();
        if (content instanceof WindowBar) return (WindowBar)content;
        Size size = rview.getSize();
        Size psize = rview.getPrefSize();
        WindowBar wbar = new WindowBar(content);
        rview.setContent(wbar);
        if (size.equals(psize))
            rview.getWindow().setSize(rview.getWindow().getPrefSize());
        return wbar;
    }

    /**
     * Detaches a WindowBar to a view.
     */
    public static void detachWindowBar(View aView)
    {
        RootView rview = aView.getRootView();
        View content = rview.getContent();
        WindowBar wbar = content instanceof WindowBar ? (WindowBar)content : null; if (wbar==null) return;
        rview.setContent(wbar.getContent());
    }
}