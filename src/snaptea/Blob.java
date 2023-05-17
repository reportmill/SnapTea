package snaptea;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 * Blob is a file-like object of immutable, raw data. Blobs represent data that isn't necessarily in a
 * JavaScript-native format. The File interface is based on Blob, inheriting blob functionality and expanding it to
 * support files on the user's system.
 */
public interface Blob extends JSObject {

    /**
     * Returns the size, in bytes, of the data contained in the Blob object.
     */
    @JSProperty
    public int getSize();

    /**
     * Returns string indicating MIME type of data contained in Blob. If type is unknown, this string is empty.
     */
    @JSProperty
    public String getType();

    /**
     * Returns the bytes for the Blob.
     */
    default byte[] getBytes()
    {
        return FileReader.getBytes(this);
    }
}