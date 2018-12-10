package snaptea;
import java.util.*;
import org.teavm.jso.ajax.XMLHttpRequest;
import snap.web.*;

/**
 * A custom class.
 */
public class TVWebSite extends WebSite {
    
    // Return the paths that are available from this site
    List <String>  _paths;
    
    // Whether to debug
    boolean        _debug = false;
    
    // The shared site
    static TVWebSite  _shared;
    
/**
 * Creates a TVWebSite.
 */
protected TVWebSite()
{
    setURL(WebURL.getURL("http://localhost"));
}

/**
 * Handle a get or head request.
 */
protected void doGetOrHead(WebRequest aReq, WebResponse aResp, boolean isHead)
{
    // Get URL, path and file
    WebURL url = aReq.getURL();
    String path = url.getPath(); if(path==null) path = "/";
    
    // Get FileHeader
    FileHeader fhdr = getFileHeader(path);
    
    // Handle NOT_FOUND
    if(fhdr==null) {
        aResp.setCode(WebResponse.NOT_FOUND); return; }
        
    // Configure response info (just return if isHead). Need to pre-create FileHeader to fix capitalization.
    aResp.setCode(WebResponse.OK);
    aResp.setFileHeader(fhdr);
    if(isHead)
        return;
        
    // If file, just set bytes
    if(aResp.isFile()) {
        byte bytes[] = getFileBytes(path);
        aResp.setBytes(bytes);
    }
    
    // If directory, configure directory info and return
    else {
        List <FileHeader> fhdrs = getFileHeaders(path);
        aResp.setFileHeaders(fhdrs);
    }
}

/**
 * Returns a data source file for given path (if file exists).
 */
public FileHeader getFileHeader(String aPath)
{
    String urls = getURLString() + aPath;
    if(_debug) System.out.println("Head: " + urls);
    
    if(!isPath(aPath)) {
        if(_debug) System.out.println("TVWebSite.getFileHeader: File Not found: " + aPath);
        return null;
    }
    
    boolean isDir = isDirPath(aPath);
    FileHeader finfo = new FileHeader(aPath, isDir); //isDir
    return finfo;
}

/**
 * Returns bytes for file path.
 */
protected byte[] getFileBytes(String aPath)
{
    String urls = aPath.substring(1);
    
    // Get XMLHttpRequest
    XMLHttpRequest req = XMLHttpRequest.create();
    req.open("GET", urls, false);
    if(_debug) System.out.println("Get: " + urls);
    req.send();
    if(_debug) System.out.println("GetDone: " + urls);
    
    // Get bytes
    String text = req.getResponseText();
    byte bytes[] = text.getBytes(); if(bytes==null) System.out.println("No file bytes: " + aPath);
    return bytes;
}

/**
 * Returns FileHeaders for dir path.
 */
protected List <FileHeader> getFileHeaders(String aPath)
{
    String urls = aPath.substring(1);
    List <String> paths = getDirPaths(aPath);
    List <FileHeader> fhdrs = new ArrayList();
    
    // Iterate over paths
    for(String path : paths) {
        boolean isDir = isDirPath(path);
        FileHeader fhdr = new FileHeader(path, isDir);
        fhdrs.add(fhdr);
    }
    
    // Return FileHeaders
    return fhdrs;
}
        
/**
 * Handle a get request.
 */
protected void doPost(WebRequest aReq, WebResponse aResp)
{
    WebURL url = aReq.getURL();
    String urls = url.getString(); if(urls.startsWith("http://localhost")) urls = url.getPath().substring(1);
    
    XMLHttpRequest req = XMLHttpRequest.create();
    req.open("POST", urls, false);
    
    String str = new String(aReq.getSendBytes());
    if(_debug) System.out.println("Post: " + urls);
    req.send(str); //req.send(str); - if not open Async
    if(_debug) System.out.println("PostDone: " + urls);
    
    // Get bytes
    String text = req.getResponseText();
    aResp.setCode(WebResponse.OK);
    aResp.setBytes(text.getBytes());
}

/**
 * Returns the paths of files available at this site.
 */
public List <String> getPaths()
{
    if(_paths!=null) return _paths;
    
    String urls = "index.txt";
    XMLHttpRequest req = XMLHttpRequest.create();
    req.open("GET", urls, false);
    req.send();
    
    String text = req.getResponseText();
    String pathStrings[] = text.split("\n");
    return _paths = new ArrayList(Arrays.asList(pathStrings));
}

/**
 * Returns whether a given path exists.
 */
public boolean isPath(String aPath)
{
    List <String> paths = getPaths(); if(paths==Collections.EMPTY_LIST) return true;
    return paths.contains(aPath) || isDirPath(aPath);
}

/**
 * Returns whether a given path exists.
 */
public boolean isDirPath(String aPath)
{
    String path = aPath; if(!aPath.endsWith("/")) path += '/';
    for(String p : getPaths()) if(p.startsWith(path)) return true;
    return false;
}

/**
 * Returns whether a given path exists.
 */
public List <String> getDirPaths(String aPath)
{
    List <String> paths = new ArrayList();
    String path = aPath; if(!path.endsWith("/")) path += '/';
    for(String p : getPaths()) if(p.startsWith(path)) {
        int ind = p.indexOf('/', path.length());
        if(ind>0) p = p.substring(0, ind);
        if(!paths.contains(p)) paths.add(p);
    }
    return paths;
}

/**
 * Return URL for class and path.
 */
java.net.URL getURL(Class c, String p)
{
    if(!isPath(p)) return null;
    try { return new java.net.URL("http://localhost" + p); }
    catch(java.net.MalformedURLException e) { throw new RuntimeException(e); }
}

/**
 * Standard toString implementation.
 */
public String toString()  { return "TVWebSite " + getURLString(); }

/**
 * Adds a known path.
 */
public static void addKnownPath(String aPath)  { _shared.getPaths().add(aPath); }

/**
 * Returns a shared instance.
 */
public static TVWebSite get()
{
    if(_shared!=null) return _shared;
    return _shared = new TVWebSite();
}

}