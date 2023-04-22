package snaptea;
import org.teavm.jso.JSBody;
import snap.util.Convert;
import snap.util.Prefs;
import java.util.ArrayList;
import java.util.List;

/**
 * A Prefs implementation for TeaVM using LocalStorage.
 */
public class TVPrefs extends Prefs {

    // The name of this prefs node
    private static String  _name;

    /**
     * Constructor.
     */
    public TVPrefs(String aName)
    {
        _name = aName;
    }

    /**
     * Override to use LocalStorage.
     */
    @Override
    public Object getValue(String aKey, Object aDefault)
    {
        // Get key for name
        String key = aKey;
        if (_name != null)
            key = _name + '.' + key;

        // Get value from LocalStorage
        String val = getStringValueJS(key);
        return val!=null ? val : aDefault;
    }

    /**
     * Override to use LocalStorage.
     */
    @Override
    public void setValue(String aKey, Object aValue)
    {
        // Get key for name
        String key = aKey;
        if (_name != null)
            key = _name + '.' + key;

        // Get string value and set in LocalStorage
        String valueStr = Convert.stringValue(aValue);
        setStringValueJS(key, valueStr);
    }

    /**
     * Override to use LocalStorage.
     */
    @Override
    public String[] getKeys()
    {
        // Get keys until null
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {

            // Get key - just break if null
            String key = getKey(i);
            if (key == null)
                break;

            // Strip name
            if (_name != null && key.startsWith(_name))
                key = key.substring(_name.length());
            keys.add(key);
        }

        // Return array
        return keys.toArray(new String[0]);
    }

    /**
     * Clears all the preferences.
     */
    public void clear()
    {
        clearJS();
    }

    @JSBody(params={ "aKey" }, script = "return window.localStorage.getItem(aKey);")
    private static native String getStringValueJS(String aKey);

    @JSBody(params={ "aKey", "aValue" }, script = "window.localStorage.setItem(aKey,aValue);")
    private static native void setStringValueJS(String aKey, String aValue);

    @JSBody(params={ "anIndex" }, script = "return window.localStorage.key(anIndex);")
    public static native String getKey(int anIndex);

    @JSBody(params={ }, script = "return window.localStorage.clear();")
    private static native void clearJS();
}
