/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// the Glazed Lists' change objects
import ca.odell.glazedlists.event.ListEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * This List is meant to simplify the task of transforming each element of a
 * source list to an element stored at the same index in this FunctionList.
 * The logic of precisely how to tranform the source elements is contained
 * within a {@link Function} that must be supplied at the time of construction
 * and can never be changed.
 *
 * <p>An optional reverse {@link Function} which is capable of mapping the
 * elements of this FunctionList back to the corresponding source element may
 * be supplied in order to use the following methods:
 *
 * <ul>
 *   <li> {@link #add(Object)}
 *   <li> {@link #add(int, Object)}
 *   <li> {@link #set(int, Object)}
 * </ul>
 *
 * If the reverse {@link Function} is not supplied then callers of those
 * methods will receive an {@link IllegalStateException} explaining that those
 * operations are not available without the reverse {@link Function}.
 *
 * <p>If specified, the reverse {@link Function} must maintain the invariant:
 *
 * <p> <strong>o.equals(reverseFunction.evaluate(forwardFunction.evaluate(o)))</strong>
 * for any o that is non-null.
 *
 * <p><strong>Note:</strong> if two source elements share the same identity
 * (i.e. source.get(i) == source.get(j) when i != j), it is up to author of the
 * {@link Function} to decide <stong>if</strong> and <stong>how</strong> to
 * preserve the relationship of their identities after their transformation.
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="tableheadingcolor"><td colspan=2><font size="+2"><b>EventList Overview</b></font></td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Writable:</b></td><td>yes</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Concurrency:</b></td><td>thread ready, not thread safe</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Performance:</b></td><td>reads: O(1), writes O(1) amortized</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Memory:</b></td><td></td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Unit Tests:</b></td><td>FunctionList</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Issues:</b></td><td>N/A</td></tr>
 * </table>
 */
public final class FunctionList<E,S> extends TransformedList<E,S> {

    /** A list of the Objects produced by running the source elements through the {@link forward} Function. */
    private final List<E> mappedElements;

    /** The Function that maps source elements to FunctionList elements. */
    private final Function<S,E> forward;

    /** The Function that maps FunctionList elements back to source elements. It may be null. */
    private final Function<E,S> reverse;

    /**
     * Construct a {@link FunctionList} which stores the result of transforming
     * each source element using the given forward {@link Function}. No reverse
     * {@link Function} will be specified implying that {@link #add(Object)},
     * {@link #add(int, Object)} and {@link #set(int, Object)} will throw
     * {@link IllegalArgumentException} if they are called.
     *
     * @param source the EventList to decorate with a function transformation
     * @param forward the function to execute on each source element
     */
    public FunctionList(EventList<S> source, Function<S,E> forward) {
        this(source, forward, null);
    }

    /**
     * Construct a {@link FunctionList} which stores the result of transforming
     * each source element using the given forward {@link Function}. If the
     * reverse {@link Function} is not null, {@link #add(Object)},
     * {@link #add(int, Object)} and {@link #set(int, Object)} will execute
     * correctly.
     *
     * @param source the EventList to decorate with a function transformation
     * @param forward the function to execute on each source element
     * @param forward the function to map elements of FunctionList back to
     *      element values in the source list
     */
    public FunctionList(EventList<S> source, Function<S,E> forward, Function<E,S> reverse) {
        super(source);

        if (forward == null)
            throw new IllegalArgumentException("forward Function may not be null");

        this.forward = forward;
        this.reverse = reverse;

        // map all of the elements within source
        this.mappedElements = new ArrayList<E>(source.size());
        for (Iterator<S> iter = source.iterator(); iter.hasNext();) {
            this.mappedElements.add(this.forward(iter.next()));
        }

        source.addListEventListener(this);
    }

    /**
     * A convenience method to map a source element to a {@link FunctionList}
     * element using the forward {@link Function}.
     *
     * @param s the source element to be transformed
     * @return the result of transforming the source element
     */
    private E forward(S s) {
        return this.getForwardFunction().evaluate(s);
    }

    /**
     * A convenience method to map a {@link FunctionList} element to a source
     * element using the reverse {@link Function}.
     *
     * @param e the {@link FunctionList} element to be transformed
     * @return the result of transforming the {@link FunctionList} element
     */
    private S reverse(E e) {
        return this.getReverseFunction().evaluate(e);
    }

    /**
     * Returns the {@link Function} which maps source elements to elements
     * stored within this {@link FunctionList}. The {@link Function} is
     * guaranteed to be non-null.
     */
    public Function<S,E> getForwardFunction() {
        return this.forward;
    }

    /**
     * Returns the {@link Function} which maps elements stored within this
     * {@link FunctionList} to elements within the source list or
     * <code>null</code> if no such {@link Function} was specified at
     * construction.
     */
    public Function<E,S> getReverseFunction() {
        return this.reverse;
    }

    /** {@inheritDoc} */
    protected boolean isWritable() {
        return true;
    }

    /** {@inheritDoc} */
    public void listChanged(ListEvent<S> listChanges) {
        while (listChanges.next()) {
            final int changeIndex = listChanges.getIndex();
            final int changeType = listChanges.getType();

            if (changeType == ListEvent.INSERT) {
                this.mappedElements.add(changeIndex, this.forward(source.get(changeIndex)));

            } else if (changeType == ListEvent.UPDATE) {
                this.mappedElements.set(changeIndex, this.forward(source.get(changeIndex)));

            } else if (changeType == ListEvent.DELETE) {
                this.mappedElements.remove(changeIndex);
            }
        }

        listChanges.reset();
        updates.forwardEvent(listChanges);
    }

    /** {@inheritDoc} */
    public E get(int index) {
        return this.mappedElements.get(index);
    }

    /** {@inheritDoc} */
    public E remove(int index) {
        final E removed = this.get(index);
        source.remove(index);
        return removed;
    }

    /** {@inheritDoc} */
    public E set(int index, E value) {
        if (this.reverse == null)
            throw new IllegalStateException("A reverse mapping function must be specified to support this List operation");

        final E updated = this.get(index);
        source.set(index, this.reverse(value));
        return updated;
    }

    /** {@inheritDoc} */
    public boolean add(E value) {
        this.add(this.size(), value);
        return true;
    }

    /** {@inheritDoc} */
    public void add(int index, E value) {
        if (this.reverse == null)
            throw new IllegalStateException("A reverse mapping function must be specified to support this List operation");

        source.add(index, this.reverse(value));
    }

    /**
     * A Function encapsulates the logic for transforming a list element into
     * any kind of Object. Implementations should typically create and return
     * new objects, though it is permissible to return the original value
     * unchanged (i.e. the Identity Function). It is, however, typically not a
     * good idea to mutate and return the given value.
     */
    public interface Function<A,B> {

        /**
         * Transform the given <code>value</code> into any kind of Object.
         *
         * @param value the Object to transform
         * @return the transformed version of the object
         */
        public B evaluate(A value);
    }
}