package snaptea;
import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSArray;

/**
 * JSPromise.
 */
public abstract class JSPromise<T extends org.teavm.jso.JSObject> implements JSThenable<T>, org.teavm.jso.JSObject {
    @org.teavm.jso.JSFunctor
    public interface CatchOnRejectedCallbackFn<V extends org.teavm.jso.JSObject> extends org.teavm.jso.JSObject {
        JSThenable<V> onInvoke(org.teavm.jso.JSObject error);
    }

    @org.teavm.jso.JSFunctor
    public interface PromiseExecutorCallbackFn<T extends org.teavm.jso.JSObject> extends org.teavm.jso.JSObject {
        @org.teavm.jso.JSFunctor
        interface RejectCallbackFn extends org.teavm.jso.JSObject {
            void onInvoke(org.teavm.jso.JSObject error);
        }

        @org.teavm.jso.JSFunctor
        interface ResolveCallbackFn<T extends org.teavm.jso.JSObject> extends org.teavm.jso.JSObject {
            public abstract static class ResolveUnionType<T extends org.teavm.jso.JSObject> implements org.teavm.jso.JSObject {
                public T asT()
                {
                    return this.cast();//jsinterop.base.Js.cast(this);
                }

                public JSThenable<T> asIThenable()
                {
                    return this.cast();//jsinterop.base.Js.cast(this);
                }

                public static <T extends org.teavm.jso.JSObject> JSPromise.PromiseExecutorCallbackFn.ResolveCallbackFn.ResolveUnionType<T> of(org.teavm.jso.JSObject o)
                {
                    return o.cast();//jsinterop.base.Js.cast(o);
                }

                @org.teavm.jso.JSBody(params = "o", script = "return o")
                public static native <T extends org.teavm.jso.JSObject> JSPromise.PromiseExecutorCallbackFn.ResolveCallbackFn.ResolveUnionType<T> of(JSThenable<T> o);

                @org.teavm.jso.JSBody(params = "o", script = "return o")
                public static native <T extends org.teavm.jso.JSObject> JSPromise.PromiseExecutorCallbackFn.ResolveCallbackFn.ResolveUnionType<T> of(java.lang.String o);
            }

            default void onInvoke(JSThenable<T> value)
            {
                onInvoke(JSPromise.PromiseExecutorCallbackFn.ResolveCallbackFn.ResolveUnionType.of(value));
            }

            void onInvoke(JSPromise.PromiseExecutorCallbackFn.ResolveCallbackFn.ResolveUnionType<T> p0);

            default void onInvoke(T value)
            {
                onInvoke(JSPromise.PromiseExecutorCallbackFn.ResolveCallbackFn.ResolveUnionType.of(value));
            }
        }

        void onInvoke(JSPromise.PromiseExecutorCallbackFn.ResolveCallbackFn<T> resolve, JSPromise.PromiseExecutorCallbackFn.RejectCallbackFn reject);
    }

    public abstract static class ResolveValueUnionType<V extends org.teavm.jso.JSObject> implements org.teavm.jso.JSObject {
        public static <V extends org.teavm.jso.JSObject> JSPromise.ResolveValueUnionType<V> of(org.teavm.jso.JSObject o)
        {
            return o.cast();//jsinterop.base.Js.cast(o);
        }

        public V asV()
        {
            return this.cast();//jsinterop.base.Js.cast(this);
        }

        public JSThenable<V> asIThenable()
        {
            return this.cast();//jsinterop.base.Js.cast(this);
        }

        @org.teavm.jso.JSBody(params = "o", script = "return o")
        public static native <V extends org.teavm.jso.JSObject> JSPromise.ResolveValueUnionType<V> of(JSThenable<V> o);

        @org.teavm.jso.JSBody(params = "o", script = "return o")
        public static native <V extends org.teavm.jso.JSObject> JSPromise.ResolveValueUnionType<V> of(java.lang.String o);
    }

    @org.teavm.jso.JSBody(params = {"_promises"}, script = "return Promise.all(_promises);")
    public static native <V extends org.teavm.jso.JSObject> JSPromise<JSArray<V>> all(JSThenable<? extends V>... promises);

    @org.teavm.jso.JSBody(params = {"_promises"}, script = "return Promise.race(_promises);")
    public static native <V extends org.teavm.jso.JSObject> JSPromise<V> race(JSThenable<? extends V>... promises);

