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
 * and can never be changed. This {@link Function} is called the forward
 * function because it creates elements in this {@link FunctionList} from
 * elements that have been added or mutated within the source list. The forward
 * function may be an implementation of either {@link Function} or
 * {@link AdvancedFunction}.
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
 * <p>If specified, the reverse {@link Function} should do its best to
 * maintain the invariant:
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
public final class FunctionList<S, E> extends TransformedList<S, E> {

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
     * <p> Note: a {@link AdvancedFunction} can be specified for the forward
     * {@link Function} which allows the implementor a chance to examine the
     * prior value that was mapped to a source element when it must be remapped
     * due to a modification (from a call to {@link List#set}).
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
     * A convenience method to remap a source element to a {@link FunctionList}
     * element using the forward {@link Function}.
     *
     * @param e the last prior result of transforming the source element
     * @param s the source element to be transformed
     * @return the result of transforming the source element
     */
    private E forward(E e, S s) {
        final Function<S,E> forwardFunction = this.getForwardFunction();
        if (forwardFunction instanceof AdvancedFunction)
            return ((AdvancedFunction<S,E>) forwardFunction).reevaluate(e, s);
        else
            return forwardFunction.evaluate(s);
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
                this.mappedElements.set(changeIndex, this.forward(this.get(changeIndex), source.get(changeIndex)));

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
     * unchanged (i.e. the Identity Function).
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

    /**
     * A Function which defines a separate method, {@link #reevaluate}, for
     * transforming values that have been transformed in the past. This allows
     * the implementation a chance to reuse aspects of the previous value produced
     * by the AdvancedFunction. If the old value need not be considered, when
     * evaluating the forward Function, then an implementation of the simpler
     * {@link Function} interface will suffice.
     */
    public interface AdvancedFunction<A,B> extends Function<A,B> {

        /**
         * Evaluate the <code>newValue</code> newly added to the source of the
         * FunctionList to produce the corresponding value in FunctionList. The
         * <code>oldValue</code> is provided as a reference when evaluating a
         * value that has previously been evaluated (for example, to service a
         * {@link List#set} method).
         *
         * @param oldValue the Object produced by this function the last time
         *      it evaluated <code>newValue</code>
         * @param newValue the Object to transform
         * @return the transformed version of the object
         */
        public B reevaluate(B oldValue, A newValue);
    }
}