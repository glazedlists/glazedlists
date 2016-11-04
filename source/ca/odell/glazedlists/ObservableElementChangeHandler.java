package ca.odell.glazedlists;

import ca.odell.glazedlists.ObservableElementList.Connector;

/**
 * This interface defines the contract between an {@link ObservableElementList} and an associated {@link Connector}.
 * This allows different implementations of an ObservableElementList while reusing the existing Connector
 * (implementations).
 * 
 * @param <E> list element type
 * 
 * @author Holger Brands
 */
public interface ObservableElementChangeHandler<E> {

    /**
     * Handle a listener being notified for the specified <code>listElement</code>.
     * This method causes a ListEvent to be fired from this EventList indicating
     * an update occurred at all locations of the given <code>listElement</code>.
     *
     * <p>Note that listElement must be the exact object located within this list
     * (i.e. <code>listElement == get(i) for some i >= 0</code>).
     *
     * @param listElement the list element which has been modified
     */
    void elementChanged(Object listElement);
}