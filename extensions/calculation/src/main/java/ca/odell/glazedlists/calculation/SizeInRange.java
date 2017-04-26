/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.calculation;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

/**
 * Reports <tt>true</tt> when the size of the backing EventList is inside
 * the specified range; <tt>false</tt> otherwise.
 *
 * The range is inclusive - [1,2] will match either 1 or 2.
 *
 * @author James Lemieux
 */
final class SizeInRange extends AbstractCalculation<Boolean> implements ListEventListener {

    private final EventList source;
    private final int min;
    private final int max;

    /**
     * @param source the List whose size is verified against <code>min</code> and <code>max</code>
     * @param min the lower end of the range, inclusive
     * @param max the maximum end of the range, inclusive
     */
    public SizeInRange(EventList source, int min, int max) {
        super(new Boolean(source.size() >= min && source.size() <= max));

        if (min > max)
            throw new IllegalArgumentException("min must be less than max");

        this.source = source;
        this.source.addListEventListener(this);

        this.min = min;
        this.max = max;
    }

    /** @inheritDoc */
    @Override
    public void dispose() {
        this.source.removeListEventListener(this);
    }

    /** @inheritDoc */
    @Override
    public void listChanged(ListEvent listChanges) {
        final Boolean oldValue = getValue();
        final int size = listChanges.getSourceList().size();
        setValue(new Boolean(size >= min && size <= max));
        final Boolean newValue = getValue();
        fireValueChange(oldValue, newValue);
    }
}