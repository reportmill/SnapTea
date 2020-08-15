package snaptea;

import org.teavm.jso.JSObject;

/**
 * Base contract of IThenable promise provided for compatibility with non-official Promise implementations.
 */
public interface JSThenable<T extends org.teavm.jso.JSObject> extends org.teavm.jso.JSObject {

    @org.teavm.jso.JSFunctor
    interface ThenOnFulfilledCallbackFn<T extends org.teavm.jso.JSObject, V extends org.teavm.jso.JSObject> extends org.teavm.jso.JSObject {
        JSThenable<V> onInvoke(T p0);
    }

    @org.teavm.jso.JSFunctor
    interface ThenOnRejectedCallbackFn<V extends org.teavm.jso.JSObject> extends org.teavm.jso.JSObject {
        JSThenable<V> onInvoke(JSObject p0);
    }

    <V extends JSObject> JSThenable<V> then(ThenOnFulfilledCallbackFn<? super T, ? extends V> onFulfilled);

    <V extends JSObject> JSThenable<V> then(ThenOnFulfilledCallbackFn<? super T, ? extends V> onFulfilled, ThenOnRejectedCallbackFn<? extends V> onRejected);
}