    @org.teavm.jso.JSBody(params = {"_error"}, script = "return Promise.reject(_error);")
    public static native JSPromise<JSObject> reject(org.teavm.jso.JSObject error);

    public static final <V extends org.teavm.jso.JSObject> JSPromise<V> resolve(JSThenable<V> value)
    {
        return JSPromise.resolve(JSPromise.ResolveValueUnionType.of(value));
    }

    @org.teavm.jso.JSBody(params = {"_value"}, script = "return Promise.resolve(_value);")
    public static native <V extends org.teavm.jso.JSObject> JSPromise<V> resolve(JSPromise.ResolveValueUnionType<V> value);

    public static final <V extends org.teavm.jso.JSObject> JSPromise<V> resolve(V value)
    {
        return JSPromise.resolve(JSPromise.ResolveValueUnionType.of(value));
    }

    protected JSPromise(JSPromise.PromiseExecutorCallbackFn<T> executor)  { }

    @org.teavm.jso.JSMethod(value = "catch")
    public abstract <V extends org.teavm.jso.JSObject> JSPromise<V> catch_(JSPromise.CatchOnRejectedCallbackFn<? extends V> onRejected);

    public abstract <V extends org.teavm.jso.JSObject> JSPromise<V> then(JSThenable.ThenOnFulfilledCallbackFn<? super T, ? extends V> onFulfilled, JSThenable.ThenOnRejectedCallbackFn<? extends V> onRejected);

    public abstract <V extends org.teavm.jso.JSObject> JSPromise<V> then(JSThenable.ThenOnFulfilledCallbackFn<? super T, ? extends V> onFulfilled);

    @org.teavm.jso.JSBody(params = {"_promises$1"}, script = "return Promise.all(_promises$1);")
    public static native <V extends org.teavm.jso.JSObject> JSPromise<JSArray<V>> all(JSThenable<? extends V> promises$1);

    @org.teavm.jso.JSBody(params = {"_promises$1", "_promises$2"}, script = "return Promise.all(_promises$1,_promises$2);")
    public static native <V extends org.teavm.jso.JSObject> JSPromise<JSArray<V>> all(JSThenable<? extends V> promises$1, JSThenable<? extends V> promises$2);

    @org.teavm.jso.JSBody(params = {"_promises$1", "_promises$2", "_promises$3"}, script = "return Promise.all(_promises$1,_promises$2,_promises$3);")
    public static native <V extends org.teavm.jso.JSObject> JSPromise<JSArray<V>> all(JSThenable<? extends V> promises$1, JSThenable<? extends V> promises$2, JSThenable<? extends V> promises$3);

    @org.teavm.jso.JSBody(params = {"_promises$1", "_promises$2", "_promises$3", "_promises$4"}, script = "return Promise.all(_promises$1,_promises$2,_promises$3,_promises$4);")
    public static native <V extends org.teavm.jso.JSObject> JSPromise<JSArray<V>> all(JSThenable<? extends V> promises$1, JSThenable<? extends V> promises$2, JSThenable<? extends V> promises$3, JSThenable<? extends V> promises$4);

    @org.teavm.jso.JSBody(params = {"_promises$1", "_promises$2", "_promises$3", "_promises$4", "_promises$5"}, script = "return Promise.all(_promises$1,_promises$2,_promises$3,_promises$4,_promises$5);")
    public static native <V extends org.teavm.jso.JSObject> JSPromise<JSArray<V>> all(JSThenable<? extends V> promises$1, JSThenable<? extends V> promises$2, JSThenable<? extends V> promises$3, JSThenable<? extends V> promises$4, JSThenable<? extends V> promises$5);

    @org.teavm.jso.JSBody(params = {"_promises$1", "_promises$2", "_promises$3", "_promises$4", "_promises$5", "_promises$6"}, script = "return Promise.all(_promises$1,_promises$2,_promises$3,_promises$4,_promises$5,_promises$6);")
    public static native <V extends org.teavm.jso.JSObject> JSPromise<JSArray<V>> all(JSThenable<? extends V> promises$1, JSThenable<? extends V> promises$2, JSThenable<? extends V> promises$3, JSThenable<? extends V> promises$4, JSThenable<? extends V> promises$5, JSThenable<? extends V> promises$6);

