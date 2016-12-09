package snaptea;
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
public FileHeader getFileHeader(String aPath)
{
    String urls = getURLString() + aPath;
    if(urls.startsWith("http://abc")) urls = aPath.substring(1) + "?v=" + System.currentTimeMillis();
    System.out.println("Head: " + urls);
    
    FileHeader finfo = new FileHeader(aPath, false); //isDir
    return finfo;
}

/**
 * Returns file content (bytes for file, FileHeaders for dir).
 */
protected Object getFileContent(String aPath) throws Exception
{
    String urls = getURLString() + aPath;
    if(urls.startsWith("http://abc")) urls = aPath.substring(1) + "?v=" + System.currentTimeMillis();
        
    XMLHttpRequest req = XMLHttpRequest.create();
    req.open("GET", urls, false);
    System.out.println("Get: " + urls);
    sendSync(req, null); //req.send(); - if not open Async
    System.out.println("GetDone: " + urls);
    
    // Get bytes
    String text = req.getResponseText();
    byte bytes[] = text.getBytes(); if(bytes==null) System.out.println("No file bytes: " + aPath);
    return bytes;
}

/**
 * Handle a get request.
 */
protected WebResponse doPost(WebRequest aReq)
{
    WebURL url = aReq.getURL();
    String urls = url.getString(); if(urls.startsWith("http://abc")) urls = url.getPath().substring(1);
    
    XMLHttpRequest req = XMLHttpRequest.create();
    req.open("POST", urls, false);
    
    String str = new String(aReq.getPostBytes());
    System.out.println("Post: " + urls);
    sendSync(req, str); //req.send(str); - if not open Async
    System.out.println("PostDone: " + urls);
    
    // Get bytes
    String text = req.getResponseText();
    WebResponse resp = new WebResponse(); resp.setRequest(aReq);
    resp.setCode(WebResponse.OK);
    resp.setBytes(text.getBytes());
    return resp;
}

/**
 * Sends an XMLHttpRequest synchronously.
 */
protected void sendSync(XMLHttpRequest aReq, String aStr)
{
    TVLock lock = new TVLock();
    aReq.onComplete(() -> lock.unlock());
    if(aStr==null) aReq.send(); else aReq.send(aStr);
    lock.lock();
}

/**
 * Standard toString implementation.
 */
public String toString()  { return "TVWebSite " + getURLString(); }

}