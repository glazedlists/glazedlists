/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// the Glazed Lists' change objects
import ca.odell.glazedlists.event.ListEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class is
 * thread ready but not thread safe. See {@link EventList} for an example
 * of thread safe code.
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="TableHeadingColor"><td colspan=2><font size="+2"><b>EventList Overview</b></font></td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Writable:</b></td><td>yes</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Concurrency:</b></td><td>thread ready, not thread safe</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Performance:</b></td><td>reads: O(1), writes O(1) amortized</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Memory:</b></td><td></td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Unit Tests:</b></td><td>FunctionList</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Issues:</b></td><td>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=282">282</a>
 * </td></tr>
 * </table>
 */
public final class FunctionList<S, E> extends TransformedList<S, E> {

    private final List<S> sourceElements;

    /** A list of the Objects produced by running the source elements through the {@link forward} Function. */
    private final List<E> mappedElements;

    /** The Function that maps source elements to FunctionList elements. */
    private final AdvancedFunction<S,E> forward;

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
     * @param reverse the function to map elements of FunctionList back to
     *      element values in the source list
     */
    public FunctionList(EventList<S> source, Function<S,E> forward, Function<E,S> reverse) {
        super(source);

        if (forward == null)
            throw new IllegalArgumentException("forward Function may not be null");

        // wrap the forward function in an adapter to the AdvancedFunction interface it is isn't yet
        if (forward instanceof AdvancedFunction)
            this.forward = (AdvancedFunction<S,E>) forward;
        else
            this.forward = new AdvancedFunctionAdapter<S,E>(forward);
        this.reverse = reverse;

        // save a reference to the source elements
        this.sourceElements = new ArrayList<S>(source);

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
        return this.forward.evaluate(s);
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
        return this.forward.reevaluate(s, e);
    }

    /**
     * A convenience method to map a {@link FunctionList} element to a source
     * element using the reverse {@link Function}.
     *
     * @param e the {@link FunctionList} element to be transformed
     * @return the result of transforming the {@link FunctionList} element
     */
    private S reverse(E e) {
        if (this.reverse == null)
            throw new IllegalStateException("A reverse mapping function must be specified to support this List operation");

        return this.reverse.evaluate(e);
    }

    /**
     * Returns the {@link Function} which maps source elements to elements
     * stored within this {@link FunctionList}. The {@link Function} is
     * guaranteed to be non-null.
     */
    public Function<S,E> getForwardFunction() {
        // unwrap the forward function from an AdvancedFunctionAdapter if necessary
        if (this.forward instanceof AdvancedFunctionAdapter)
            return ((AdvancedFunctionAdapter) this.forward).getDelegate();
        else
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
                final S inserted = source.get(changeIndex);
                this.sourceElements.add(changeIndex, inserted);
                this.mappedElements.add(changeIndex, this.forward(inserted));

            } else if (changeType == ListEvent.UPDATE) {
                final S updated = source.get(changeIndex);

                this.sourceElements.set(changeIndex, updated);
                this.mappedElements.set(changeIndex, this.forward(this.get(changeIndex), updated));

            } else if (changeType == ListEvent.DELETE) {
                final S deletedSource = this.sourceElements.remove(changeIndex);
                final E deletedTransform = this.mappedElements.remove(changeIndex);

                this.forward.dispose(deletedSource, deletedTransform);
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
        final E updated = this.get(index);
        source.set(index, this.reverse(value));
        return updated;
    }

    /** {@inheritDoc} */
    public void add(int index, E value) {
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
         * Transform the given <code>sourceValue</code> into any kind of Object.
         *
         * @param sourceValue the Object to transform
         * @return the transformed version of the object
         */
        public B evaluate(A sourceValue);
    }

    /**
     * An AdvancedFunction is an extension of the simple Function interface
     * which provides more hooks in the lifecycle of the transformation of a
     * source element. Specifically, it includes:
     *
     * <ul>
     *   <li> {@link #reevaluate} which is called when an element is mutated
     *        in place and thus run through this mapping function again. It
     *        provides access to the previous value returned from this function
     *        in case it is of use when remapping the same element.
     *
     *   <li> {@link #dispose} which is called when an element is removed from
     *        the FunctionList and is meant to be location that cleans up any
     *        resource the Function may have allocated. (like Listeners for
     *        example)
     * </ul>
     *
     * If neither of these extensions to FunctionList are useful, users are
     * encouraged to implement only the Function interface for their forward
     * function.
     */
    public interface AdvancedFunction<A,B> extends Function<A,B> {

        /**
         * Evaluate the <code>sourceValue</code> again to produce the
         * corresponding value in the FunctionList. The last
         * <code>transformedValue</code> is provided as a reference when
         * evaluating a <code>sourceValue</code> that has previously been
         * evaluated.
         *
         * @param sourceValue the Object to transform (again)
         * @param transformedValue the Object produced by this function the
         *      last time it evaluated <code>sourceValue</code>
         * @return the transformed version of the sourceValue
         */
        public B reevaluate(A sourceValue, B transformedValue);

        /**
         * Perform any necessary resource cleanup on the given
         * <code>sourceValue</code> and <code>transformedValue</code> as they
         * are removed from the FunctionList. For example, an installed
         * listeners
         *
         * @param sourceValue the Object that was transformed
         * @param transformedValue the Object that resulted from the last
         *      transformation
         */
        public void dispose(A sourceValue, B transformedValue);
    }

    /**
     * This class adapts an implementation of the simple {@link Function}
     * interface to the {@link AdvancedFunction} interface. This is purely to
     * ease the implementation of FunctionList since it can treat all forward
     * functions as though they are AdvancedFunctions which means less casting.
     */
    private static final class AdvancedFunctionAdapter<A,B> implements AdvancedFunction<A,B> {
        private final Function<A,B> delegate;

        /**
         * Adapt the given <code>delegate</code> to the
         * {@link AdvancedFunction} interface.
         */
        public AdvancedFunctionAdapter(Function<A,B> delegate) {
            this.delegate = delegate;
        }

        /**
         * Defers to the delegate.
         */
        public B evaluate(A sourceValue) {
            return this.delegate.evaluate(sourceValue);
        }

        /**
         * Defers to the delegate's {@link Function#evaluate} method.
         */
        public B reevaluate(A sourceValue, B transformedValue) {
            return this.evaluate(sourceValue);
        }

        public void dispose(A sourceValue, B transformedValue) {
            // do nothing
        }

        public Function getDelegate() {
            return this.delegate;
        }
    }
}