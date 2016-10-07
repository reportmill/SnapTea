package snaptea;
import java.util.*;
import org.teavm.jso.ajax.XMLHttpRequest;
import snap.web.*;

/**
 * A custom class.
 */
public class TVWebSite extends WebSite {
    
    // The file being fetched
    FileHeader     _finfo;

/**
 * Returns the string identifying the prefix for URLs in this data source.
 */
public String getURLScheme()  { return "http"; }

/**
 * Override to make this available to package.
 */
public void setURL(WebURL aURL)  { super.setURL(aURL); }

/**
 * Returns a data source file for given path (if file exists).
 */
protected FileHeader getFileHeader(String aPath) throws Exception
{
    // Fetch URL
    //String urls = getURLString() + aPath;
    //XMLHttpRequest req = XMLHttpRequest.create();
    //req.onComplete(() -> handleHeadResponse(req, aPath));
    //req.open("HEAD", aPath, false);
    //req.send();
    // Handle non-success response codes
    //int code = aReq.getStatus();
    //if(code==HTTPResponse.NOT_FOUND) throw new FileNotFoundException(aPath);
    //if(code==HTTPResponse.UNAUTHORIZED) throw new AccessException();
    //if(code!=HTTPResponse.OK) throw new IOException(resp.getMessage());
    
    // Create file, set bytes and return
    //boolean isDir = StringUtils.getPathExtension(aPath).length()==0;
    FileHeader finfo = new FileHeader(aPath, false); //isDir
    //finfo.setLastModifiedTime(resp.getLastModified());
    //finfo.setSize(resp.getContentLength());
    return finfo;
}

/**
 * Gets file bytes.
 */
public synchronized byte[] getFileBytes(String aPath)
{
    String urls = getURLString() + aPath;
    XMLHttpRequest req = XMLHttpRequest.create();
    req.open("GET", aPath.substring(1));
    sendSync(req);
    
    // Get bytes
    String text = req.getResponseText();
    byte bytes[] = text.getBytes(); if(bytes==null) System.out.println("No file bytes: " + aPath);
    return bytes;
}

/**
 * Sends an XMLHttpRequest synchronously.
 */
public void sendSync(XMLHttpRequest aReq)
{
    Object lock = new Object();
    aReq.onComplete(() -> { synchronized(lock) { lock.notify(); }});
    aReq.send();
    try { synchronized(lock) { lock.wait(); } }
    catch(Exception e) { throw new RuntimeException(e); }
}

/**
 * Returns files at path.
 */
public List <FileHeader> getFileHeaders(WebFile aFile) { return Collections.EMPTY_LIST; }

public String toString()  { return "TVWebSite " + getURLString(); }

}