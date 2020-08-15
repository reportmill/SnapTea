package snaptea;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.core.JSArray;
import org.teavm.jso.core.JSArrayReader;
import org.teavm.jso.core.JSString;
import org.teavm.jso.dom.html.HTMLElement;

/**
 * DataTransfer is used to hold the data that is being dragged during a drag and drop operation. It may hold one or more
 * data items, each of one or more data types. For more information about drag and drop, see HTML Drag and Drop API.
 */
public interface JSDataTransfer extends JSObject {

    @JSProperty
    public JSArray <JSString> getTypes();

    /**
     * Returns the data for a given type.
     */
    public String getData(String aType);

    /**
     * Set the data for a given type. If data for the type does not exist, it is added at the end, such that the last item
     * in the types list will be the new format. If data for the type already exists, the existing data is replaced in the
     * same position.
     */
    public void setData(String aType, String theData);

    /**
     * Returns an array of all the local files available on the data transfer. If the drag operation doesn't involve
     * dragging files, this property is an empty list.
     */
    @JSProperty
    public JSArrayReader<File> getFiles();

    /**
     * Sets the image Element element to use for the drag feedback image.
     */
    public void setDragImage(HTMLElement aImg, double xOffset, double yOffset);





    // The types
    //String            _types[];
    // The items
    //DataTransferItem  _items[];
    // The Files
    //File              _files[];

    /**
     * Returns whether DataTransfer has given type.
     */
    //public boolean hasType(String aType)
    //{
    //    for (String type : getTypes())
    //        if (type.equals(aType))
    //            return true;
    //    return false;
    //}

    /**
     * Returns an array of strings giving the formats that were set in the dragstart event.
     */
    //public String[] getTypes()
    //{
    //    if (_types!=null) return _types;
    //    int count = getTypeCount();
    //    _types = new String[count];
    //    for (int i=0;i<count;i++) _types[i] = getString(getType(i));
    //    return _types;
    //}

    /** Returns the number of types. */
    //native int getTypeCount();

    /** Returns the number of types. */
    //native Object getType(int anIndex);

    /** Returns data for given type, or empty string if data for type does not exist or data transfer contains no data. */
    /*public String getData(String aType)
    {
        Object dstr = getDataJSO(aType);
        return getString(dstr);
    }*/

    /**
     * Set the data for a given type. If data for the type does not exist, it is added at the end, such that the last item
     * in the types list will be the new format. If data for the type already exists, the existing data is replaced in the
     * same position.
     */
    /*public void setData(String aType, String theData);
    {
        setDataJSO(aType, theData);
        _types = null; _items = null; _files = null;
    }*/

    /** Set the data for a given type. */
    //native void setDataJSO(String aType, String theData);

    /** Removes drag op's drag data for given type. If data for given type does not exist, this method does nothing. */
    //public native void clearData(String aType);

    /**
     * Returns an array of all the local files available on the data transfer. If the drag operation doesn't involve
     * dragging files, this property is an empty list.
     */
    /*public File[] getFiles()
    {
        if (_files!=null) return _files;
        int count = getFileCount(); _files = new File[count];
        for (int i=0;i<count;i++) { _files[i] = new File(); _files[i]._jso = getFileJSO(i); }
        return _files;
    }*/

    /** Returns an the number of all the local files available on the data transfer. */
    //native int getFileCount();

    /** Returns an the local files available on the data transfer at given index. */
    //native Object getFileJSO(int anIndex);

    /** Sets the files. */
    /*public void setFiles(File ... theFiles)  { _files = Arrays.copyOf(theFiles, theFiles.length); }*/

    /**
     * Sets the image Element element to use for the drag feedback image.
     */
    //public native void setDragImage(Element aImg, double xOffset, double yOffset);

    /**
     * Returns an array of DataTransferItem objects representing drag data.
     */
    /*public DataTransferItem[] getItems()
    {
        if (_items!=null) return _items;
        int count = getItemCount(); _items = new DataTransferItem[count];
        for (int i=0;i<count;i++) { _items[i] = new DataTransferItem(); _items[i]._jso = getItemJSO(i); }
        return _items;
    }*/

    /** Returns the number of data transfer items. */
    //native int getItemCount();

    /** Returns the DataTransferItem JSO at index. */
    //native Object getItemJSO(int anIndex);

    /**
     * Standard toString implementation.
     */
    /*public String toString()
    {
        StringBuilder sb = new StringBuilder("DataTransfer { Types:[");
        String types[] = getTypes();
        for (String type : types) { if (type!=types[0]) sb.append(", "); sb.append(type); }
        sb.append(" ] }");
        return sb.toString();
    }*/
}