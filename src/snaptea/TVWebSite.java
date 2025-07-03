package snaptea;
import java.net.URL;
import java.util.*;

import org.teavm.jso.ajax.XMLHttpRequest;
import snap.util.ArrayUtils;
import snap.util.SnapUtils;
import snap.web.*;

/**
 * This class is a WebSite implementation for the HTTP root at website holding jar resource files.
 * All resource Files need to be listed in an index.txt file.
 */
public class TVWebSite extends WebSiteX {

    // Return the paths that are available from this site
    private String[] _paths;

    // The site root url string
    private String ROOT_URL;

    // Whether root has path
    private boolean _rootHasPath;

    // The shared site
    private static TVWebSite _shared;

    /**
     * Constructor.
     */
    protected TVWebSite()
    {
        ROOT_URL = TVViewEnv.getScriptRoot(); // Was "http://localhost"
        WebURL url = WebURL.getUrl(ROOT_URL);
        assert (url != null);
        _rootHasPath = !url.getPath().isEmpty();
        setURL(url);
    }

    /**
     * Returns the file header for given path.
     */
    @Override
    protected FileHeader getFileHeaderForPath(String filePath)
    {
        if (!isPath(filePath))
            return null;

        boolean isDir = isDirPath(filePath);
        return new FileHeader(filePath, isDir); //isDir
    }

    /**
     * Returns FileHeaders for dir file path.
     */
    @Override
    protected List<FileHeader> getFileHeadersForPath(String filePath)
    {
        List<String> paths = getDirPaths(filePath);
        List<FileHeader> fileHeaders = new ArrayList<>();

        // Iterate over paths
        for (String path : paths) {
            boolean isDir = isDirPath(path);
            FileHeader fileHeader = new FileHeader(path, isDir);
            fileHeaders.add(fileHeader);
        }

        // Return
        return fileHeaders;
    }

    /**
     * Returns bytes for Get call and given request/response.
     */
    @Override
    protected byte[] getFileBytesForGet(WebRequest aReq, WebResponse aResp)
    {
        WebURL url = aReq.getURL();
        URL javaUrl = getJavaUrlForUrl(url);
        return getBytesForJavaUrl(javaUrl);
    }

    /**
     * Handle a get request.
     */
    @Override
    protected void doPut(WebRequest aReq, WebResponse aResp)
    {
        WebURL url = aReq.getURL();
        String urls = url.getString(); //if(urls.startsWith("http://localhost")) urls = url.getPath().substring(1);

        XMLHttpRequest req = XMLHttpRequest.create();
        req.open("POST", urls, false);

        String str = new String(aReq.getSendBytes());
        req.send(str); //req.send(str); - if not open Async

        // Get bytes
        String text = req.getResponseText();
        aResp.setCode(WebResponse.OK);
        aResp.setBytes(text.getBytes());
    }

    /**
     * Returns the paths of files available at this site.
     */
    public String[] getPaths()
    {
        // If already set, just return
        if (_paths != null) return _paths;

        // Get index.txt file
        String urls = ROOT_URL + "/index.txt";
        XMLHttpRequest req = XMLHttpRequest.create();
        req.open("GET", urls, false);
        req.send();

        // Split
        String text = req.getResponseText();
        String[] pathStrings = text.split("\\s*\n\\s*");
        return _paths = pathStrings;
    }

    /**
     * Returns whether a given path exists.
     */
    private boolean isPath(String aPath)
    {
        // If path in known paths list, return true
        String[] paths = getPaths();
        if (ArrayUtils.contains(paths, aPath))
            return true;

        // If prefix in list, return true
        else if (isDirPath(aPath))
            return true;

        // Return not found
        return false;
    }

    /**
     * Returns whether a given path exists.
     */
    private boolean isDirPath(String aPath)
    {
        // Get path (strip trailing separator)
        String path = aPath;
        if (!aPath.endsWith("/")) path += '/';

        // Iterate over paths and return true if prefix found
        for (String p : getPaths())
            if (p.startsWith(path))
                return true;
        return false;
    }

    /**
     * Returns whether a given path exists.
     */
    public List<String> getDirPaths(String aPath)
    {
        // Get path (strip trailing separator)
        String path = aPath;
        if (!path.endsWith("/")) path += '/';

        // Iterate over paths and add to list if has prefix
        List<String> paths = new ArrayList<>();
        for (String p : getPaths()) {
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
    public URL getJavaUrlForPath(String aPath)
    {
        // If not known path, return null
        if (!isPath(aPath))
            return null;

        String urls = _rootHasPath ? (ROOT_URL + '!' + aPath) : (ROOT_URL + aPath);
        try { return new java.net.URL(urls); }
        catch (java.net.MalformedURLException e)  { throw new RuntimeException(e); }
    }

    /**
     * Returns a java URL for given WebURL.
     */
    private URL getJavaUrlForUrl(WebURL aURL)
    {
        String urlStr = aURL.getString().replace("!", "");
        try { return new java.net.URL(urlStr); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        return "TVWebSite " + getUrlAddress();
    }

    /**
     * Returns bytes for url.
     */
    private static byte[] getBytesForJavaUrl(java.net.URL aURL)
    {
        // Get connection, stream, stream bytes, then close stream and return bytes
        try {
            java.net.URLConnection conn = aURL.openConnection();
            java.io.InputStream stream = conn.getInputStream();  // Get stream for URL
            byte[] bytes = SnapUtils.getInputStreamBytes(stream);
            stream.close();
            return bytes;
        }

        // Rethrow exceptions
        catch (Exception e) { throw new RuntimeException(e); }
    }

    /**
     * Returns a shared instance.
     */
    public static TVWebSite get()
    {
        if (_shared != null) return _shared;
        return _shared = new TVWebSite();
    }
}