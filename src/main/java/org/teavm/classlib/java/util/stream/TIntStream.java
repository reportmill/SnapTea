package org.teavm.classlib.java.util.stream;
import org.teavm.classlib.java.util.function.*;

/**
 * A custom class.
 */
public interface TIntStream {
    
default TIntStream range(int s, int e)  { return new IntStreamImpl(s, e); }

default TIntStream parrallel()  { return this; }

default void forEach(TIntConsumer action)
{
    
}

public static class IntStreamImpl implements TIntStream {
    
    int _start, _end;
    
    public IntStreamImpl(int s, int e)  { _start = s; _end = e; }
    
    public TIntStream range(int s, int e)  { return new IntStreamImpl(s, e); }
    
    public TIntStream parrallel()  { return this; }
    
    public void forEach(TIntConsumer action) { }
}

}