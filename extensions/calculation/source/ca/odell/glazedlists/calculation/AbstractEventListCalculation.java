/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.calculation;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * The transition between an EventList and a single calculated value based on
 * information from each element of the EventList happens in subclasses of this
 * class. To ease the job of maintaining the value of the Calculation when the
 * backing EventList changes, this abstract class provides three abstract
 * methods which must be implemented:
 *
 * <ul>
 *   <li>{@link #inserted} updates the value of this Calculation to include the inserted element</li>
 *   <li>{@link #deleted} updates the value of this Calculation to exclude the deleted element</li>
 *   <li>{@link #updated} updates the value of this Calculation to include the replacement element and exclude the prior element</li>
 * </ul>
 *
 * @author James Lemieux
 */
public abstract class AbstractEventListCalculation<N, E> extends AbstractCalculation<N> implements ListEventListener<E> {

    /** the List of elements from which this calculation is derived */
    private final EventList<E> source;

    /** a snapshot of the {@link #source} after the last ListEvent; used to retrieve deleted elements */
    private final List<E> snapshot;

    /**
     * @param initialValue the value that should immediately be reported as the
     *      value of this Calculation
     * @param source the List of elements from which this calculation is derived
     */
    protected AbstractEventListCalculation(N initialValue, EventList<E> source) {
        super(initialValue);

        this.source = source;
        this.snapshot = new ArrayList<E>(source);

        // compute the first value of this Calculation by simulating the entry
        // of all existing elements
        for (E element : this.snapshot)
            inserted(element);

        // begin listening to the source for changes
        this.source.addListEventListener(this);
    }

    /**
     * Releases the resources consumed by this {@link AbstractEventListCalculation}
     * so that it may eventually be garbage collected.
     *
     * <p>An {@link AbstractEventListCalculation} will be garbage collected
     * without a call to {@link #dispose()}, but not before its source
     * {@link EventList} is garbage collected. By calling {@link #dispose()},
     * you allow the {@link AbstractEventListCalculation} to be garbage
     * collected before its source {@link EventList}. This is  necessary for
     * situations where an {@link AbstractEventListCalculation} is short-lived
     * but its source {@link EventList} is long-lived.
     */
    public void dispose() {
        this.source.removeListEventListener(this);
    }

    /**
     * Updates the value of this Calculation to include the information from
     * the <code>newElement</code>.
     *
     * @param newElement the new element within the EventList
     */
    protected abstract void inserted(E newElement);

    /**
     * Updates the value of this Calculation to exclude the information from
     * the <code>oldElement</code>.
     *
     * @param oldElement the old element within the EventList
     */
    protected abstract void deleted(E oldElement);

    /**
     * Updates the value of this Calculation to exclude the information from
     * the <code>oldElement</code> and include the information from the
     * <code>newElement</code>.
     *
     * @param oldElement the old element within the EventList
     * @param newElement the new element which replaced the oldElement
     */
    protected abstract void updated(E oldElement, E newElement);

    /**
     * Updates the value of this Calculation in response to the
     * <code>listChanges</code>.
     *
     * @param listChanges describes the changes to the backing EventList
     */
    public void listChanged(ListEvent<E> listChanges) {
        // store the value for later when we fire a PropertyChangeEvent
        final N oldValue = getValue();

        final List<E> source = listChanges.getSourceList();

        // update our snapshot and update the value of this Calculation by
        // delegating to the abstract methods which provide that updating logic
        while (listChanges.next()) {
            final int index = listChanges.getIndex();

            switch (listChanges.getType()) {
                case ListEvent.INSERT: {
                    final E element = source.get(index);
                    snapshot.add(index, element);
                    inserted(element);
                    break;
                }

                case ListEvent.DELETE: {
                    deleted(snapshot.remove(index));
                    break;
                }

                case ListEvent.UPDATE: {
                    final E newElement = source.get(index);
                    final E oldElement = snapshot.set(index, newElement);
                    updated(oldElement, newElement);
                    break;
                }
            }
        }

        // fetch the new value of this Calculation and try to fire an event
        final N newValue = getValue();
        fireValueChange(oldValue, newValue);
    }
}