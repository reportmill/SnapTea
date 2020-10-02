package snaptea;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.core.JSArray;
import org.teavm.jso.core.JSString;

/**
 * JSClipboardItem.
 */
public interface JSClipboardItem extends JSObject {

    @JSProperty
    JSArray<JSString> getTypes();

    JSPromise<Blob> getType(String aMimeType);
}