    @org.teavm.jso.JSBody(params = {"_promises$1", "_promises$2", "_promises$3", "_promises$4", "_promises$5", "_promises$6", "_promises$7"}, script = "return Promise.all(_promises$1,_promises$2,_promises$3,_promises$4,_promises$5,_promises$6,_promises$7);")
    public static native <V extends org.teavm.jso.JSObject> JSPromise<JSArray<V>> all(JSThenable<? extends V> promises$1, JSThenable<? extends V> promises$2, JSThenable<? extends V> promises$3, JSThenable<? extends V> promises$4, JSThenable<? extends V> promises$5, JSThenable<? extends V> promises$6, JSThenable<? extends V> promises$7);

    @org.teavm.jso.JSBody(params = {"_promises$1", "_promises$2", "_promises$3", "_promises$4", "_promises$5", "_promises$6", "_promises$7", "_promises$8"}, script = "return Promise.all(_promises$1,_promises$2,_promises$3,_promises$4,_promises$5,_promises$6,_promises$7,_promises$8);")
    public static native <V extends org.teavm.jso.JSObject> JSPromise<JSArray<V>> all(JSThenable<? extends V> promises$1, JSThenable<? extends V> promises$2, JSThenable<? extends V> promises$3, JSThenable<? extends V> promises$4, JSThenable<? extends V> promises$5, JSThenable<? extends V> promises$6, JSThenable<? extends V> promises$7, JSThenable<? extends V> promises$8);

    @org.teavm.jso.JSBody(params = {"_promises$1", "_promises$2", "_promises$3", "_promises$4", "_promises$5", "_promises$6", "_promises$7", "_promises$8", "_promises$9"}, script = "return Promise.all(_promises$1,_promises$2,_promises$3,_promises$4,_promises$5,_promises$6,_promises$7,_promises$8,_promises$9);")
    public static native <V extends org.teavm.jso.JSObject> JSPromise<JSArray<V>> all(JSThenable<? extends V> promises$1, JSThenable<? extends V> promises$2, JSThenable<? extends V> promises$3, JSThenable<? extends V> promises$4, JSThenable<? extends V> promises$5, JSThenable<? extends V> promises$6, JSThenable<? extends V> promises$7, JSThenable<? extends V> promises$8, JSThenable<? extends V> promises$9);

    @org.teavm.jso.JSBody(params = {"_promises$1", "_promises$2", "_promises$3", "_promises$4", "_promises$5", "_promises$6", "_promises$7", "_promises$8", "_promises$9", "_promises$10"}, script = "return Promise.all(_promises$1,_promises$2,_promises$3,_promises$4,_promises$5,_promises$6,_promises$7,_promises$8,_promises$9,_promises$10);")
    public static native <V extends org.teavm.jso.JSObject> JSPromise<JSArray<V>> all(JSThenable<? extends V> promises$1, JSThenable<? extends V> promises$2, JSThenable<? extends V> promises$3, JSThenable<? extends V> promises$4, JSThenable<? extends V> promises$5, JSThenable<? extends V> promises$6, JSThenable<? extends V> promises$7, JSThenable<? extends V> promises$8, JSThenable<? extends V> promises$9, JSThenable<? extends V> promises$10);

    @org.teavm.jso.JSBody(params = {"_promises$1"}, script = "return Promise.race(_promises$1);")
    public static native <V extends org.teavm.jso.JSObject> JSPromise<V> race(JSThenable<? extends V> promises$1);

    @org.teavm.jso.JSBody(params = {"_promises$1", "_promises$2"}, script = "return Promise.race(_promises$1,_promises$2);")
    public static native <V extends org.teavm.jso.JSObject> JSPromise<V> race(JSThenable<? extends V> promises$1, JSThenable<? extends V> promises$2);

    @org.teavm.jso.JSBody(params = {"_promises$1", "_promises$2", "_promises$3"}, script = "return Promise.race(_promises$1,_promises$2,_promises$3);")
    public static native <V extends org.teavm.jso.JSObject> JSPromise<V> race(JSThenable<? extends V> promises$1, JSThenable<? extends V> promises$2, JSThenable<? extends V> promises$3);

    @org.teavm.jso.JSBody(params = {"_promises$1", "_promises$2", "_promises$3", "_promises$4"}, script = "return Promise.race(_promises$1,_promises$2,_promises$3,_promises$4);")
    public static native <V extends org.teavm.jso.JSObject> JSPromise<V> race(JSThenable<? extends V> promises$1, JSThenable<? extends V> promises$2, JSThenable<? extends V> promises$3, JSThenable<? extends V> promises$4);

