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
public TScanner(InputStream aIS)
{
    _is = aIS;
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

public int nextInt()
{
    String str = next();
    return Integer.valueOf(str);
}

/**
 * Returns the next token.
 */
public String next()
{
    String str = peekNext();
    _index++;
    return str;
}

/**
 * Returns the next token.
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
    String toks[] = aStr.split("\\s");
    int len = _tokens.length;
    _tokens = Arrays.copyOf(_tokens, len + toks.length);
    System.arraycopy(toks, 0, _tokens, len, toks.length);
}

}