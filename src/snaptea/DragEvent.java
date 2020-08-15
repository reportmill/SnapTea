package snaptea;
import org.teavm.jso.JSProperty;
import org.teavm.jso.dom.events.MouseEvent;

/**
 * A DragEvent represents a drag and drop interaction. The user initiates a drag by placing a pointer device (such as
 * a mouse) on the touch surface and then dragging the pointer to a new location (such as another DOM element).
 * Applications are free to interpret a drag and drop interaction in an application-specific way.
 */
public interface DragEvent extends MouseEvent {

    /**
     * Returns the data that is transferred during a drag and drop interaction.
     */
    @JSProperty
    public JSDataTransfer getDataTransfer();





    // The DataTransfer object
    //DataTransfer          _dataTrans;

    /**
     * Returns the data that is transferred during a drag and drop interaction.
     */
    /*public JSDataTransfer getDataTransfer();
    {
        if(_dataTrans!=null) return _dataTrans;
        DataTransfer dt = new DataTransfer();
        dt._jso = getDataTransferJSO();
        return dt;
    }*/

    /**
     * Returns the data transfer JSO.
     */
    //native Object getDataTransferJSO();

    /**
     * Standard toString implementation.
     */
    /*default String toString()
    {
        StringBuilder sb = new StringBuilder("DragEvent { ").append(getDataTransfer()).append(" }");
        return sb.toString();
    }*/
}