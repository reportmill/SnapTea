package snaptea;
import java.util.*;
import java.util.function.Consumer;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSString;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import snap.gfx.Image;
import snap.view.*;

/**
 * A snap Clipboard implementation for TeaVM.
 */
public class TVClipboard extends Clipboard {
    
    // The view event
    private ViewEvent  _viewEvent;
    
    // The DataTransfer
    private DataTransfer  _dataTrans;
    
    // The shared clipboards for system and drag
    private static TVClipboard  _shared;
    private static TVClipboard  _sharedDrag;

    /**
     * Returns the clipboard content.
     */
    protected boolean hasDataImpl(String aMimeType)
    {
        // If no DataTransfer, just return normal version
        if (_dataTrans==null)
            return super.hasDataImpl(aMimeType);

        if (aMimeType==FILE_LIST)
            return _dataTrans.getFileCount()>0;
        return _dataTrans.hasType(aMimeType);
    }

    /**
     * Returns the clipboard content.
     */
    protected ClipboardData getDataImpl(String aMimeType)
    {
        // If no DataTransfer, just return normal version
        if (_dataTrans==null)
            return super.getDataImpl(aMimeType);

        Object data;

        // Handle Files
        if (aMimeType==FILE_LIST) {
            File jsfiles[] = _dataTrans.getFiles(); if (jsfiles==null) return null;
            List <ClipboardData> cfiles = new ArrayList(jsfiles.length);
            for (File jsfile : jsfiles) {
                ClipboardData cbfile = new TVClipboardData(jsfile);
                cfiles.add(cbfile);
            }
            data = cfiles;
        }

        // Handle anything else (String data)
        else data = _dataTrans.getData(aMimeType);

        // Return ClipboardData for data
        return new ClipboardData(aMimeType, data);
    }

    /**
     * Adds clipboard content.
     */
    protected void addDataImpl(String aMimeType, ClipboardData aData)
    {
        // Do normal implementation to populate ClipboardDatas map
        super.addDataImpl(aMimeType, aData);

        // If no DataTransfer, just return
        if (_dataTrans==null) return;

        // Handle string data
        if (aData.isString())
            _dataTrans.setData(aMimeType, aData.getString());

        // Otherwise complain
        else System.err.println("CJClipboard.addDataImpl: Unsupported data type: " + aMimeType + ", " + aData.getSource());
    }

    /**
     * Starts the drag.
     */
    public void startDrag()
    {
        // Set Dragging true and consume event
        isDragging = true;
        _viewEvent.consume();

        // Get drag image and point and set in DataTransfer
        Image dimg = getDragImage();
        if (dimg==null) dimg = Image.get(1,1,true);
        HTMLElement img = (HTMLElement)dimg.getNative();
        double dx = getDragImageOffset().x;
        double dy = getDragImageOffset().y;
        _dataTrans.setDragImage(img, dx, dy);

        // Add image element to canvas so browsers can generate image (then remove a short time later)
        HTMLElement body = HTMLDocument.current().getBody();
        body.appendChild(img);
        TVViewEnv.get().runDelayed(() -> { isDragging = false; body.removeChild(img); }, 100, false);
    }

    public static boolean isDragging;

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
        DragEvent dragEvent = (DragEvent)anEvent.getEvent();
        JSDataTransfer jsdt = dragEvent.getDataTransfer();
        _dataTrans = DataTransfer.getDataTrasferForJSDataTransfer(jsdt);
    }

    /**
     * Returns the shared TVClipboard.
     */
    public static TVClipboard get()
    {
        if (_shared!=null) return _shared;
        return _shared = new TVClipboard();
    }

    /**
     * If Clipboard needs to be 'approved', get approved and call given consumer.
     */
    public void getApprovedClipboardAndRun(Consumer<Clipboard> aConsumer)
    {
        _clipboardConsumer = aConsumer;
        JSPromise<JSObject> rval = getReadPermissionsPromise();
        if (rval!=null)
            rval.then(perm -> didGetPermissions(perm));
        else didGetPermissions(null);
    }

    private static Consumer<Clipboard>  _clipboardConsumer;

    /**
     * Returns a Promise for read permissions.
     */
    @JSBody(params={ }, script = "return navigator.permissions ? navigator.permissions.query({name: 'clipboard-read'}) : null;")
    public static native JSPromise<JSObject> getReadPermissionsPromise();

    private static JSPromise didGetPermissions(JSObject aPermResult)
    {
        //getState(aPermResult);
        //System.out.println("Got Read Permissions: " + aPermResult!=null ? getState(aPermResult) : "null");
        JSPromise<JSString> rval = getClipboardReadTextPromise();
        rval.then(str -> didGetClipboardReadText(str));
        return rval;
    }

    @JSBody(params={ }, script = "return navigator.clipboard.readText();")
    public static native JSPromise<JSString> getClipboardReadTextPromise();

    /**
     * Returns the system DataTransfer.
     */
    private static JSPromise didGetClipboardReadText(JSString aStr)
    {
        System.out.println("Got DataTransfer: " + (aStr!=null ? "Yes" : "Failed!"));
        String str = aStr.stringValue();
        _shared._dataTrans = DataTransfer.getDataTrasferForString(str);
        ViewUtils.runLater(() -> _clipboardConsumer.accept(_shared));
        return null;
    }

    //JSPromise<JSArray<JSDataTransferItem>> rval = getClipboardReadPromise();
    //rval.then(dataTransfer -> didGetClipboardReadDataTransfer(dataTransfer));
    //@JSBody(params={ }, script = "return navigator.clipboard.read();")
    //public static native JSPromise<JSArray<JSDataTransferItem>> getClipboardReadPromise();
    /*private static JSPromise didGetClipboardReadDataTransfer(JSArray<JSDataTransferItem> theDTIs) {
        System.out.println("Got DataTransfers: " + (theDTIs!=null ? theDTIs.getLength() : "Failed!"));TV.log(theDTIs);
        _shared._dataTrans = DataTransfer.getDataTrasferForDataTransferItemArray(theDTIs);
        ViewUtils.runLater(() -> _clipboardConsumer.accept(_shared)); return null; } */

    @JSBody(params={ "aPermResult" }, script = "console.log(aPermResult); return aPermResult.state;")
    static native String getState(JSObject aPermResult);

    /**
     * Returns the shared TVClipboard for drag and drop.
     */
    public static TVClipboard getDrag(ViewEvent anEvent)
    {
        if (_sharedDrag==null) _sharedDrag = new TVClipboard();
        if (anEvent!=null) _sharedDrag.setEvent(anEvent);
        return _sharedDrag;
    }

    /**
     * A ClipboardData subclass to read JS File bytes asynchronously.
     */
    private static class TVClipboardData extends ClipboardData {

        /** Creates ClipboardData for given JS File and starts loading. */
        public TVClipboardData(File aFile)
        {
            super(aFile.getType(), null);
            setName(aFile.getName());
            setLoaded(false);
            FileReader fr = new FileReader();
            fr.readBytesAndRunLater(aFile, () -> fileReaderDidLoad(fr));
        }

        /** Called when FileReader finishes reading bytes. */
        void fileReaderDidLoad(FileReader aFR)
        {
            byte bytes[] = aFR.getResultBytes();
            setBytes(bytes);
        }
    }
}