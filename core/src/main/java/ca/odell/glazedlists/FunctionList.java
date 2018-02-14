/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventAssembler;

import java.util.ArrayList;
import java.util.List;
import java.util.RandomAccess;
import java.util.function.Predicate;

/**
 * This List is meant to simplify the task of transforming each element of a
 * source list to an element stored at the same index in this FunctionList.
 * The logic of precisely how to transform the source elements is contained
 * within a {@link Function} that must be supplied at the time of construction
 * but can be changed afterward using {@link #setForwardFunction}. This
 * {@link Function} is called the forward function because it creates elements
 * in this {@link FunctionList} from elements that have been added or mutated
 * within the source list. The forward function may be an implementation of
 * either {@link Function} or {@link AdvancedFunction}.
 *
 * <p>An optional reverse {@link Function} which is capable of mapping the
 * elements of this FunctionList back to the corresponding source element may
 * be supplied in order to use the following mutating methods:
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
public final class FunctionList<S, E> extends TransformedList<S, E> implements RandomAccess {
    /** A sometimes-used copy of the source list. This is needed to provide proper
     *  disposal when an {@link AdvancedFunction} is used. When not needed, this list
     *  is truncated. */
    private final ArrayList<S> sourceElements;

    /** The sourceElements copy is needed for the dispose method of AdvancedFunctions.
     *  This can be omitted when using a normal function so this indicates whether or not
     *  the source copy is in use. */
    private boolean needDispose = false;

    /** A list of the Objects produced by running the source elements through the {@link #forward} Function. */
    private final List<E> mappedElements;

    /** The Function that maps source elements to FunctionList elements. */
    private AdvancedFunction<S,E> forward;

    /** The Function that maps FunctionList elements back to source elements. It may be null. */
    private Function<E,S> reverse;

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
     * as expected.
     *
     * <p> Note: an {@link AdvancedFunction} can be specified for the forward
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

        this.sourceElements = new ArrayList<>();

        updateForwardFunction(forward);
        setReverseFunction(reverse);

        // map all of the elements within source
        this.mappedElements = new ArrayList<>(source.size());
        source.forEach(s -> mappedElements.add(forward(s)));

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
        return forward.evaluate(s);
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
        return forward.reevaluate(s, e);
    }

    /**
     * A convenience method to map a {@link FunctionList} element to a source
     * element using the reverse {@link Function}.
     *
     * @param e the {@link FunctionList} element to be transformed
     * @return the result of transforming the {@link FunctionList} element
     */
    private S reverse(E e) {
        if (reverse == null)
            throw new IllegalStateException("A reverse mapping function must be specified to support this List operation");

        return reverse.evaluate(e);
    }

    /**
     * Changes the {@link Function} that evaluates source elements to produce
     * mapped elements. Calling this method with a different
     * <code>forward</code> Function will cause all elements in this
     * FunctionList to be reevaluated.
     *
     * <p>Callers of this method typically also want to update the reverse
     * function using {@link #setReverseFunction} if one exists.
     */
    public void setForwardFunction(Function<S,E> forward) {
        updateForwardFunction(forward);

        updates.beginEvent(true);

        // remap all of the elements within source
        for (int i = 0, n = source.size(); i < n; i++) {
            final E newValue = forward(source.get(i));
            final E oldValue = this.mappedElements.set(i, newValue);
            updates.elementUpdated(i, oldValue, newValue);
        }

        updates.commitEvent();
    }

    /**
     * A convenience method to run a null check on the given
     * <code>forward</code> Function and to wrap it in a delegating
     * implementation of the {@link AdvancedFunction} interface as needed.
     */
    private void updateForwardFunction(Function<S,E> forward) {
        if (forward == null)
            throw new IllegalArgumentException("forward Function may not be null");

        // wrap the forward function in an adapter to the AdvancedFunction interface if necessary
        if (forward instanceof AdvancedFunction) {
            this.forward = (AdvancedFunction<S, E>) forward;

            // If we didn't previously need disposals, the source copy will need to
            // be rebuilt.
            if (!needDispose) {
                needDispose = true;
                sourceElements.ensureCapacity(source.size());
                sourceElements.clear();
                sourceElements.addAll(source);
            }
        }
        else {
            this.forward = new AdvancedFunctionAdapter<>(forward);

            needDispose = false;
            sourceElements.clear();
            sourceElements.trimToSize();
        }
    }

    /**
     * Returns the {@link Function} which maps source elements to elements
     * stored within this {@link FunctionList}. The {@link Function} is
     * guaranteed to be non-null.
     */
    public Function<S,E> getForwardFunction() {
        // unwrap the forward function from an AdvancedFunctionAdapter if necessary
        if (forward instanceof AdvancedFunctionAdapter)
            return ((AdvancedFunctionAdapter<S,E>) forward).getDelegate();
        else
            return forward;
    }

    /**
     * Changes the {@link Function} that evaluates FunctionList elements to
     * produce the original source element with which it corresponds. The
     * reverse Function will be used in all subsequent calls to:
     *
     * <ul>
     *   <li> {@link #add(Object)}
     *   <li> {@link #add(int, Object)}
     *   <li> {@link #set(int, Object)}
     * </ul>
     *
     * This method should typically be called at the same time the forward
     * function is changed using {@link #setForwardFunction}.
     */
    public void setReverseFunction(Function<E,S> reverse) {
        this.reverse = reverse;
    }

    /**
     * Returns the {@link Function} which maps elements stored within this
     * {@link FunctionList} back to elements within the source list or
     * <code>null</code> if no such {@link Function} was specified.
     */
    public Function<E,S> getReverseFunction() {
        return reverse;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean isWritable() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void listChanged(ListEvent<S> listChanges) {
        updates.beginEvent(true);

        if (listChanges.isReordering()) {
            final int[] reorderMap = listChanges.getReorderMap();
            final List<E> originalMappedElements = new ArrayList<>(mappedElements);
            for (int i = 0; i < reorderMap.length; i++) {
                final int sourceIndex = reorderMap[i];
                mappedElements.set(i, originalMappedElements.get(sourceIndex));
            }
            updates.reorder(reorderMap);

        } else {
            while (listChanges.next()) {
                final int changeIndex = listChanges.getIndex();
                final int changeType = listChanges.getType();

                if (changeType == ListEvent.INSERT) {
                    final S newValue = source.get(changeIndex);
                    final E newValueTransformed = forward(newValue);
                    if (needDispose) sourceElements.add(changeIndex, newValue);
                    mappedElements.add(changeIndex, newValueTransformed);
                    updates.elementInserted(changeIndex, newValueTransformed);

                } else if (changeType == ListEvent.UPDATE) {
                    final E oldValueTransformed = get(changeIndex);
                    final S newValue = source.get(changeIndex);
                    final E newValueTransformed = forward(oldValueTransformed, newValue);
                    if (needDispose) sourceElements.set(changeIndex, newValue);
                    mappedElements.set(changeIndex, newValueTransformed);
                    updates.elementUpdated(changeIndex, oldValueTransformed, newValueTransformed);

                } else if (changeType == ListEvent.DELETE) {
                    // NOTE: The `listChanges.getOldValue()` method could potentially be
                    //       used here, but holding off on that for now until the
                    //       ListEvent API is solidified.
                    final E oldValueTransformed = mappedElements.remove(changeIndex);
                    if (needDispose) {
                        final S oldValue = sourceElements.remove(changeIndex);
                        forward.dispose(oldValue, oldValueTransformed);
                    }
                    updates.elementDeleted(changeIndex, oldValueTransformed);
                }
            }
        }
        updates.commitEvent();
    }

    /** {@inheritDoc} */
    @Override
    public E get(int index) {
        return mappedElements.get(index);
    }

    /** {@inheritDoc} */
    @Override
    public E remove(int index) {
        final E removed = get(index);
        source.remove(index);
        return removed;
    }

    /** {@inheritDoc} */
    @Override
    public E set(int index, E value) {
        final E updated = get(index);
        source.set(index, reverse(value));
        return updated;
    }

    /** {@inheritDoc} */
    @Override
    public void add(int index, E value) {
        source.add(index, reverse(value));
    }

    /** {@inheritDoc} */
    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        // Ideally this remove would be processed as a single transaction. The only real
        // way that can happen (efficiently) is to get access to the source
        // ListEventAssembler, which we can if the list extends AbstractEventList.
        // Otherwise, things will work fine, but there will be multiple events dispatched
        // for a single remove operation.
        ListEventAssembler<?> sourceUpdates;
        if (source instanceof AbstractEventList) {
            sourceUpdates = ((AbstractEventList) source).updates;
        }
        else sourceUpdates = null;

        boolean foundMatch = false;
        for(int i = size() - 1; i >= 0; i--) {
            if (filter.test(mappedElements.get(i))) {
                if (sourceUpdates != null && !foundMatch) {
                    sourceUpdates.beginEvent(true);
                }
                foundMatch = true;
                remove(i);
            }
        }
        if (sourceUpdates != null && foundMatch) {
            sourceUpdates.commitEvent();
        }
        return foundMatch;
    }

    /**
     * A Function encapsulates the logic for transforming a list element into
     * any kind of Object. Implementations should typically create and return
     * new objects, though it is permissible to return the original value
     * unchanged (i.e. the Identity Function).
     */
    @FunctionalInterface
    public interface Function<A,B> {

        /**
         * Transform the given <code>sourceValue</code> into any kind of Object.
         *
         * @param sourceValue the Object to transform
         * @return the transformed version of the object
         */
        B evaluate(A sourceValue);
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
     *        resources the Function may have allocated. (like Listeners for
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
        B reevaluate(A sourceValue, B transformedValue);

        /**
         * Perform any necessary resource cleanup on the given
         * <code>sourceValue</code> and <code>transformedValue</code> as they
         * are removed from the FunctionList such as installed listeners.
         *
         * @param sourceValue the Object that was transformed
         * @param transformedValue the Object that resulted from the last
         *      transformation
         */
        void dispose(A sourceValue, B transformedValue);
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
        AdvancedFunctionAdapter(Function<A, B> delegate) {
            this.delegate = delegate;
        }

        /**
         * Defers to the delegate.
         */
        @Override
        public B evaluate(A sourceValue) {
            return delegate.evaluate(sourceValue);
        }

        /**
         * Defers to the delegate's {@link Function#evaluate} method.
         */
        @Override
        public B reevaluate(A sourceValue, B transformedValue) {
            return evaluate(sourceValue);
        }

        @Override
        public void dispose(A sourceValue, B transformedValue) {
            // do nothing
        }

        public Function<A,B> getDelegate() {
            return delegate;
        }
    }
}