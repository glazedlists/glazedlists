/* Glazed Lists                                                 (c) 2003-2014 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.Filterator;

/**
 * A MatcherEditor that produces Matchers which match Objects if they lie
 * within a range of {@link Comparable}s.  This {@link RangeMatcherEditor} is
 * not coupled with any UI component that allows the user to edit the range.
 * That job is left to subclasses. This MatcherEditor is fully concrete, and
 * may be used directly by headless applications.
 *
 * <p>The {@link RangeMatcherEditor} requires that either a {@link Filterator}
 * appropriate for extracting {@link Comparable} objects be specified in its
 * constructor, or that every Object to be matched is a {@link Comparable}.
 *
 * @author James Lemieux
 */
public class RangeMatcherEditor<D extends Comparable, E> extends AbstractMatcherEditor<E> {

    /** the filterator is used as an alternative to implementing the TextFilterable interface */
    private final Filterator<D,E> filterator;

    /** the Comparable object which starts the current range; null indicates there is no start */
    private D currentRangeStart;
    /** the Comparable object which ends the current range; null indicates there is no end */
    private D currentRangeEnd;

    /**
     * Creates a {@link RangeMatcherEditor} whose Matchers can test only elements which
     * are {@link Comparable} objects.
     *
     * <p>The {@link Matcher}s from this {@link MatcherEditor} will throw a
     * {@link ClassCastException} when {@link Matcher#matches} is called with
     * an Object that is not a {@link Comparable}.
     */
    public RangeMatcherEditor() {
        this(null);
    }

    /**
     * Creates a {@link RangeMatcherEditor} that matches Objects using the
     * specified {@link Filterator} to get the {@link Comparable}s to search.
     *
     * @param filterator the object that will extract filter Comparables from
     *      each object in the <code>source</code>; <code>null</code> indicates
     *      the list elements are Comparables
     */
    public RangeMatcherEditor(Filterator<D,E> filterator) {
        this.filterator = filterator;
    }

    /**
     * Get the filterator used to extract Comparables from the matched elements.
     */
    public Filterator<D,E> getFilterator() {
        return filterator;
    }

    /**
     * This method is used to change the range currently matched by this
     * MatcherEditor. When a change to the range is detected, users of this
     * class are expected to call this method with the new bounds of the
     * range to be matched.
     *
     * <p><code>null</code> values for either <code>newStart</code> or
     * <code>newEnd</code> indicate there is no start of end to the range
     * respectively. Consequently, calling <code>setRange(null, null)</code>
     * causes this matcher editor match all values it filters.
     *
     * <p>Note: if <code>newStart</code> and <code>newEnd</code> are out of
     * their natural order with respect to each other, their values are swapped.
     * For example, <code>setRange(Jan 1, 2006, Jan 1, 1955)</code> would swap
     * the values so <code>newStart</code> is <code>Jan 1, 1955</code> and
     * <code>newEnd</code> is <code>Jan 1, 2006</code>.
     *
     * @param newStart the new value marking the start of the range;
     *      <code>null</code> indicates there is no start
     * @param newEnd the new value marking the start of the range;
     *      <code>null</code> indicates there is no start
     */
    public void setRange(D newStart, D newEnd) {
        // swap the newStart and newEnd if they are out of order
        if (newStart != null && newEnd != null && newStart.compareTo(newEnd) > 0) {
            final D temp = newEnd;
            newEnd = newStart;
            newStart = temp;
        }

        try {
            // detect the special case of no range, (which matches all elements)
            if (newStart == null && newEnd == null) {
                if (currentRangeStart != null || currentRangeEnd != null) {
                    fireMatchAll();
                }
                return;
            }

            // determine if the change in the range relaxes or constrains the previous range
            // (if it does both, it is treated as a generic change)
            boolean isRelaxed = false;
            boolean isConstrained = false;

            // determine the type of change that occurred at the start of the range
            int newStartVsOldStart = compare(newStart, currentRangeStart, true);
            isRelaxed |= newStartVsOldStart < 0;
            isConstrained |= newStartVsOldStart > 0;

            // determine the type of change that occurred at the end of the range
            int newEndVsOldEnd = compare(newEnd, currentRangeEnd, false);
            isRelaxed |= newEndVsOldEnd > 0;
            isConstrained |= newEndVsOldEnd < 0;

            // construct a matcher describing the new range
            final Matcher<E> matcher = Matchers.rangeMatcher(newStart, newEnd, filterator);

            // fire the appropriate matcher event
            if (isRelaxed && isConstrained) {
                fireChanged(matcher);
            } else if (isRelaxed) {
                fireRelaxed(matcher);
            } else if (isConstrained) {
                fireConstrained(matcher);
            }

        } finally {
            currentRangeStart = newStart;
            currentRangeEnd = newEnd;
        }
    }

    /**
     * Compare the specified two values, treating null as either before
     * all other values or after all other values.
     */
    private static int compare(Comparable a, Comparable b, boolean nullsBeforeAll) {
        if(a == null && b == null) {
            return 0;
        } else if(a == null) {
            return nullsBeforeAll ? -1 : 1;
        } else if(b == null) {
            return nullsBeforeAll ? 1 : -1;
        } else {
            return a.compareTo(b);
        }
    }
}