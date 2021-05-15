package snaptea;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import snap.gfx.Image;
import snap.view.ViewEvent;

/**
 * A TVClipboard subclass to support drag and drop.
 */
public class TVDragboard extends TVClipboard {

    // The view event
    private ViewEvent  _viewEvent;

    // Whether dragging is in progress
    public static boolean isDragging;

    // The shared clipboard for system drag/drop
    private static TVDragboard  _sharedDrag;

    /**
     * Starts the drag.
     */
    public void startDrag()
    {
        // Set Dragging true and consume event
        isDragging = true;
        _viewEvent.consume();

        // Get drag image
        Image dimg = getDragImage();
        if (dimg == null)
            dimg = Image.get(1,1,true);

        // Get native HTML element for image
        HTMLElement img = (HTMLElement) dimg.getNative();
        double dx = getDragImageOffset().x;
        double dy = getDragImageOffset().y;

        // Start Drag
        _dataTrans.setDragImage(img, dx, dy);

        // Add image element to canvas so browsers can generate image
        HTMLElement body = HTMLDocument.current().getBody();
        body.appendChild(img);

        // Register to remove element a short time later
        TVViewEnv.get().runDelayed(() -> {
            isDragging = false;
            body.removeChild(img);
        }, 100, false);
    }

    /** Called to indicate that drop is accepted. */
    public void acceptDrag()  { }

    /** Called to indicate that drop is complete. */
    public void dropComplete()  { }

    /**
     * Sets the current event.
     */
    protected void setEvent(ViewEvent anEvent)
    {
        _viewEvent = anEvent;
        DragEvent dragEvent = (DragEvent) anEvent.getEvent();
        JSDataTransfer jsdt = dragEvent.getDataTransfer();
        _dataTrans = DataTransfer.getDataTrasferForJSDataTransfer(jsdt);
    }

    /**
     * Returns the shared TVClipboard for drag and drop.
     */
    public static TVClipboard getDrag(ViewEvent anEvent)
    {
        if (_sharedDrag == null)
            _sharedDrag = new TVDragboard();
        if (anEvent != null)
            _sharedDrag.setEvent(anEvent);
        return _sharedDrag;
    }
}
