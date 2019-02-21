package snaptea;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSProperty;
import org.teavm.jso.dom.events.EventTarget;
import org.teavm.jso.typedarrays.ArrayBuffer;
import org.teavm.jso.typedarrays.Int8Array;
import snap.view.ViewUtils;

/**
 * FileReader lets web applications asynchronously read the contents of files (or raw data buffers) stored on
 * the user's computer, using File or Blob objects to specify the file or data to read.
 */
public class FileReader {

    JSFileReader _js;
    
/**
 * Creates a new FileReader.
 */
public FileReader()
{
    _js = createFileReaderJS();
}

/**
 * Creates a FileReader JSO.
 */
@JSBody(params={ }, script = "return new FileReader();")
static native JSFileReader createFileReaderJS();

/**
 * Returns the bytes.
 */
public byte[] getResultBytes()
{
    ArrayBuffer arrayBuf = _js.getResult();
    Int8Array array = Int8Array.create(arrayBuf);
    byte[] bytes = new byte[array.getLength()]; for(int i=0; i<bytes.length; ++i) bytes[i] = array.get(i);
    return bytes;
}

/**
 * readBytesAndWait
 */
synchronized void readBytesAndRunLater(Blob aBlob, Runnable aRun)
{
    _js.addEventListener("loadend", e -> ViewUtils.runLater(aRun));
    _js.readAsArrayBuffer(aBlob);
}

/**
 * readBytesAndWait
 */
synchronized void readBytesAndWait(Blob aBlob)
{
    _js.addEventListener("loadend", e -> readBytesNotify());
    _js.readAsArrayBuffer(aBlob);
    
    // Wait until done
    try { wait(); }
    catch(Exception e) { throw new RuntimeException(e); }
}

/**
 * readBytesNotify
 */
synchronized void readBytesNotify()  { notify(); }

/**
 * Returns the bytes for the Blob.
 */
public static byte[] getBytes(Blob aBlob)
{
    // Create FileReader and readBytes
    FileReader frdr = new FileReader();
    frdr.readBytesAndWait(aBlob);
    
    // Get result
    byte bytes[] = frdr.getResultBytes();
    return bytes;
}

/**
 * FileReader lets web applications asynchronously read the contents of files (or raw data buffers) stored on
 * the user's computer, using File or Blob objects to specify the file or data to read.
 */
interface JSFileReader extends EventTarget {

    @JSProperty
    ArrayBuffer getResult();
    
    void readAsArrayBuffer(Blob aBlob);
}

}