/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.calculation;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

/**
 * Returns the element at the specified position in the list. If the size of
 * the list is less than the position this Calculation returns a specified
 * default value.
 *
 * @author Kevin Day
 */
final class ElementAt<E> extends AbstractCalculation<E> implements ListEventListener<E> {

    private final EventList<E> source;
    private final int index;
    private final E defaultValue;

    /**
     * @param source the List whose element at position <code>index</code> is returned
     * @param index the position of the element in <code>source</code> to be returned
     * @param defaultValue the value of the calculation if the list does not contain <code>index+1</code> entries
     */
    public ElementAt(EventList<E> source, int index, E defaultValue) {
        super(source.size() > index ? source.get(index) : defaultValue);

        this.source = source;
        this.index = index;
        this.defaultValue = defaultValue;

        this.source.addListEventListener(this);
    }

    /** @inheritDoc */
    @Override
    public void dispose() {
        source.removeListEventListener(this);
    }

    /** @inheritDoc */
    @Override
    public void listChanged(ListEvent<E> listChanges) {
        final E oldValue = getValue();
        setValue(source.size() > index ? source.get(index) : defaultValue);
        final E newValue = getValue();
        fireValueChange(oldValue, newValue);
    }
}