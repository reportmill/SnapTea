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
    JSArray <JSString> getTypes();

    /**
     * Returns the data for a given type.
     */
    String getData(String aType);

    /**
     * Set the data for a given type. If data for the type does not exist, it is added at the end, such that the last item
     * in the types list will be the new format. If data for the type already exists, the existing data is replaced in the
     * same position.
     */
    void setData(String aType, String theData);

    /**
     * Returns an array of all the local files available on the data transfer. If the drag operation doesn't involve
     * dragging files, this property is an empty list.
     */
    @JSProperty
    JSArrayReader<File> getFiles();

    /**
     * Sets the image Element element to use for the drag feedback image.
     */
    void setDragImage(HTMLElement aImg, double xOffset, double yOffset);
}