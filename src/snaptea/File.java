package snaptea;
import org.teavm.jso.JSProperty;

/**
 * File provides information about files and allows JavaScript in a web page to access their content.
 */
public interface File extends Blob {

    /**
     * Returns the name of the file referenced by the File object.
     */
    @JSProperty
    public String getName();

    /**
     * Returns the last modified time of the file, in millisecond since the UNIX epoch (January 1st, 1970 at Midnight).
     */
    @JSProperty
    public long getLastModified();
}