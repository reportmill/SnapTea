package snaptea;
import java.util.*;

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
    
    // The DataTransfer
    protected DataTransfer  _dataTrans;

    // The runnable to call addAllDatas()
    private Runnable  _addAllDatasRun, ADD_ALL_DATAS_RUN = () -> addAllDataToClipboard();

    // Whether clipboard is loaded
    private boolean  _loaded;

    // A LoadListener to handle async browser clipboard
    private static Runnable  _loadListener;

    // The shared clipboard for system copy/paste
    private static TVClipboard  _shared;

    /**
     * Returns the clipboard content.
     */
    protected boolean hasDataImpl(String aMimeType)
    {
        // If no DataTransfer, just return normal version
        if (_dataTrans == null)
            return super.hasDataImpl(aMimeType);

        // Handle FILE_LIST: Return true if at least one file
        if (aMimeType == FILE_LIST)
            return _dataTrans.getFileCount() > 0;

        // Forward to DataTrans
        return _dataTrans.hasType(aMimeType);
    }

    /**
     * Returns the clipboard content.
     */
    protected ClipboardData getDataImpl(String aMimeType)
    {
        // If no DataTransfer, just return normal version
        if (_dataTrans == null)
            return super.getDataImpl(aMimeType);

        // Handle Files
        if (aMimeType == FILE_LIST) {

            // Get files
            File[] jsfiles = _dataTrans.getFiles(); if (jsfiles == null) return null;
            List<ClipboardData> cfiles = new ArrayList<>(jsfiles.length);
            for (File jsfile : jsfiles) {
                ClipboardData cbfile = new TVClipboardData(jsfile);
                cfiles.add(cbfile);
            }

            // Return ClipboardData for files array
            return new ClipboardData(aMimeType, cfiles);
        }

        // Handle anything else (String data)
        Object data = _dataTrans.getData(aMimeType);
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
        if (_dataTrans != null) {

            // Handle string data
            if (aData.isString())
                _dataTrans.setData(aMimeType, aData.getString());

                // Otherwise complain
            else System.err.println("TVClipboard.addDataImpl: Unsupported data type: " + aMimeType + ", " + aData.getSource());
        }

        // Handle system clipboard copy: Wait till all types added, then update clipboard
        else {
            if (_addAllDatasRun == null)
                ViewUtils.runLater(_addAllDatasRun = ADD_ALL_DATAS_RUN);
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
            if (clipboardItem != null)
                clipItemsList.add(clipboardItem);
        }

        // Convert to JSArray of JSClipboardItem
        JSClipboardItem[] clipItems = clipItemsList.toArray(new JSClipboardItem[0]);
        JSArray<JSClipboardItem> clipItemsJS = JSArray.of(clipItems);

        // Write to system clipboard
        JSPromise writePromise = getClipboardWriteItemsPromise(clipItemsJS);
        if (writePromise != null)
            writePromise.catch_(aJSObj -> {
                System.err.println("TVClipboard.addAllDataToClipboard failed:");
                TV.log(aJSObj);
                return null;
            });

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
            byte[] bytes = img.getBytesPNG();
            Blob blob = TV.createBlob(bytes, "image/png");

            // Get ClipboardItem array for blob
            return getJSClipboardItem(blob);
        }

        // Handle anything else
        else {

            // Get type and bytes
            String type = aData.getMIMEType();
            byte[] bytes = aData.getBytes();

            // If valid, just wrap in JSClipboardItem
            if (type != null && bytes != null && bytes.length > 0) {
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
    public void startDrag()  { System.err.println("TVClipboard.startDrag: Not implemented"); }

    /** Called to indicate that drop is accepted. */
    public void acceptDrag()  { System.err.println("TVClipboard.startDrag: Not implemented"); }

    /** Called to indicate that drop is complete. */
    public void dropComplete()  { System.err.println("TVClipboard.startDrag: Not implemented");  }

    /**
     * Override to clear DataTrans.
     */
    @Override
    public void clearData()
    {
        super.clearData();
        _dataTrans = null;
    }

    /**
     * Returns the shared TVClipboard.
     */
    public static TVClipboard get()
    {
        if (_shared != null) return _shared;
        return _shared = new TVClipboard();
    }

    /**
     * Returns whether clipboard is loaded.
     */
    public boolean isLoaded()  { return _loaded; }

    /**
     * Adds a callback to be triggered when resources loaded.
     * If Clipboard needs to be 'approved', get approved and call given load listener.
     */
    public void addLoadListener(Runnable aRun)
    {
        // Set LoadListener
        _loadListener = aRun;

        // Get PermissionsPromise
        JSPromise<JSObject> rval = getReadPermissionsPromise();
        if (rval != null) {
            rval.then(perm -> didGetPermissions(perm));
            rval.catch_(anObjJS -> {
                System.err.println("TVClipboard.addLoadListener: failed:");
                TV.log(anObjJS);
                return null;
            });
        }

        // Otherwise, return
        else {
            System.out.println("TVClipboard.addLoadListener: No read permissions promise?");
            didGetPermissions(null);
        }
    }

    /**
     * Notify loaded.
     */
    private void notifyLoaded()
    {
        ViewUtils.runLater(() -> {
            _loaded = true;
            _loadListener.run();
            _loaded = false;
            _loadListener = null;
        });
    }

    /**
     * Returns a readText promise
     */
    private static JSPromise didGetPermissions(JSObject aPermResult)
    {
        // Print result of permissions
        if (aPermResult != null) {
            String state = getPermissionStatusState(aPermResult);
            System.out.println("TVClipboard.didGetPermissions: Got Read Permissions: " + state);
        }

        // Get/configure readText promise to call didGetClipboardReadText
        JSPromise<JSString> rval = getClipboardReadTextPromise();
        rval.then(str -> didGetClipboardReadText(str));
        rval.catch_(aJSO -> {
            System.err.println("TVClipboard.didGetPermissions: failed:");
            TV.log(aJSO);
            return null;
        });

        // Return promise
        return rval;
    }

    /**
     * Returns the system DataTransfer.
     */
    private static JSPromise didGetClipboardReadText(JSString aStr)
    {
        // Get string. Null check? This probably can't happen
        String str = aStr != null ? aStr.stringValue() : null;
        if (str == null)  {
            System.err.println("TVClipboard.didGetClipboardReadText: null string");
            return null;
        }

        // Log string
        String msg = str.replace("\n", "\\n");
        if (str.length() > 50) msg = str.substring(0, 50) + "...";
        System.out.println("TVClipboard.didGetClipboardReadText: Read clipboard string: " + msg);

        // Create/set DataTransfer for string
        _shared._dataTrans = DataTransfer.getDataTrasferForString(str);

        // Trigger LoadListener
        _shared.notifyLoaded();
        return null;
    }

    /**
     * Returns promise for DataTransferItemArray to DataTransfer.
     */
    /*private static JSPromise didGetClipboardReadDataTransfer(JSArray<JSDataTransferItem> theDTIs)
    {
        System.out.println("Got DataTransfers: " + (theDTIs!=null ? theDTIs.getLength() : "Failed!"));TV.log(theDTIs);
        _shared._dataTrans = DataTransfer.getDataTrasferForDataTransferItemArray(theDTIs);
        _shared.notifyLoaded();
        return null;
    }*/

    /**
     * Returns a Promise for read permissions.
     * Chrome supports. Safari just returns null.
     */
    @JSBody(params={ }, script = "return navigator.permissions ? navigator.permissions.query({name: 'clipboard-read'}) : null;")
    public static native JSPromise<JSObject> getReadPermissionsPromise();

    /**
     * Returns clipboard.readText() promise.
     */
    @JSBody(params={ }, script = "return navigator.clipboard.readText();")
    public static native JSPromise<JSString> getClipboardReadTextPromise();

    /**
     * Returns navigator.clipboard.read() promise.
     */
    //@JSBody(params={ }, script = "return navigator.clipboard.read();")
    //public static native JSPromise<JSArray<JSDataTransferItem>> getClipboardReadPromise();

    /**
     * Returns clipboard.write(items) promise.
     */
    @JSBody(params={ "theItems" }, script = "return navigator.clipboard.write(theItems);")
    public static native JSPromise<JSString> getClipboardWriteItemsPromise(JSArray<JSClipboardItem> theItems);

    /**
     * Returns ClipboardItem for given blob.
     */
    @JSBody(params={ "blob" }, script = "var param = {}; param[blob.type] = blob; return new ClipboardItem(param);")
    public static native JSClipboardItem getJSClipboardItem(JSObject blob);

    /**
     * Returns navigator.permissions.state for given permissions.
     */
    @JSBody(params={ "aPermResult" }, script = "console.log(aPermResult); return aPermResult.state;")
    static native String getPermissionStatusState(JSObject aPermResult);
}