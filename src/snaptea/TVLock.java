package snaptea;

/**
 * A custom class.
 */
public class TVLock {

    Object _lock = this; //new Object();
    boolean _waiting, _finished;
    String _name;

/**
 * Creates a new lock.
 */
public TVLock()  { }

/**
 * Creates a new lock.
 */
public TVLock(String aName)  { _name = aName; }

/**
 * Called to wait until finished.
 */    
public void lock()
{
    synchronized(_lock) {
        if(_finished) return;
        if(_name!=null) System.out.println("Wait: " + _name);
        try { _waiting = true; _lock.wait(); _waiting = false; }
        catch(Exception e) { throw new RuntimeException(e); }
        if(_name!=null) System.out.println("WaitDone: " + _name);
    }
}

/**
 * Called to notify finished.
 */
public void unlock()
{
    synchronized(_lock) {
        _finished = true;
        if(!_waiting) return;
        if(_name!=null) System.out.println("Notify: " + _name);
        try { _lock.notify(); }
        catch(Exception e) { throw new RuntimeException(e); }
        if(_name!=null) System.out.println("NotifyDone: " + _name);
    }
}

}