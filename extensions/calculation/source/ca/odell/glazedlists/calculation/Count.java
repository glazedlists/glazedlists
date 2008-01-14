/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.calculation;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;

/**
 * Reports the size of the backing EventList as the value of this Calculation.
 *
 * @author James Lemieux
 */
public final class Count extends AbstractEventListCalculation<Object, Integer> {

    /**
     * @param source the List whose size is reported as the value of this
     *      Calculation
     */
    public Count(EventList source) {
        super(0, source);
        setValue(source.size());
    }

    /** No-op since the value of this Calculation is not based on element data. */
    protected void inserted(Object newElement) { }
    /** No-op since the value of this Calculation is not based on element data. */
    protected void deleted(Object oldElement) { }
    /** No-op since the value of this Calculation is not based on element data. */
    protected void updated(Object oldElement, Object newElement) { }

    /** @inheritDoc */
    public void listChanged(ListEvent<Object> listChanges) {
        final Integer oldValue = getValue();
        setValue(listChanges.getSourceList().size());
        final Integer newValue = getValue();
        fireValueChange(oldValue, newValue);
    }
}