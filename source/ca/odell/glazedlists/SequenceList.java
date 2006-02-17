/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEvent;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

/**
 * A SequenceList contains values in adjacent indices which occur at predictable
 * intervals from each other. A simple SequenceList could be:
 * <pre> {-10, -5, 0, 5, 10, 15} </pre>
 *
 * while a more complicated example could be:
 * <pre> {Jun 1, Jul 1, Aug 1, Sep 1, Oct 1} </pre>
 *
 * As long as the values can be ordered via a {@link Comparator} and a
 * {@link Sequencer} can be implemented to reliably produce the next or previous
 * value in a sequence using only some value from the source list.
 *
 * <p>The start and end values of the sequence are the smallest sequence values
 * which maintain the invariant that:
 * <code>sequence start &lt;= each value in the source list &lt;= sequence end</code>
 */
public final class SequenceList<E> extends TransformedList<E,E> {

    /** The values participating in the sequence. */
    private final List<E> sequence = new ArrayList<E>();

    /** The comparator that defines the order of the source and sequence values. */
    private final Comparator<E> comparator;

    /**
     * The object containing the logic which produces next and previous
     * sequence values by inspecting any source value.
     */
    private final Sequencer<E> sequencer;

    /**
     * Constructs a SequenceList containing a sequence of values produced by
     * the <code>sequencer</code> which cover the range of values contained
     * within the <code>source</code>.
     *
     * @param source the raw values to build a sequence around
     * @param sequencer the logic to produce sequence values relative to a value
     */
    public SequenceList(EventList<E> source, Sequencer<E> sequencer) {
        this(source, sequencer, (Comparator<E>) GlazedLists.comparableComparator());
    }

    /**
     * Constructs a SequenceList containing a sequence of values produced by
     * the <code>sequencer</code> which cover the range of values contained
     * within the <code>source</code>. The given <code>comparator</code>
     * determines the order of the sequence values.
     *
     * @param source the raw values to build a sequence around
     * @param sequencer the logic to produce sequence values relative to a value
     */
    public SequenceList(EventList<E> source, Sequencer<E> sequencer, Comparator<E> comparator) {
        this(new SortedList<E>(source, comparator), sequencer, comparator);
    }

    private SequenceList(SortedList<E> source, Sequencer<E> sequencer, Comparator<E> comparator) {
        super(source);
        this.sequencer = sequencer;
        this.comparator = comparator;
        source.addListEventListener(this);
    }

    //
    // Unsupported Operations
    //

    public E set(int index, E value) {
        throw new UnsupportedOperationException();
    }

    public E remove(int index) {
        throw new UnsupportedOperationException();
    }

    //
    // Supported Operations
    //

    /** {@inheritDoc} */
    protected boolean isWritable() {
        return true;
    }

    /** {@inheritDoc} */
    // todo doc the fact that we discard the index
    public void add(int index, E value) {
        this.add(value);
    }

    /** {@inheritDoc} */
    public boolean add(E value) {
        return source.add(value);
    }

    /** {@inheritDoc} */
    public int size() {
        return this.sequence.size();
    }

    /** {@inheritDoc} */
    public E get(int index) {
        return this.sequence.get(index);
    }

    /**
     * A Sequencer defines the logic required to calculate the previous and
     * next sequence values given any value. It is important to note that the
     * arguments passed to {@link #previous} and {@link #next} will not always
     * be sequence values themselves. For example if a Sequencer is contains
     * logic to produce a sequence of numbers evenly divisible by 2, it must
     * handle returning the next and previous even number relative to
     * <strong>any</strong> integer. So the Sequencer logic must produce:
     *
     * <ul>
     *   <li><code>previous(5)</code> returns 4
     *   <li><code>previous(6)</code> returns 4
     *   <li><code>next(5)</code> returns 6
     *   <li><code>next(4)</code> returns 6
     * </ul>
     */
    public interface Sequencer<E> {
        /**
         * Given a sequencable <code>value</code>, produce the previous value
         * in the sequence such that <code>value</code> is now included in the
         * sequence.
         *
         * @param value a sequencable value
         * @return the previous value in the sequence such that <code>value</code>
         *      would be included within the bounds of the sequence
         */
        public E previous(E value);

        /**
         * Given a sequencable <code>value</code>, produce the next value
         * in the sequence such that <code>value</code> is now included in the
         * sequence.
         *
         * @param value a sequencable value
         * @return the next value in the sequence such that <code>value</code>
         *      would be included within the bounds of the sequence
         */
        public E next(E value);
    }

    /**
     * Returns <tt>true</tt> if <code>value</code> is exactly a sequence value
     * (i.e. could be stored at some index within this {@link SequenceList}.
     */
    private boolean isSequenceValue(E value) {
        final E sequencedValue = sequencer.previous(sequencer.next(value));
        return comparator.compare(value, sequencedValue) == 0;
    }

    /**
     * Returns the previous value in the sequence defined by this list or
     * <code>value</code> itself if it is a sequence value.
     *
     * @param value the value relative to which the previous sequence value is returned
     * @return the previous sequence value relative to the given <code>value</code>
     */
    public E getPreviousSequenceValue(E value) {
        // if value is already a sequence value, return it
        if (isSequenceValue(value))
            return value;

        // ask the sequencer for the previous value
        return sequencer.previous(value);
    }

    /**
     * Returns the next value in the sequence defined by this list or
     * <code>value</code> itself if it is a sequence value.
     *
     * @param value the value relative to which the next sequence value is returned
     * @return the next sequence value relative to the given <code>value</code>
     */
    public E getNextSequenceValue(E value) {
        // if value is already a sequence value, return it
        if (isSequenceValue(value))
            return value;

        // ask the sequencer for the next value
        return sequencer.next(value);
    }

    /** {@inheritDoc} */
    public void listChanged(ListEvent<E> listChanges) {
        updates.beginEvent();

        while (listChanges.next()) {
            if (listChanges.getType() == ListEvent.INSERT) {
                // determine the location of the change in the sorted source list
                final int index = listChanges.getIndex();

                // if the element wasn't added at the first or last index of the list, our sequence already includes it
                if (index != 0 && index != listChanges.getSourceList().size()-1)
                    continue;

                // fetch the value and calculate its previous and next sequence values
                final E value = listChanges.getSourceList().get(index);
                final E previousSequenceValue = getPreviousSequenceValue(value);
                final E nextSequenceValue = getNextSequenceValue(value);

                // if this is the first element added to the SequenceList, add the first two sequence values
                if (this.isEmpty()) {
                    sequence.add(previousSequenceValue);
                    sequence.add(nextSequenceValue);
                    updates.addInsert(0);
                    updates.addInsert(1);

                } else {
                    // prepend sequence values until the required first sequence value is covered
                    E firstSequenceValue = sequence.get(0);
                    while (comparator.compare(previousSequenceValue, firstSequenceValue) == -1) {
                        firstSequenceValue = sequencer.previous(firstSequenceValue);
                        sequence.add(0, firstSequenceValue);
                        updates.addInsert(0);
                    }

                    // append sequence values until the required last sequence value is covered
                    E lastSequenceValue = sequence.get(sequence.size()-1);
                    while (comparator.compare(nextSequenceValue, lastSequenceValue) == 1) {
                        lastSequenceValue = sequencer.next(lastSequenceValue);
                        sequence.add(lastSequenceValue);
                        updates.addInsert(sequence.size());
                    }
                }
            }
        }

        updates.commitEvent();
    }
}