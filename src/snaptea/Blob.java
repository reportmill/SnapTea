package snaptea;
//import java.io.*;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 * Blob is a file-like object of immutable, raw data. Blobs represent data that isn't necessarily in a
 * JavaScript-native format. The File interface is based on Blob, inheriting blob functionality and expanding it to
 * support files on the user's system.
 */
public interface Blob extends JSObject {
    
    // The bytes
    //byte      _bytes[];

/**
 * Creates a new Blob.
 */
//protected Blob()  { }

/**
 * Creates a new Blob from given object.
 */
/*public Blob(Object anObj, String aType)  { byte bytes[] = getBytes(anObj); _jso = createBlobJSO(bytes, aType); }*/

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
    // If already set, just return
    //if(_bytes!=null) return _bytes;
    
    // Return bytes
    byte bytes[] = FileReader.getBytes(this);
    return bytes;
}

/**
 * Returns bytes for an object (byte array or InputStream).
 */
/*static byte[] getBytes(Object anObj)
{
    if(anObj instanceof byte[])
        return (byte[])anObj;
    if(anObj instanceof InputStream)
        return getBytes((InputStream)anObj);
    throw new RuntimeException("Blob: Can't init from object: " + anObj);
}*/

/**
 * Returns bytes for an input stream.
 */
/*static byte[] getBytes(InputStream aStream)
{
    try { return getBytes2(aStream); }
    catch(IOException e) { throw new RuntimeException(e); }
}*/

/**
 * Returns bytes for an input stream.
 */
/*static byte[] getBytes2(InputStream aStream) throws IOException
{
    ByteArrayOutputStream bs = new ByteArrayOutputStream();
    byte chunk[] = new byte[8192];
    for(int len=aStream.read(chunk, 0, chunk.length); len>0; len=aStream.read(chunk, 0, chunk.length))
        bs.write(chunk, 0, len);
    return bs.toByteArray();
}*/

/**
 * Returns a URL string to this blob.
 */
//public String getURL()  { return URL.createObjectURL(this); }

}