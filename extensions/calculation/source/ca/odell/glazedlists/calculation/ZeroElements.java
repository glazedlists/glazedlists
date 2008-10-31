/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.calculation;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

/**
 * Reports <tt>true</tt> when the size of the backing EventList is
 * <code>0</code>; <tt>false</tt> otherwise.
 *
 * @author James Lemieux
 */
final class ZeroElements extends AbstractCalculation<Boolean> implements ListEventListener {

    private final EventList source;

    /**
     * @param source the List whose size is compared against 0
     */
    public ZeroElements(EventList source) {
        super(new Boolean(source.isEmpty()));

        this.source = source;
        this.source.addListEventListener(this);
    }

    /** @inheritDoc */
    public void dispose() {
        this.source.removeListEventListener(this);
    }

    /** @inheritDoc */
    public void listChanged(ListEvent listChanges) {
        final Boolean oldValue = getValue();
        setValue(new Boolean(listChanges.getSourceList().isEmpty()));
        final Boolean newValue = getValue();
        fireValueChange(oldValue, newValue);
    }
}