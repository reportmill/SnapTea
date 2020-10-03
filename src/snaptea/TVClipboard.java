package snaptea;
import java.util.*;
import java.util.function.Consumer;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSArray;
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

    // The runnable to call addAllDatas()
    private Runnable  _addAllDatasRun, ADD_ALL_DATAS_RUN = () -> addAllDataToClipboard();
    
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
            List <ClipboardData> cfiles = new ArrayList<>(jsfiles.length);
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

        // Handle DragDrop case
        if (_dataTrans!=null) {

            // Handle string data
            if (aData.isString())
                _dataTrans.setData(aMimeType, aData.getString());

                // Otherwise complain
            else System.err.println("TVClipboard.addDataImpl: Unsupported data type: " + aMimeType + ", " + aData.getSource());
        }

        // Handle system clipboard copy: Wait till all types added, then update clipboard
        else {
            if (_addAllDatasRun==null)
                ViewUtils.runLater(_addAllDatasRun=ADD_ALL_DATAS_RUN);
        }
    }

    /**
     * Load datas into system clipboard
     */
    private void addAllDataToClipboard()
    {
        // Clear run
        _addAllDatasRun = null;

        // Get list of ClipbardData
        Map<String,ClipboardData> clipDataMap = getClipboardDatas();
        Collection<ClipboardData> clipDataList = clipDataMap.values();

        // Convert to list of JSClipboardItem
        List<JSClipboardItem> clipItemsList = new ArrayList<>();
        for (ClipboardData cdata : clipDataList) {
            JSClipboardItem clipboardItem = getJSClipboardItemForClipboardData(cdata);
            if (clipboardItem!=null)
                clipItemsList.add(clipboardItem);
        }

        // Convert to JSArray of JSClipboardItem
        JSClipboardItem clipItems[] = clipItemsList.toArray(new JSClipboardItem[0]);
        JSArray<JSClipboardItem> cbitems = JSArray.of(clipItems);

        // Write to system clipboard
        JSPromise writePromise = getClipboardWriteItemsPromise(cbitems);
        if (writePromise!=null)
            writePromise.catch_(JSObject -> { System.err.println("TVClipboard.addAllDataToClipboard failed"); return null; });

        // Clear datas
        clearData();
    }

    /**
     * Returns a JSClipboardItem for given ClipboardData.
     */
    private JSClipboardItem getJSClipboardItemForClipboardData(ClipboardData aData)
    {
        // Handle image
        if (aData.isImage()) {

            // Get image as PNG blob
            TVImage img = (TVImage) aData.getImage();
            byte bytes[] = img.getBytesPNG();
            Blob blob = TV.createBlob(bytes, "image/png");

            // Get ClipboardItem array for blob
            return getJSClipboardItem(blob);
        }

        else {

            // Get type and bytes
            String type = aData.getMIMEType();
            byte bytes[] = aData.getBytes();

            // If valid, just wrap in JSClipboardItem
            if (type!=null && bytes!=null && bytes.length>0) {
                Blob blob = TV.createBlob(bytes, type);
                return getJSClipboardItem(blob);
            }
        }

        // Complain and return null
        System.err.println("TVClipboard.getJSClipboardItemForClipboardData: Had problem with " + aData);
        return null;
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

    @JSBody(params={ "theItems" }, script = "return navigator.clipboard.write(theItems);")
    public static native JSPromise<JSString> getClipboardWriteItemsPromise(JSArray<JSClipboardItem> theItems);

    @JSBody(params={ "blob" }, script = "var param = {}; param[blob.type] = blob; return new ClipboardItem(param);")
    public static native JSClipboardItem getJSClipboardItem(JSObject blob);

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