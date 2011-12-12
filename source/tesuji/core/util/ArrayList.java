/**
 * Project: Tesuji Go Framework.<br>
 * <br>
 * <font color="#CC6600"><font size=-1> Copyright (c) 1985-2006 Mark Boon<br>
 * All rights reserved.<br>
 * <br>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * provided that the above copyright notice(s) and this permission notice appear
 * in all copies of the Software and that both the above copyright notice(s) and
 * this permission notice appear in supporting documentation.<br>
 * <br>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.<br>
 * <br>
 * <font color="#00000"><font size=+1>
 */
package tesuji.core.util;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * Straightforward array implementation of a generic list.<br>
 * <br>
 * For some reason all out-of-the-box implementations are incredibly slow.
 * One thing must be noted though, that one of the ways this implementation
 * achieves its speed is by not maintaining strict order of the items. To
 * keep a list in its original order, use the methods insert() and delete()
 * instead of their otherwise equivalent add() and remove().
 * 
 * Another caveat is this class is not thread-safe. Factory classes should use
 * SynchronizedArrayStack to keep their objects in a multi-threaded application.
 */
public class ArrayList<Type>
	implements List<Type>, Serializable
{
	/**
	 * Generated serial number
	 */
	private static final long serialVersionUID = 4054664430781805299L;

	private static final int DEFAULT_CAPACITY = 16;
	
	protected int _capacity;
	protected int _index;
	protected Type[] _array;
	
	/**
	 * Default constructor, using DEFAULT_CAPACITY.
	 */
	public ArrayList()
	{
		this(DEFAULT_CAPACITY);
	}
	
	/**
	 * Constructor using the capacity argument to allocate the initial list.
	 * @param capacity
	 */
	@SuppressWarnings("unchecked")
	public ArrayList(int capacity)
	{
		_capacity = capacity;
		_array = (Type[]) new Object[capacity];
	}
	
	/* (non-Javadoc)
	 * @see tesuji.coer.util.List#isEmpty()
	 */
	public final boolean isEmpty()
	{
		return (_index==0);
	}
	
	/**
	 * Add an object at a certain index in the list. Although the objects will 
	 * initially appear in the list in the same order as they were originally 
	 * added, subsequent modifications to the list may change this order in
	 * unexpected ways.
	 * @param o is the new object to put in the list.
	 */
	public final void add(Type o)
	{
		if (_index==_capacity)
			resize();
		
		_array[_index++] = o;
	}
	
	/**
	 * Add an object at a certain index in the list. Note that this may change 
	 * the order of the remaining items in the list. Use insert() if the order
	 * needs to remain intact.
	 * @param i is the index where to put the new oject
	 * @param o is the new object to put in the list.
	 */
	public final void add(int i, Type o)
	{
		if (i>_index)
			throw new IndexOutOfBoundsException("Index "+i+" is beyond last item");

		if (_index==_capacity)
			resize();
		
		_array[_index++] = _array[i];
		_array[i] = o;
	}
	
	/**
	 * Insert an object at a certain index in the list.
	 * This operation is more expensive than te add() method but it retains the
	 * order of the elements.
	 * @param i is the index where to put the new oject
	 * @param o is the new object to put in the list.
	 */
	public final void insert(int i, Type o)
	{
		if (i>_index)
			throw new IndexOutOfBoundsException("Index "+i+" is beyond last item");

		if (_index==_capacity)
			resize();
		
		if (i<_index)
			System.arraycopy(_array, i, _array, i+1, _index-i);
		_array[i] = o;
		_index++;
	}
	
	/**
	 * Replace the object at a certain index with the one passed to this method.
	 * @param i is the index where to put the new oject
	 * 
	 * @param o is the new object to put in the list.
	 */
	public final void set(int i, Type o)
	{
		if (i>=_index)
			throw new IndexOutOfBoundsException("Index "+i+" is beyond last item");

		_array[i] = o;
	}
	
	/**
	 * Remove an object from the list. Note that this may change the
	 * order of the remaining items in the list.
	 * 
	 * @param o is the object to be to removed from te list.
	 */
	public final void remove(Type o)
	{
		Type last = _array[_index-1];
		for (int i=_index; --i>=0;)
		{
			if (_array[i].equals(o))
			{
				_array[i] = last;
				_index--;
			}
		}
	}
	
	/**
	 * Remove an object from the list. Note that this may change the
	 * order of the remaining items in the list.
	 * 
	 * @param o is the object to be to removed from te list.
	 */
	public final boolean removeAndCheck(Type o)
	{
		Type last = _array[_index-1];
		for (int i=_index; --i>=0;)
		{
			if (/*_array[i]==o || */_array[i].equals(o))
			{
				_array[i] = last;
				_index--;
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Remove the object at a specific index. Note that this may change the
	 * order of the remaining items in the list.
	 * @param i is the index at whicch to remove the object.
	 */
	public final void remove(int i)
	{
		if (i>_index)
			throw new IndexOutOfBoundsException("Index "+i+" is beyond last item");

		_array[i] = _array[_index-1];
		_index--;
	}
	
	/**
	 * Remove the object at a specific index. Note that this is much slower than
	 * the remove() method but it maintains the order of the items.
	 * 
	 * @param i is the index at whicch to remove the object.
	 */
	public final void delete(int i)
	{
		if (i>_index)
			throw new IndexOutOfBoundsException("Index "+i+" is beyond last item");

		if (i>=_index-1)
			System.arraycopy(_array, i+1, _array, i, _index-i-1);
		_index--;
	}
	
	/**
	 * Remove an object from the list. Note that this is much slower than
	 * the remove() method but it maintains the order of the items.
	 * 
	 * @param o is the object to be to removed from te list.
	 */
	public final void delete(Type o)
	{
		for (int i=_index; --i>=0;)
		{
			if (_array[i].equals(o))
				delete(i);
		}
	}
	
	/* (non-Javadoc)
	 * @see tesuji.core.util.List#get(int)
	 */
	public final Type get(int index)
	{
		return _array[index];
	}
	
	/* (non-Javadoc)
	 * @see tesuji.core.util.List#toArray()
	 */
	public final Type[] toArray()
	{
		return _array;
	}
	
	/* (non-Javadoc)
	 * @see tesuji.core.util.List#size()
	 */
	public final int size()
	{
		return _index;
	}
	
	/* (non-Javadoc)
	 * @see tesuji.core.util.List#clear()
	 */
	public void clear()
	{
		_index = 0;
	}
	
	public int getCapacity()
	{
		return _capacity;
	}
	
	/**
	 * @param o is the object to look up.
	 * @return the index at which an object exists in the list. It returns -1
	 * if the list doesn't contain it.
	 */
	public int indexOf(Type o)
	{
		for (int i=_index; --i>=0;)
		{
			if (_array[i].equals(o))
				return i;
		}
		return -1;
	}
	
	/**
	 * Double the capacity of the list.
	 */
	@SuppressWarnings("unchecked")
	private final void resize()
	{
		int newCapacity;
		if (_capacity==0)
			newCapacity = 1;
		else
			newCapacity = _capacity*2;
		
		Type[] newArray = (Type[]) new Object[newCapacity];
		if (_capacity>0)
			System.arraycopy(_array, 0, newArray, 0, _capacity);
		_array = newArray;
		_capacity = newCapacity;
	}
	
	public String toString()
	{
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("["+_index+"]{");
		for (int i=0; i<_index && i<20; i++)
		{
			stringBuffer.append(_array[i].toString());
			if (i<_index-1)
				stringBuffer.append(" | ");
			else if (i==19 && _index!=20)
				stringBuffer.append(" | ...");
		}
		stringBuffer.append("}");
		return stringBuffer.toString();
	}

	/**
	 * Return an iterator for this object. This is used for automatic for-loops.
	 * But since it allocates using 'new' it should be used sparsely. Maybe
	 * some day we can build in a pooling mechanism for the iterators.
	 */
    public Iterator<Type> iterator()
    {
        return new Iterator<Type>()
        {
            private int iteratorIndex = 0;

            public final boolean hasNext()
            {
                return (iteratorIndex<_index);
            }

            public final Type next()
            {
                return _array[iteratorIndex++];
            }

            public final void remove()
            {   
            }
            
        };
    }

	/**
	 * Return an Enumeration for this object. This is used for backward compatibility.
	 * The TreeNode interface in Swing is still backwards in this respect.
	 */
    public Enumeration<Type> elements()
    {
        return new Enumeration<Type>()
        {
            private int enumerationIndex = 0;

            public final boolean hasMoreElements()
            {
                return (enumerationIndex<_index);
            }

            public final Type nextElement()
            {
                return _array[enumerationIndex++];
            }
        };
    }

    public ArrayList<Type> createClone()
    {
    	ArrayList<Type> clone = new ArrayList<Type>(_capacity);
    	clone._index = _index;
    	System.arraycopy(toArray(), 0, clone.toArray(), 0, _index);
    	
    	return clone;
    }
}
