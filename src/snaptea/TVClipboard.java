package snaptea;
//import cjdom.Element;
import java.util.*;
import snap.gfx.Image;
import snap.view.*;
//import cjdom.DragEvent;
//import cjdom.DataTransfer;

/**
 * A snap Clipboard implementation for TeaVM.
 */
public class TVClipboard extends Clipboard {
    
    // The view to initiate drag
    View             _view;
    
    // The view event
    ViewEvent        _viewEvent;
    
    // The DataTransfer
    DataTransfer     _dataTrans;
    
    // The shared clipboards for system and drag
    static TVClipboard  _shared;
    static TVClipboard  _sharedDrag;

/**
 * Returns the clipboard content.
 */
protected boolean hasDataImpl(String aMimeType)
{
    // If no DataTransfer, just return normal version
    if(_dataTrans==null) return super.hasDataImpl(aMimeType);
    
    if(aMimeType==FILE_LIST)
        return _dataTrans.getFiles().getLength()>0;
    return _dataTrans.hasType(aMimeType);
}

/**
 * Returns the clipboard content.
 */
protected ClipboardData getDataImpl(String aMimeType)
{
    // If no DataTransfer, just return normal version
    if(_dataTrans==null) return super.getDataImpl(aMimeType);
    
    Object data = null;
    
    // Handle Files
    if(aMimeType==FILE_LIST) {
        File jsfiles[] = _dataTrans.getFilesArray(); if(jsfiles==null) return null;
        List <ClipboardData> cfiles = new ArrayList(jsfiles.length);
        for(File jsfile : jsfiles) {
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
/*protected void addDataImpl(String aMimeType, ClipboardData aData)
{
    // Do normal implementation to populate ClipboardDatas map
    super.addDataImpl(aMimeType, aData);
    
    // If no DataTransfer, just return
    if(_dataTrans==null) return;
    
    // Handle string data
    if(aData.isString())
        _dataTrans.setData(aMimeType, aData.getString());
        
    // Otherwise complain
    else System.err.println("CJClipboard.addDataImpl: Unsupported data type: " + aMimeType + ", " + aData.getSource());
}*/

/**
 * Starts the drag.
 */
public void startDrag()
{
    // Set Dragging true and consume event
    //isDragging = true;
    _viewEvent.consume();
    
    // Get drag image and point and set in DataTransfer
    Image dimg = getDragImage(); if(dimg==null) dimg = Image.get(1,1,true);
    //Element img = (Element)dimg.getNative();
    double dx = getDragImageOffset().x;
    double dy = getDragImageOffset().y;
    //_dataTrans.setDragImage(img, dx, dy);
        
    // Add image element to canvas so browsers can generate image (then remove a short time later)
    //cjdom.Element body = cjdom.Document.current().getBody();
    //body.appendChild(img);
    //TVViewEnv.get().runDelayed(() -> { isDragging = false; body.removeChild(img); }, 100, false);
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
    _dataTrans = dragEvent.getDataTransfer();
}

/**
 * Returns the shared TVClipboard.
 */
public static TVClipboard get()
{
    if(_shared!=null) return _shared;
    return _shared = new TVClipboard();
}

/**
 * Returns the shared TVClipboard for drag and drop.
 */
public static TVClipboard getDrag(ViewEvent anEvent)
{
    if(_sharedDrag==null) _sharedDrag = new TVClipboard();
    if(anEvent!=null) _sharedDrag.setEvent(anEvent);
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