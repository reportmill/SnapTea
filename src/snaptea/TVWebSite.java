package snaptea;
import java.net.URL;
import java.util.*;
import org.teavm.jso.ajax.XMLHttpRequest;
import snap.util.SnapUtils;
import snap.web.*;

/**
 * A SnapSite subclass for the TeaVM root that uses an index.txt file to determine if app files exist.
 */
public class TVWebSite extends WebSite {
    
    // Return the paths that are available from this site
    List <String>  _paths;
    
    // The site root url string
    String         ROOT_URL;
    
    // Whether root has path
    boolean        _rootHasPath;
    
    // Whether to debug
    boolean        _debug = false;
    
    // The shared site
    static TVWebSite  _shared;
    
/**
 * Creates a TVWebSite.
 */
protected TVWebSite()
{
    ROOT_URL = TVViewEnv.getScriptRoot(); // Was "http://localhost"
    WebURL url = WebURL.getURL(ROOT_URL);
    _rootHasPath = url.getPath()!=null;
    setURL(url);
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
        
        // Get Java URL
        String urls = url.getString().replace("!", "");
        java.net.URL urlx;
        try { urlx = new java.net.URL(urls); }
        catch(Exception e) { throw new RuntimeException(e); }
        
        // Get bytes
        byte bytes[] = getBytes(urlx); //getFileBytes(path);
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
/*protected byte[] getFileBytes(String aPath) {
    // Get XMLHttpRequest
    String urls = ROOT_URL + aPath;  // Was urls = aPath.substring(1);
    XMLHttpRequest req = XMLHttpRequest.create();
    req.open("GET", urls, false); if(_debug) System.out.println("Get: " + urls);
    req.send(); if(_debug) System.out.println("GetDone: " + urls);
    
    // Get bytes
    String text = req.getResponseText();
    byte bytes[] = text.getBytes(); if(bytes==null) System.out.println("No file bytes: " + aPath);
    return bytes; }*/

/**
 * Returns bytes for file path.
 */
/*protected byte[] getFileBytes2(String aPath) {
    // Get XMLHttpRequest
    String urls = ROOT_URL + aPath;  // Was urls = aPath.substring(1);
    XMLHttpRequest req = XMLHttpRequest.create();
    req.open("GET", urls, false); if(_debug) System.out.println("Get: " + urls);
    req.send(); if(_debug) System.out.println("GetDone: " + urls);
    
    // Get bytes
    req.setResponseType("arraybuffer");
    ArrayBuffer arrayBuf = (ArrayBuffer)req.getResponse();
    Int8Array array = Int8Array.create(arrayBuf);
    byte[] bytes = new byte[array.getLength()]; for(int i=0; i<bytes.length; ++i) bytes[i] = array.get(i);
    return bytes; }*/

/**
 * Returns bytes for url.
 */
private static byte[] getBytes(java.net.URL aURL)
{
    try { return getBytesOrThrow(aURL); }
    catch(Exception e) { throw new RuntimeException(e); }
}

/**
 * Returns bytes for url.
 */
private static byte[] getBytesOrThrow(java.net.URL aURL) throws java.io.IOException
{
    // Get connection, stream, stream bytes, then close stream and return bytes
    java.net.URLConnection conn = aURL.openConnection();
    java.io.InputStream stream = conn.getInputStream();  // Get stream for URL
    byte bytes[] = SnapUtils.getBytesOrThrow(stream);  // Get bytes for stream, close and return bytes
    stream.close();
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
    String urls = url.getString(); //if(urls.startsWith("http://localhost")) urls = url.getPath().substring(1);
    
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
    // If already set, just return
    if(_paths!=null) return _paths;

    // Get index.txt file
    String urls = ROOT_URL + "/index.txt";
    XMLHttpRequest req = XMLHttpRequest.create();
    req.open("GET", urls, false);
    req.send();

    // Split
    String text = req.getResponseText();
    String pathStrings[] = text.split("\\s*\n\\s*");
    return _paths = new ArrayList(Arrays.asList(pathStrings));
}

/**
 * Returns whether a given path exists.
 */
public boolean isPath(String aPath)
{
    // Get paths
    List <String> paths = getPaths();

    // Return true if path in list, or prefix in list
    if (paths.contains(aPath))
        return true;
    else if (isDirPath(aPath))
        return true;
    return false;
}

/**
 * Returns whether a given path exists.
 */
public boolean isDirPath(String aPath)
{
    // Get path (strip trailing separator)
    String path = aPath;
    if(!aPath.endsWith("/")) path += '/';

    // Iterate over paths and return true if prefix found
    for(String p : getPaths())
        if(p.startsWith(path))
            return true;
    return false;
}

/**
 * Returns whether a given path exists.
 */
public List <String> getDirPaths(String aPath)
{
    // Get path (strip trailing separator)
    String path = aPath; if(!path.endsWith("/")) path += '/';

    // Iterate over paths and add to list if has prefix
    List <String> paths = new ArrayList();
    for(String p : getPaths()) {
        if (!p.startsWith(path)) continue;
        int ind = p.indexOf('/', path.length());
        if (ind > 0)
            p = p.substring(0, ind);
        if (!paths.contains(p))
            paths.add(p);
    }

    // Return paths
    return paths;
}

/**
 * Return URL for class and path.
 */
public URL getJavaURL(Class aClass, String aPath)
{
    // If not known path, return null
    if(!isPath(aPath))
        return null;

    String urls = _rootHasPath? (ROOT_URL + '!' + aPath) : (ROOT_URL + aPath);
    try {
        URL url = new java.net.URL(urls); // was "http://localhost"
        System.out.println("TVWebSite.getURL: Returning url: " + url);
        return url;
    }
    catch(java.net.MalformedURLException e) { throw new RuntimeException(e); }
}

/**
 * Standard toString implementation.
 */
public String toString()
{
    return "TVWebSite " + getURLString();
}

/**
 * Adds a known path.
 */
public static void addKnownPath(String aPath)
{
    _shared.getPaths().add(aPath);
}

/**
 * Returns a shared instance.
 */
public static TVWebSite get()
{
    if(_shared!=null) return _shared;
    return _shared = new TVWebSite();
}

}