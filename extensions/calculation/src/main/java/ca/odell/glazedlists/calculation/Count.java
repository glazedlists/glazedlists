/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.calculation;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

/**
 * Reports the size of the backing EventList as the value of this Calculation.
 *
 * @author James Lemieux
 */
final class Count extends AbstractCalculation<Integer> implements ListEventListener {

    private final EventList source;

    /**
     * @param source the List whose size is reported as the value of this
     *      Calculation
     */
    public Count(EventList source) {
        super(new Integer(source.size()));

        this.source = source;
        this.source.addListEventListener(this);
    }

    /** @inheritDoc */
    @Override
    public void dispose() {
        this.source.removeListEventListener(this);
    }

    /** @inheritDoc */
    @Override
    public void listChanged(ListEvent listChanges) {
        final Integer oldValue = getValue();
        setValue(new Integer(listChanges.getSourceList().size()));
        final Integer newValue = getValue();
        fireValueChange(oldValue, newValue);
    }
}