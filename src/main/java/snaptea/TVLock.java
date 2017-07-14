package snaptea;

/**
 * A custom class.
 */
public class TVLock {

    Object _lock = this; //new Object();
    boolean   _finished;
    String    _name;
    boolean   _debug;

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
        if(_debug && _name!=null) System.out.println("Wait: " + _name);
        while(!_finished)
            try { _lock.wait(); }
            catch(InterruptedException e) { throw new RuntimeException(e); }
        if(_debug && _name!=null) System.out.println("WaitDone: " + _name);
    }
}

/**
 * Called to notify finished.
 */
public void unlock()
{
    synchronized(_lock) {
        _finished = true;
        if(_debug && _name!=null) System.out.println("Notify: " + _name);
        _lock.notify();
        if(_debug && _name!=null) System.out.println("NotifyDone: " + _name);
    }
}

}