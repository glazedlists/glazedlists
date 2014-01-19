/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.matchers;

import ca.odell.glazedlists.Filterator;
import ca.odell.glazedlists.matchers.Matcher;

import java.util.ArrayList;
import java.util.List;

/**
 * A Matcher for matching {@link Comparable}s within a range. The list of
 * objects being filtered contains either:
 *
 * <ul>
 *   <li> Comparable objects
 *   <li> objects from which Comparable can be extracted via a {@link Filterator}
 * </ul>
 *
 * @author James Lemieux
 */
public class RangeMatcher<D extends Comparable,E> implements Matcher<E> {

    /** The start of the range; <code>null</code> if the range has no starting value. */
    private final D start;
    /** The end of the range; <code>null</code> if the range has no ending value. */
    private final D end;

    /**
     * The Filterator which can extract Comparables from the filter objects;
     * <code>null</code> implies the objects to be filtered are already
     * Comparable objects.
     */
    private final Filterator<D,E> filterator;

    /** a heavily recycled list of filter Comparables, call clear() before use */
    private final List<D> filterComparables = new ArrayList<D>();

    /**
     * This constructor should be used when the objects to be filtered are
     * already {@link Comparable} objects and thus no conversion is necessary
     * to be used with this matcher.
     *
     * @param start the start of the range to filter on; <code>null</code>
     *      indicates there is no start to the range
     * @param end the end of the range to filter on; <code>null</code>
     *      indicates there is no end to the range
     */
    public RangeMatcher(D start, D end) {
        this(start, end, null);
    }

    /**
     * This constructor should be used when the objects to be filtered are not
     * {@link Comparable} objects, but contain {@link Comparable} objects which
     * can be extracted. The given <code>filterator</code> is used to extract
     * those {@link Comparable} objects from the filtered objects.
     *
     * @param start the start of the range to filter on; <code>null</code>
     *      indicates there is no start to the range
     * @param end the end of the range to filter on; <code>null</code>
     *      indicates there is no end to the range
     * @param filterator contains the logic to fetch {@link Comparable} objects
     *      from the list of filtered objects
     */
    public RangeMatcher(D start, D end, Filterator<D,E> filterator) {
        this.start = start;
        this.end = end;
        this.filterator = filterator;
    }

    /** {@inheritDoc} */
    @Override
    public boolean matches(E item) {
        filterComparables.clear();

        if (filterator == null)
            filterComparables.add((D) item);
        else
            filterator.getFilterValues(filterComparables, item);

        // ensure the range contains at least one extracted Comparable
        for (int c = 0; c < filterComparables.size(); c++) {
            D filterComparable = filterComparables.get(c);

            // check if the filterComparable is within the defined range
            if (filterComparable != null) {
                if (start != null && start.compareTo(filterComparable) > 0)
                    continue;

                if (end != null && end.compareTo(filterComparable) < 0)
                    continue;
            }

            // a filterComparable is within the given range, so the object matches
            return true;
        }

        // no filterComparable fell within this range
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "[RangeMatcher between " + start + " and " + end + "]";
    }
}