package snaptea;
import org.teavm.jso.JSBody;
import snap.util.Prefs;
import java.util.ArrayList;
import java.util.List;

/**
 * A Prefs implementation for TeaVM using LocalStorage.
 */
public class TVPrefs extends Prefs {

    // The shared prefs
    private static TVPrefs _shared;

    /**
     * Override to use LocalStorage.
     */
    @Override
    public Object getValue(String aKey, Object aDefault)
    {
        if (isValueNumberJS(aKey)) {
            double val = getNumberValueJS(aKey);
            return val;
        }
        String val = getStringValueJS(aKey);
        return val!=null ? val : aDefault;
    }

    @JSBody(params={ "aKey" }, script = "return typeof window.localStorage.getItem(aKey) === 'number';")
    public static native boolean isValueNumberJS(String aKey);

    @JSBody(params={ "aKey" }, script = "return window.localStorage.getItem(aKey);")
    public static native String getStringValueJS(String aKey);

    @JSBody(params={ "aKey" }, script = "return window.localStorage.getItem(aKey);")
    public static native double getNumberValueJS(String aKey);

    /**
     * Override to use LocalStorage.
     */
    @Override
    public void setValue(String aKey, Object aValue)
    {
        if (aValue instanceof Number) {
            double val = ((Number)aValue).doubleValue();
            setDoubleValueJS(aKey, val);
        }
        else {
            String val = aValue!=null ? aValue.toString() : null;
            setStringValueJS(aKey, val);
        }
    }

    @JSBody(params={ "aKey", "aValue" }, script = "window.localStorage.setItem(aKey,aValue);")
    public static native void setStringValueJS(String aKey, String aValue);

    @JSBody(params={ "aKey", "aValue" }, script = "window.localStorage.setItem(aKey,aValue);")
    public static native void setDoubleValueJS(String aKey, double aValue);

    /**
     * Override to use LocalStorage.
     */
    @Override
    public String[] getKeys()
    {
        List<String> keys = new ArrayList<>();
        for (int i=0; i<1000;i++) {
            Object key = getKey(i);
            if (key==null)
                break;
            keys.add(key.toString());
        }
        return keys.toArray(new String[0]);
    }

    @JSBody(params={ "anIndex" }, script = "return window.localStorage.key(anIndex);")
    public static native String getKey(int anIndex);

    /**
     * Clears all the preferences.
     */
    public void clear()
    {
        clearJS();
    }

    @JSBody(params={ }, script = "return window.localStorage.clear();")
    public static native void clearJS();

    /**
     * Returns the shared prefs.
     */
    public static TVPrefs get()
    {
        if (_shared!=null) return _shared;
        _shared = new TVPrefs();
        return _shared;
    }
}
