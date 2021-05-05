package org.teavm.classlib.java.util;
import java.util.*;
import java.io.*;

/**
 * A basic implementation of java.util.Scanner.
 */
public class TScanner {

    // The source InputStream to read input bytes from
    private InputStream  _inputStream;

    // The current tokens
    private String[]  _tokens = new String[0];
    
    // The index of next token
    private int  _index;
    
    // Whether scanner is done reading input stream
    private boolean  _done;

    /**
     * Create new Scanner for InputStream.
     */
    public TScanner(InputStream aIS)
    {
        _inputStream = aIS;
    }

    /**
     * Create new Scanner for InputStream.
     */
    public TScanner(String aStr)
    {
        byte[] bytes = aStr.getBytes();
        _inputStream = new ByteArrayInputStream(bytes);
    }

    /**
     * Returns whether next input is available.
     */
    public boolean hasNext()
    {
        String str = peekNext();
        return str != null && str.length() > 0;
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
        try {
            Float.valueOf(str);
            return true;
        }
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
        try {
            Integer.valueOf(str);
            return true;
        }
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
        try {
            Double.valueOf(str);
            return true;
        }
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
    public boolean hasNextLine()
    {
        return _index < _tokens.length && !_done;
    }

    /**
     * Returns the next line of input.
     */
    public String nextLine()
    {
        String str = next();
        while (_index < _tokens.length)
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

        // If another token still available, return it
        if(_index < _tokens.length)
            return _tokens[_index];

        // Otherwise return null
        return null;
    }

    /**
     * Reads a new line of text.
     */
    private void readNext()
    {
        try {

            // Read next bytes, just return if null
            byte[] nextBytes = readNextBytes();
            if (nextBytes == null)
                return;

            // Otherwise, create string, add chars
            String str = new String(nextBytes);
            addChars(str);
        }

        // Catch exception and complain
        catch(Exception e) { throw new RuntimeException(e); }
    }

    /**
     * Returns next array of bytes from InputStream.
     */
    private byte[] readNextBytes() throws IOException
    {
        // Create a ByteArrayOutputStream to hold bytes and byte buffer
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        byte chunk[] = new byte[8192];

        // Read from InputStream to ByteStream
        int len = _inputStream.read(chunk, 0, chunk.length);
        while (len > 0) {
            byteStream.write(chunk, 0, len);
            len = _inputStream.read(chunk, 0, chunk.length);
        }

        // If len is negative, InputStream is done
        if (len < 0)
            _done = true;

        // Return bytes
        return byteStream.toByteArray();
    }

    /**
     * Adds characters to input.
     */
    private void addChars(String aStr)
    {
        // Get new tokens from given string
        String[] newTokens = aStr.split("\\s");
        if (newTokens.length == 0)
            newTokens = new String[] { "" };

        // Extend Tokens array and copy new tokens in
        int oldLength = _tokens.length;
        _tokens = Arrays.copyOf(_tokens, oldLength + newTokens.length);
        System.arraycopy(newTokens, 0, _tokens, oldLength, newTokens.length);
    }
}