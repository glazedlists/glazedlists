/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// the Glazed Lists' change objects
import ca.odell.glazedlists.event.ListEventAssembler;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.event.ListEventPublisher;
import ca.odell.glazedlists.impl.EventListIterator;
import ca.odell.glazedlists.impl.GlazedListsImpl;
import ca.odell.glazedlists.impl.SimpleIterator;
import ca.odell.glazedlists.impl.SubEventList;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;

import java.lang.reflect.Array;
import java.util.*;

/**
 * A convenience class that implements common functionality for all {@link EventList}s.
 *
 * <p>If you are creating a custom {@link EventList}, consider extending the more
 * feature-rich {@link TransformedList}.
 *
 * <p>Documentation Note: Javadoc tags have been copied from the {@link List} API
 * because the <code>javadoc</code> tool does not inherit external descriptions.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public abstract class AbstractEventList<E> implements EventList<E> {

    /** the change event and notification system */
    protected ListEventAssembler<E> updates = null;

    /** the read/write lock provides mutual exclusion to access */
    protected ReadWriteLock readWriteLock = null;

    /** the publisher manages the distribution of changes */
    protected ListEventPublisher publisher = null;

    /**
     * Creates an {@link AbstractEventList} that sends events using the specified
     * {@link ListEventPublisher}.
     *
     * @param publisher the channel for event distribution. If this is <tt>null</tt>,
     *      then a new {@link ListEventPublisher} will be created.
     */
    protected AbstractEventList(ListEventPublisher publisher) {
        if(publisher == null) publisher = ListEventAssembler.createListEventPublisher();
        this.publisher = publisher;
        updates = new ListEventAssembler<E>(this, publisher);
    }

    /**
     * Create an {@link AbstractEventList} that sends events with the default
     * {@link ListEventPublisher}.
     */
    protected AbstractEventList() {
        this(null);
    }

    /** {@inheritDoc} */
    public ListEventPublisher getPublisher() {
        return publisher;
    }

    /** {@inheritDoc} */
    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }

    /** {@inheritDoc} */
    public void addListEventListener(ListEventListener<? super E> listChangeListener) {
        updates.addListEventListener(listChangeListener);
    }

    /** {@inheritDoc} */
    public void removeListEventListener(ListEventListener<? super E> listChangeListener) {
        updates.removeListEventListener(listChangeListener);
    }

    /**
     * Returns the number of elements in this list.  If this list contains
     * more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
     *
     * @return the number of elements in this list.
     */
    public abstract int size();


    /**
     * Returns <tt>true</tt> if this list contains no elements.
     *
     * @return <tt>true</tt> if this list contains no elements.
     */
    public boolean isEmpty() {
        return (size() == 0);
    }

    /**
     * Returns <tt>true</tt> if this list contains the specified element.
     * More formally, returns <tt>true</tt> if and only if this list contains
     * at least one element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param object element whose presence in this list is to be tested.
     * @return <tt>true</tt> if this list contains the specified element.
     * @throws ClassCastException if the type of the specified element
     *         is incompatible with this list (optional).
     * @throws NullPointerException if the specified element is null and this
     *         list does not support null elements (optional).
     */
    public boolean contains(Object object) {
        // for through this, looking for the lucky object
        for(Iterator i = iterator(); i.hasNext(); ) {
            if(GlazedListsImpl.equal(object, i.next())) return true;
        }
        // not found
        return false;
    }

    /**
     * Returns an iterator over the elements in this list in proper sequence.
     *
     * <p>The returned {@link Iterator} will become inconsistent if the
     * {@link EventList} that it views is modified. To overcome this problem,
     * use {@link #listIterator()}. When used concurrently, the returned
     * {@link Iterator} requires locking via this list's
     * {@link #getReadWriteLock() ReadWriteLock}.
     *
     * @return an iterator over the elements in this list in proper sequence.
     */
    public Iterator<E> iterator() {
        return new SimpleIterator<E>(this);
    }

    /**
     * Returns an array containing all of the elements in this list in proper
     * sequence.  Obeys the general contract of the
     * <tt>Collection.toArray</tt> method.
     *
     * @return an array containing all of the elements in this list in proper
     *         sequence.
     * @see Arrays#asList
     */
    public Object[] toArray() {
        // copy values into the array
        Object[] array = new Object[size()];
        int index = 0;
        for(Iterator i = iterator(); i.hasNext(); ) {
            array[index] = i.next();
            index++;
        }
        return array;
    }

    /**
     * Returns an array containing all of the elements in this list in proper
     * sequence; the runtime type of the returned array is that of the
     * specified array.  Obeys the general contract of the
     * <tt>Collection.toArray(Object[])</tt> method.
     *
     * @param array the array into which the elements of this list are to
     *      be stored, if it is big enough; otherwise, a new array of the
     *      same runtime type is allocated for this purpose.
     * @return  an array containing the elements of this list.
     *
     * @throws ArrayStoreException if the runtime type of the specified array
     *        is not a supertype of the runtime type of every element in
     *        this list.
     * @throws NullPointerException if the specified array is <tt>null</tt>.
     */
    public <T> T[] toArray(T[] array) {
        // create an array of the same type as the array passed
        if (array.length < size()) {
            array = (T[]) Array.newInstance(array.getClass().getComponentType(), size());
        } else if(array.length > size()) {
            array[size()] = null;
        }

        // copy values into the array
        int index = 0;
        for(Iterator<E> i = iterator(); i.hasNext(); ) {
            array[index] = (T) i.next();
            index++;
        }
        return array;
    }

    /**
     * Appends the specified element to the end of this list (optional
     * operation).
     *
     * <p>Lists that support this operation may place limitations on what
     * elements may be added to this list.  In particular, some
     * lists will refuse to add null elements, and others will impose
     * restrictions on the type of elements that may be added.  List
     * classes should clearly specify in their documentation any restrictions
     * on what elements may be added.
     *
     * @param value element to be appended to this list.
     * @return <tt>true</tt> (as per the general contract of the
     *            <tt>Collection.add</tt> method).
     *
     * @throws UnsupportedOperationException if the <tt>add</tt> method is not
     *        supported by this list.
     * @throws ClassCastException if the class of the specified element
     *        prevents it from being added to this list.
     * @throws NullPointerException if the specified element is null and this
     *           list does not support null elements.
     * @throws IllegalArgumentException if some aspect of this element
     *            prevents it from being added to this list.
     */
    public boolean add(E value) {
        final int initialSize = this.size();
        this.add(this.size(), value);
        return this.size() != initialSize;
    }

    /**
     * Removes the first occurrence in this list of the specified element
     * (optional operation).  If this list does not contain the element, it is
     * unchanged.  More formally, removes the element with the lowest index i
     * such that <tt>(o==null ? get(i)==null : o.equals(get(i)))</tt> (if
     * such an element exists).
     *
     * @param toRemove element to be removed from this list, if present.
     * @return <tt>true</tt> if this list contained the specified element.
     * @throws ClassCastException if the type of the specified element
     *            is incompatible with this list (optional).
     * @throws NullPointerException if the specified element is null and this
     *            list does not support null elements (optional).
     * @throws UnsupportedOperationException if the <tt>remove</tt> method is
     *        not supported by this list.
     */
    public boolean remove(Object toRemove) {
        int index = indexOf(toRemove);
        if(index == -1) return false;
        this.remove(index);
        return true;
    }

    /**
     * Returns <tt>true</tt> if this list contains all of the elements of the
     * specified collection.
     *
     * @param  values collection to be checked for containment in this list.
     * @return <tt>true</tt> if this list contains all of the elements of the
     *         specified collection.
     * @throws ClassCastException if the types of one or more elements
     *         in the specified collection are incompatible with this
     *         list (optional).
     * @throws NullPointerException if the specified collection contains one
     *         or more null elements and this list does not support null
     *         elements (optional).
     * @throws NullPointerException if the specified collection is
     *         <tt>null</tt>.
     * @see #contains(Object)
     */
    public boolean containsAll(Collection<?> values) {
        // look for something that is missing
        for(Iterator i = values.iterator(); i.hasNext(); ) {
            Object a = i.next();
            if(!contains(a)) return false;
        }
        // contained everything we looked for
        return true;
    }

    /**
     * Appends all of the elements in the specified collection to the end of
     * this list, in the order that they are returned by the specified
     * collection's iterator (optional operation).  The behavior of this
     * operation is unspecified if the specified collection is modified while
     * the operation is in progress.  (Note that this will occur if the
     * specified collection is this list, and it's nonempty.)
     *
     * @param values collection whose elements are to be added to this list.
     * @return <tt>true</tt> if this list changed as a result of the call.
     *
     * @throws UnsupportedOperationException if the <tt>addAll</tt> method is
     *         not supported by this list.
     * @throws ClassCastException if the class of an element in the specified
     *         collection prevents it from being added to this list.
     * @throws NullPointerException if the specified collection contains one
     *         or more null elements and this list does not support null
     *         elements, or if the specified collection is <tt>null</tt>.
     * @throws IllegalArgumentException if some aspect of an element in the
     *         specified collection prevents it from being added to this
     *         list.
     * @see #add(Object)
     */
    public boolean addAll(Collection<? extends E> values) {
        return addAll(size(), values);
    }

    /**
     * Inserts all of the elements in the specified collection into this
     * list at the specified position (optional operation).  Shifts the
     * element currently at that position (if any) and any subsequent
     * elements to the right (increases their indices).  The new elements
     * will appear in this list in the order that they are returned by the
     * specified collection's iterator.  The behavior of this operation is
     * unspecified if the specified collection is modified while the
     * operation is in progress.  (Note that this will occur if the specified
     * collection is this list, and it's nonempty.)
     *
     * @param index index at which to insert first element from the specified
     *              collection.
     * @param values elements to be inserted into this list.
     * @return <tt>true</tt> if this list changed as a result of the call.
     *
     * @throws UnsupportedOperationException if the <tt>addAll</tt> method is
     *        not supported by this list.
     * @throws ClassCastException if the class of one of elements of the
     *        specified collection prevents it from being added to this
     *        list.
     * @throws NullPointerException if the specified collection contains one
     *           or more null elements and this list does not support null
     *           elements, or if the specified collection is <tt>null</tt>.
     * @throws IllegalArgumentException if some aspect of one of elements of
     *        the specified collection prevents it from being added to
     *        this list.
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *        &lt; 0 || index &gt; size()).
     */
    public boolean addAll(int index, Collection<? extends E> values) {
        // don't do an add of an empty set
        if(index < 0 || index > size()) throw new IndexOutOfBoundsException("Cannot add at " + index + " on list of size " + size());
        if(values.size() == 0) return false;

        final int initializeSize = this.size();

        for (Iterator<? extends E> iter = values.iterator(); iter.hasNext();) {
            this.add(index, iter.next());

            // advance the insertion location if its within the size of the list
            if (index < this.size())
                index++;
        }

        return this.size() != initializeSize;
    }

    /**
     * Removes from this list all the elements that are contained in the
     * specified collection (optional operation).
     *
     * @param values collection that defines which elements will be removed from
     *          this list.
     * @return <tt>true</tt> if this list changed as a result of the call.
     *
     * @throws UnsupportedOperationException if the <tt>removeAll</tt> method
     *        is not supported by this list.
     * @throws ClassCastException if the types of one or more elements
     *            in this list are incompatible with the specified
     *            collection (optional).
     * @throws NullPointerException if this list contains one or more
     *            null elements and the specified collection does not support
     *            null elements (optional).
     * @throws NullPointerException if the specified collection is
     *            <tt>null</tt>.
     * @see #remove(Object)
     * @see #contains(Object)
     */
    public boolean removeAll(Collection<?> values) {
        boolean changed = false;
        for(Iterator i = iterator(); i.hasNext(); ) {
            if(values.contains(i.next())) {
                i.remove();
                changed = true;
            }
        }
        return changed;
    }


    /**
     * Retains only the elements in this list that are contained in the
     * specified collection (optional operation).  In other words, removes
     * from this list all the elements that are not contained in the specified
     * collection.
     *
     * @param values collection that defines which elements this set will retain.
     *
     * @return <tt>true</tt> if this list changed as a result of the call.
     *
     * @throws UnsupportedOperationException if the <tt>retainAll</tt> method
     *        is not supported by this list.
     * @throws ClassCastException if the types of one or more elements
     *            in this list are incompatible with the specified
     *            collection (optional).
     * @throws NullPointerException if this list contains one or more
     *            null elements and the specified collection does not support
     *            null elements (optional).
     * @throws NullPointerException if the specified collection is
     *         <tt>null</tt>.
     * @see #remove(Object)
     * @see #contains(Object)
     */
    public boolean retainAll(Collection<?> values) {
        boolean changed = false;
        for(Iterator i = iterator(); i.hasNext();) {
            if(!values.contains(i.next())) {
                i.remove();
                changed = true;
            }
        }
        return changed;
    }

    /**
     * Removes all of the elements from this list (optional operation).  This
     * list will be empty after this call returns (unless it throws an
     * exception).
     *
     * @throws UnsupportedOperationException if the <tt>clear</tt> method is
     *        not supported by this list.
     */
    public void clear() {
        for(Iterator i = iterator(); i.hasNext();) {
            i.next();
            i.remove();
        }
    }

    /**
     * Compares the specified object with this list for equality.  Returns
     * <tt>true</tt> if and only if the specified object is also a list, both
     * lists have the same size, and all corresponding pairs of elements in
     * the two lists are <i>equal</i>.  (Two elements <tt>e1</tt> and
     * <tt>e2</tt> are <i>equal</i> if <tt>(e1==null ? e2==null :
     * e1.equals(e2))</tt>.)  In other words, two lists are defined to be
     * equal if they contain the same elements in the same order.  This
     * definition ensures that the equals method works properly across
     * different implementations of the <tt>List</tt> interface.
     *
     * @param object the object to be compared for equality with this list.
     * @return <tt>true</tt> if the specified object is equal to this list.
     */
    @Override
    public boolean equals(Object object) {
        if(object == this) return true;
        if(object == null) return false;
        if(!(object instanceof List)) return false;

        // ensure the lists are the same size
        List otherList = (List)object;
        if(otherList.size() != size()) return false;

        // compare element wise, via iterators
        Iterator iterA = iterator();
        Iterator iterB = otherList.iterator();
        while(iterA.hasNext() && iterB.hasNext()) {
            if(!GlazedListsImpl.equal(iterA.next(), iterB.next())) return false;
        }

        // if we haven't failed yet, they match
        return true;
    }

    /**
     * Returns the hash code value for this list.  The hash code of a list
     * is defined to be the result of the following calculation:
     * <pre>
     *  hashCode = 1;
     *  Iterator i = list.iterator();
     *  while (i.hasNext()) {
     *      Object obj = i.next();
     *      hashCode = 31*hashCode + (obj==null ? 0 : obj.hashCode());
     *  }
     * </pre>
     * This ensures that <tt>list1.equals(list2)</tt> implies that
     * <tt>list1.hashCode()==list2.hashCode()</tt> for any two lists,
     * <tt>list1</tt> and <tt>list2</tt>, as required by the general
     * contract of <tt>Object.hashCode</tt>.
     *
     * @return the hash code value for this list.
     * @see Object#hashCode()
     * @see Object#equals(Object)
     * @see #equals(Object)
     */
    @Override
    public int hashCode() {
        int hashCode = 1;
        for(Iterator<E> i = iterator(); i.hasNext(); ) {
            E a = i.next();
            hashCode = 31 * hashCode + (a == null ? 0 : a.hashCode());
        }
        return hashCode;
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index index of element to return.
     * @return the element at the specified position in this list.
     *
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *        &lt; 0 || index &gt;= size()).
     */
    public abstract E get(int index);

    /**
     * Replaces the element at the specified position in this list with the
     * specified element (optional operation).
     *
     * @param index index of element to replace.
     * @param value element to be stored at the specified position.
     * @return the element previously at the specified position.
     *
     * @throws UnsupportedOperationException if the <tt>set</tt> method is not
     *        supported by this list.
     * @throws    ClassCastException if the class of the specified element
     *        prevents it from being added to this list.
     * @throws    NullPointerException if the specified element is null and
     *            this list does not support null elements.
     * @throws    IllegalArgumentException if some aspect of the specified
     *        element prevents it from being added to this list.
     * @throws    IndexOutOfBoundsException if the index is out of range
     *        (index &lt; 0 || index &gt;= size()).
     */
    public E set(int index, E value) {
        throw new UnsupportedOperationException("this list does not support set()");
    }

    /**
     * Inserts the specified element at the specified position in this list
     * (optional operation).  Shifts the element currently at that position
     * (if any) and any subsequent elements to the right (adds one to their
     * indices).
     *
     * @param index index at which the specified element is to be inserted.
     * @param value element to be inserted.
     *
     * @throws UnsupportedOperationException if the <tt>add</tt> method is not
     *        supported by this list.
     * @throws    ClassCastException if the class of the specified element
     *        prevents it from being added to this list.
     * @throws    NullPointerException if the specified element is null and
     *            this list does not support null elements.
     * @throws    IllegalArgumentException if some aspect of the specified
     *        element prevents it from being added to this list.
     * @throws    IndexOutOfBoundsException if the index is out of range
     *        (index &lt; 0 || index &gt; size()).
     */
    public void add(int index, E value) {
        throw new UnsupportedOperationException("this list does not support add()");
    }

    /**
     * Removes the element at the specified position in this list (optional
     * operation).  Shifts any subsequent elements to the left (subtracts one
     * from their indices).  Returns the element that was removed from the
     * list.
     *
     * @param index the index of the element to removed.
     * @return the element previously at the specified position.
     *
     * @throws UnsupportedOperationException if the <tt>remove</tt> method is
     *        not supported by this list.
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *            &lt; 0 || index &gt;= size()).
     */
    public E remove(int index) {
        throw new UnsupportedOperationException("this list does not support remove()");
    }

    /**
     * Returns the index in this list of the first occurrence of the specified
     * element, or -1 if this list does not contain this element.
     * More formally, returns the lowest index <tt>i</tt> such that
     * <tt>(o==null ? get(i)==null : o.equals(get(i)))</tt>,
     * or -1 if there is no such index.
     *
     * @param object element to search for.
     * @return the index in this list of the first occurrence of the specified
     *         element, or -1 if this list does not contain this element.
     * @throws ClassCastException if the type of the specified element
     *         is incompatible with this list (optional).
     * @throws NullPointerException if the specified element is null and this
     *         list does not support null elements (optional).
     */
    public int indexOf(Object object) {
        // for through this, looking for the lucky object
        int index = 0;
        for(Iterator<E> i = iterator(); i.hasNext(); ) {
            if(GlazedListsImpl.equal(object, i.next())) return index;
            else index++;
        }
        // not found
        return -1;
    }

    /**
     * Returns the index in this list of the last occurrence of the specified
     * element, or -1 if this list does not contain this element.
     * More formally, returns the highest index <tt>i</tt> such that
     * <tt>(o==null ? get(i)==null : o.equals(get(i)))</tt>,
     * or -1 if there is no such index.
     *
     * @param object element to search for.
     * @return the index in this list of the last occurrence of the specified
     *         element, or -1 if this list does not contain this element.
     * @throws ClassCastException if the type of the specified element
     *         is incompatible with this list (optional).
     * @throws NullPointerException if the specified element is null and this
     *         list does not support null elements (optional).
     */
    public int lastIndexOf(Object object) {
        // for through this, looking for the lucky object
        for(int i = size() - 1; i >= 0; i--) {
            if(GlazedListsImpl.equal(object, get(i))) return i;
        }
        // not found
        return -1;
    }

    /**
     * Returns a list iterator of the elements in this list (in proper
     * sequence).
     *
     * <p>Unlike the {@link ListIterator} from a regular {@link List}, the
     * {@link EventList}'s {@link ListIterator} will remain consistent even if the
     * {@link EventList} is changed externally. Note that when used concurrently, 
     * the returned {@link ListIterator} requires locking via this list's
     * {@link #getReadWriteLock() ReadWriteLock}.
     *
     * @return a list iterator of the elements in this list (in proper
     *         sequence).
     */
    public ListIterator<E> listIterator() {
        return listIterator(0);
    }

    /**
     * Returns a list iterator of the elements in this list (in proper
     * sequence), starting at the specified position in this list.  The
     * specified index indicates the first element that would be returned by
     * an initial call to the <tt>next</tt> method.  An initial call to
     * the <tt>previous</tt> method would return the element with the
     * specified index minus one.
     *
     * <p>Unlike the {@link ListIterator} from a regular {@link List}, the
     * {@link EventList}'s {@link ListIterator} will remain consistent even if the
     * {@link EventList} is changed externally. Note that when used concurrently, 
     * the returned {@link ListIterator} requires locking via this list's
     * {@link #getReadWriteLock() ReadWriteLock}.
     *
     * @param index index of first element to be returned from the
     *          list iterator (by a call to the <tt>next</tt> method).
     * @return a list iterator of the elements in this list (in proper
     *         sequence), starting at the specified position in this list.
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *         &lt; 0 || index &gt; size()).
     */
    public ListIterator<E> listIterator(int index) {
        return new EventListIterator<E>(this, index);
    }

    /**
     * Returns a view of the portion of this list between the specified
     * <tt>fromIndex</tt>, inclusive, and <tt>toIndex</tt>, exclusive.  (If
     * <tt>fromIndex</tt> and <tt>toIndex</tt> are equal, the returned list is
     * empty.)  

     * <p>Unlike the standard {@link List#subList(int,int) List.subList()}
     * method, the {@link List} returned by this method will continue to be 
     * consistent even if the {@link EventList} it views is modified, 
     * structurally or otherwise. The returned {@link List} can always be safely 
     * cast to {@link EventList}. Note that when used concurrently, the returned
     * {@link List} requires locking via this list's
     * {@link #getReadWriteLock() ReadWriteLock}.
     *
     * <p>This method eliminates the need for explicit range operations (of
     * the sort that commonly exist for arrays).   Any operation that expects
     * a list can be used as a range operation by passing a subList view
     * instead of a whole list.  For example, the following idiom
     * removes a range of elements from a list:
     * <pre>
     *      list.subList(from, to).clear();
     * </pre>
     * Similar idioms may be constructed for <tt>indexOf</tt> and
     * <tt>lastIndexOf</tt>, and all of the algorithms in the
     * <tt>Collections</tt> class can be applied to a subList.
     *
     * @param fromIndex low endpoint (inclusive) of the subList.
     * @param toIndex high endpoint (exclusive) of the subList.
     * @return a view of the specified range within this list.
     *
     * @throws IndexOutOfBoundsException for an illegal endpoint index value
     *     (fromIndex &lt; 0 || toIndex &gt; size || fromIndex &gt; toIndex).
     */
    public List<E> subList(int fromIndex, int toIndex) {
        return new SubEventList<E>(this, fromIndex, toIndex, true);
    }

    /**
     * Returns a string representation of this collection.  The string
     * representation consists of a list of the collection's elements in the
     * order they are returned by its iterator, enclosed in square brackets
     * (<tt>"[]"</tt>).  Adjacent elements are separated by the characters
     * <tt>", "</tt> (comma and space).  Elements are converted to strings as
     * by <tt>String.valueOf(Object)</tt>.
     *
     * <p>This implementation creates an empty string buffer, appends a left
     * square bracket, and iterates over the collection appending the string
     * representation of each element in turn.  After appending each element
     * except the last, the string <tt>", "</tt> is appended.  Finally a right
     * bracket is appended.  A string is obtained from the string buffer, and
     * returned.
     *
     * @return a string representation of this collection.
     */
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("[");
        for(Iterator i = iterator(); i.hasNext(); ) {
            result.append(String.valueOf(i.next()));
            if(i.hasNext()) result.append(", ");
        }
        result.append("]");
        return result.toString();
    }
}