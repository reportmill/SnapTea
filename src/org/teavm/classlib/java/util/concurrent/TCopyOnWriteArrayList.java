package org.teavm.classlib.java.util.concurrent;
import java.util.*;

/**
 * A custom class.
 */
public class TCopyOnWriteArrayList <E> implements List<E> {

    // The real array list.
    List <E>       _list;
    
/**
 * Creates from given list.
 */
public TCopyOnWriteArrayList(Collection c)
{
    _list = new ArrayList(c);
}

public int size()  { return _list.size(); }
public E get(int index)  { return _list.get(index); }

public void add(int index, E element)
{
    List <E> list = new ArrayList(_list);
    list.add(index, element);
    _list = list;
}

public boolean add(E e)  { add(size(), e); return true; }

public boolean remove(Object o)
{
    List <E> list = new ArrayList(_list);
    boolean val = list.remove(o);
    _list = list;
    return val;
}

public E remove(int index)
{
    List <E> list = new ArrayList(_list);
    E item = list.remove(index);
    _list = list;
    return item;
}

public E set(int index, E element)
{
    List <E> list = new ArrayList(_list);
    E item = list.set(index, element);
    _list = list;
    return item;
}

public void clear()  { _list = new ArrayList(); }

public boolean addAll(Collection<? extends E> c)  { return addAll(size(),c); }

public boolean addAll(int index, Collection<? extends E> c)
{
    List <E> list = new ArrayList(_list);
    boolean val = list.addAll(index, c);
    _list = list;
    return val;
}

public boolean removeAll(Collection<?> c)
{
    List <E> list = new ArrayList(_list);
    boolean val = list.removeAll(c);
    _list = list;
    return val;
}

public boolean retainAll(Collection<?> c)
{
    List <E> list = new ArrayList(_list);
    boolean val = list.retainAll(c);
    _list = list;
    return val;
}

public boolean containsAll(Collection<?> c)  { return _list.containsAll(c); }

public int indexOf(Object o)  { return _list.indexOf(o); }
public List<E> subList(int fromIndex, int toIndex)  { return _list.subList(fromIndex,toIndex); }
public ListIterator<E> listIterator()  { return _list.listIterator(); }
public ListIterator<E> listIterator(int index)  { return _list.listIterator(index); }
public int lastIndexOf(Object o)  { return _list.lastIndexOf(o); }
public Object[] toArray()  { return _list.toArray(); }
public <T> T[] toArray(T[] a)  { return _list.toArray(a); }
public Iterator<E> iterator()  { return _list.iterator(); }
public boolean contains(Object o)  { return _list.contains(o); }
public boolean isEmpty()  { return _list.isEmpty(); }

}