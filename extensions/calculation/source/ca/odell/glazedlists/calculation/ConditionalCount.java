/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.calculation;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

/**
 * Reports the number of elements from a given EventList that satisfy a given
 * Matcher as the value of this Calculation.
 *
 * @author James Lemieux
 */
final class ConditionalCount<E> extends AbstractCalculation<Integer> implements ListEventListener<E> {

    private final FilterList<E> filtered;

    /**
     * @param source the raw elements that will be tested by the given
     *      <code>matcher</code>
     * @param matcher the logic which determines whether an element is counted
     *      in the value reported by this Calculation
     */
    public ConditionalCount(EventList<E> source, Matcher<E> matcher) {
        super(new Integer(0));

        // the user should ideally know nothing about this FilterList, so we
        // lock the pipeline during its construction so the calling code need
        // not consider concurrency, which is really an implementation detail
        source.getReadWriteLock().readLock().lock();
        try {
            filtered = new FilterList<E>(source, matcher);
            filtered.addListEventListener(this);

            setValue(new Integer(filtered.size()));
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /** @inheritDoc */
    @Override
    public void dispose() {
        filtered.removeListEventListener(this);
        filtered.dispose();
    }

    /** @inheritDoc */
    @Override
    public void listChanged(ListEvent<E> listChanges) {
        final Integer oldValue = getValue();
        setValue(new Integer(listChanges.getSourceList().size()));
        final Integer newValue = getValue();
        fireValueChange(oldValue, newValue);
    }
}