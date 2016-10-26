package org.teavm.classlib.java.util;
import java.util.*;
import java.io.*;

/**
 * A custom class.
 */
public class TScanner {
    
    // The current tokens
    String          _tokens[] = new String[0];
    
    // The index of next token
    int             _index;
    
    // Whether scanner is done reading input stream
    boolean         _done;

    // The InputStream
    InputStream     _is;

/**
 * Create new Scanner for InputStream.
 */
public TScanner(InputStream aIS)  { _is = aIS; }

/**
 * Returns the next token.
 */
public String next()
{
    String str = peekNext(); _index++;
    return str;
}

/**
 * Returns whether next input is boolean.
 */
public boolean hasNextBoolean()
{
    String str = peekNext().toLowerCase();
    return str.equals("true") || str.equals("false");
}

/**
 * Returns the next input as boolean.
 */
public boolean nextBoolean()
{
    String str = next();
    return Boolean.valueOf(str);
}

/**
 * Returns whether next input is float.
 */
public boolean hasNextFloat()
{
    String str = peekNext();
    try { Float.valueOf(str); return true; }
    catch(Exception e) { return false; }
}

/**
 * Returns the next input as float.
 */
public float nextFloat()
{
    String str = next();
    return Float.valueOf(str);
}

/**
 * Returns whether next input is int.
 */
public boolean hasNextInt()
{
    String str = peekNext();
    try { Integer.valueOf(str); return true; }
    catch(Exception e) { return false; }
}

/**
 * Returns the next input as int.
 */
public int nextInt()
{
    String str = next();
    return Integer.valueOf(str);
}

/**
 * Returns whether next input is double.
 */
public boolean hasNextDouble()
{
    String str = peekNext();
    try { Double.valueOf(str); return true; }
    catch(Exception e) { return false; }
}

/**
 * Returns the next input as double.
 */
public double nextDouble()
{
    String str = next();
    return Double.valueOf(str);
}

/**
 * Returns whether there is an input line.
 */
public boolean hasNextLine()  { return _index < _tokens.length && !_done; }

/**
 * Returns the next line of input.
 */
public String nextLine()
{
    String str = next();
    while(_index<_tokens.length)
        str += next() + " ";
    return str;
}

/**
 * Returns the next token without removing it.
 */
public String peekNext()
{
    while (_index >= _tokens.length && !_done)
        readNext();
    
    if(_index<_tokens.length)
        return _tokens[_index];
    return null;
}

/**
 * Reads a new line of text.
 */
private void readNext()
{
    try {
        byte bytes[] = new byte[1024];
        int len = _is.read(bytes, 0, 1024);
        String str = new String(bytes, 0, len);
        if(len>=0) addChars(str);
        else _done = true;
    }
    catch(Exception e) { throw new RuntimeException(e); }
}

/**
 * Adds characters to input.
 */
private void addChars(String aStr)
{
    String toks[] = aStr.split("\\s"); if(toks.length==0) toks = new String[] { "" };
    int len = _tokens.length;
    _tokens = Arrays.copyOf(_tokens, len + toks.length);
    System.arraycopy(toks, 0, _tokens, len, toks.length);
}

}