    @org.teavm.jso.JSBody(params = {"_promises$1", "_promises$2", "_promises$3", "_promises$4", "_promises$5"}, script = "return Promise.race(_promises$1,_promises$2,_promises$3,_promises$4,_promises$5);")
    public static native <V extends org.teavm.jso.JSObject> JSPromise<V> race(JSThenable<? extends V> promises$1, JSThenable<? extends V> promises$2, JSThenable<? extends V> promises$3, JSThenable<? extends V> promises$4, JSThenable<? extends V> promises$5);

    @org.teavm.jso.JSBody(params = {"_promises$1", "_promises$2", "_promises$3", "_promises$4", "_promises$5", "_promises$6"}, script = "return Promise.race(_promises$1,_promises$2,_promises$3,_promises$4,_promises$5,_promises$6);")
    public static native <V extends org.teavm.jso.JSObject> JSPromise<V> race(JSThenable<? extends V> promises$1, JSThenable<? extends V> promises$2, JSThenable<? extends V> promises$3, JSThenable<? extends V> promises$4, JSThenable<? extends V> promises$5, JSThenable<? extends V> promises$6);

    @org.teavm.jso.JSBody(params = {"_promises$1", "_promises$2", "_promises$3", "_promises$4", "_promises$5", "_promises$6", "_promises$7"}, script = "return Promise.race(_promises$1,_promises$2,_promises$3,_promises$4,_promises$5,_promises$6,_promises$7);")
    public static native <V extends org.teavm.jso.JSObject> JSPromise<V> race(JSThenable<? extends V> promises$1, JSThenable<? extends V> promises$2, JSThenable<? extends V> promises$3, JSThenable<? extends V> promises$4, JSThenable<? extends V> promises$5, JSThenable<? extends V> promises$6, JSThenable<? extends V> promises$7);

    @org.teavm.jso.JSBody(params = {"_promises$1", "_promises$2", "_promises$3", "_promises$4", "_promises$5", "_promises$6", "_promises$7", "_promises$8"}, script = "return Promise.race(_promises$1,_promises$2,_promises$3,_promises$4,_promises$5,_promises$6,_promises$7,_promises$8);")
    public static native <V extends org.teavm.jso.JSObject> JSPromise<V> race(JSThenable<? extends V> promises$1, JSThenable<? extends V> promises$2, JSThenable<? extends V> promises$3, JSThenable<? extends V> promises$4, JSThenable<? extends V> promises$5, JSThenable<? extends V> promises$6, JSThenable<? extends V> promises$7, JSThenable<? extends V> promises$8);

    @org.teavm.jso.JSBody(params = {"_promises$1", "_promises$2", "_promises$3", "_promises$4", "_promises$5", "_promises$6", "_promises$7", "_promises$8", "_promises$9"}, script = "return Promise.race(_promises$1,_promises$2,_promises$3,_promises$4,_promises$5,_promises$6,_promises$7,_promises$8,_promises$9);")
    public static native <V extends org.teavm.jso.JSObject> JSPromise<V> race(JSThenable<? extends V> promises$1, JSThenable<? extends V> promises$2, JSThenable<? extends V> promises$3, JSThenable<? extends V> promises$4, JSThenable<? extends V> promises$5, JSThenable<? extends V> promises$6, JSThenable<? extends V> promises$7, JSThenable<? extends V> promises$8, JSThenable<? extends V> promises$9);

    @org.teavm.jso.JSBody(params = {"_promises$1", "_promises$2", "_promises$3", "_promises$4", "_promises$5", "_promises$6", "_promises$7", "_promises$8", "_promises$9", "_promises$10"}, script = "return Promise.race(_promises$1,_promises$2,_promises$3,_promises$4,_promises$5,_promises$6,_promises$7,_promises$8,_promises$9,_promises$10);")
    public static native <V extends org.teavm.jso.JSObject> JSPromise<V> race(JSThenable<? extends V> promises$1, JSThenable<? extends V> promises$2, JSThenable<? extends V> promises$3, JSThenable<? extends V> promises$4, JSThenable<? extends V> promises$5, JSThenable<? extends V> promises$6, JSThenable<? extends V> promises$7, JSThenable<? extends V> promises$8, JSThenable<? extends V> promises$9, JSThenable<? extends V> promises$10);

    @org.teavm.jso.JSBody(params = {"_executor"}, script = " return new Promise(_executor);")
    public static native <T extends org.teavm.jso.JSObject> JSPromise<T> newPromise(JSPromise.PromiseExecutorCallbackFn<T> executor);

}