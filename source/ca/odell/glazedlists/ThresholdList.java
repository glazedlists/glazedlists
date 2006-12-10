/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// to use standard collections
import java.util.Comparator;

/**
 * An {@link EventList} that shows a range of the elements of the source
 * {@link EventList}. Each element in the source {@link EventList} is assigned
 * an integer value via an {@link Evaluator}. This integer is used
 * to determine whether the element fits in the {@link ThresholdList}s range.
 *
 * <p>By modifying the upper and lower thresholds in the range, the list can
 * be filtered in a simple and powerful way.
 *
 * <p>The {@link ThresholdList} lends itself to use with a slider widget for
 * manipulating one of the range's endpoints.
 *
 * <p>One use case for {@link ThresholdList} is in a media player application.
 * By creating a {@link Evaluator} for a song's bitrate, the user could
 * limit results to MP3 files between 192 and 320kbps.
 *
 * <p>Note that the elements in the {@link ThresholdList} will be presented in
 * order sorted by their {@link Evaluator} value.
 *
 * <p>This {@link EventList} supports all write operations.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class
 * breaks the contract required by {@link java.util.List}. See {@link EventList}
 * for an example.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class is
 * thread ready but not thread safe. See {@link EventList} for an example
 * of thread safe code.
 * 
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="TableHeadingColor"><td colspan=2><font size="+2"><b>EventList Overview</b></font></td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Writable:</b></td><td>yes</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Concurrency:</b></td><td>thread ready, not thread safe</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Performance:</b></td><td>reads: O(log N), writes O(log N), change threshold O(log N)</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Memory:</b></td><td>72 bytes per element</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Unit Tests:</b></td><td>N/A</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Issues:</b></td><td>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=47">47</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=137">137</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=217">217</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=218">218</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=246">246</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=277">277</a>
 * </td></tr>
 * </table>
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public final class ThresholdList<E> extends RangeList<E> {

    /** the lower bound to use to define list containment */
    private int lowerThreshold = Integer.MIN_VALUE;

    /** the upper bound to use to define list containment */
    private int upperThreshold = Integer.MAX_VALUE;

    /** the evaluator to use to compare Objects against the threshold */
    private Evaluator<E> evaluator = null;

    /** a sorted view of the source makes threshold operations really fast */
    private final SortedList<E> sortedSource;

    /**
     * Creates a {@link ThresholdList} that provides range-filtering based on the
     * specified {@link EventList} based on the specified integer JavaBean property.
     */
    public ThresholdList(EventList<E> source, String propertyName) {
        this(source, (Evaluator<E>) GlazedLists.thresholdEvaluator(propertyName));
    }

    /**
     * Creates a {@link ThresholdList} that provides range-filtering on the
     * specified {@link EventList} using the specified {@link Evaluator}.
     */
    public ThresholdList(EventList<E> source, Evaluator<E> evaluator) {
        this(new SortedList<E>(source, new ThresholdComparator<E>(evaluator)), evaluator);
    }

    private ThresholdList(SortedList<E> sortedSource, Evaluator<E> evaluator) {
        super(sortedSource);
        this.sortedSource = sortedSource;
        this.evaluator = evaluator;
    }

    /**
     * Sets the lower threshold for this list to be the result of calling
     * {@link Evaluator#evaluate(Object) evaluate()} on the given object.
     *
     * <p>This list can be used programmatically rather than hooking it up to
     * a UI component. <strong>Calling this method directly while this list
     * is connected to a particular widget could result in errors.</strong>
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> This method is
     * thread ready but not thread safe. See {@link EventList} for an example
     * of thread safe code.
     */
    public void setLowerThreshold(E object) {
        setLowerThreshold(evaluator.evaluate(object));
    }

    /**
     * Sets the lower threshold for this list.
     *
     * <p>This list can be used programmatically rather than hooking it up to
     * a UI component. <strong>Calling this method directly while this list
     * is connected to a particular widget could result in errors.</strong>
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> This method is
     * thread ready but not thread safe. See {@link EventList} for an example
     * of thread safe code.
     */
    public void setLowerThreshold(int lowerThreshold) {
        this.lowerThreshold = lowerThreshold;
        adjustRange();
    }

    /**
     * Gets the lower threshold for this list
     */
    public int getLowerThreshold() {
        return lowerThreshold;
    }

    /**
     * Sets the upper threshold for this list to be the result of calling
     * {@link Evaluator#evaluate(Object) evaluate()} on the given object.
     *
     * <p>This list can be used programmatically rather than hooking it up to
     * a UI component. <strong>Calling this method directly while this list
     * is connected to a particular widget could result in errors.</strong>
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> This method is
     * thread ready but not thread safe. See {@link EventList} for an example
     * of thread safe code.
     */
    public void setUpperThreshold(E object) {
        setUpperThreshold(evaluator.evaluate(object));
    }

    /**
     * Sets the upper threshold for this list.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> This method is
     * thread ready but not thread safe. See {@link EventList} for an example
     * of thread safe code.
     */
    public void setUpperThreshold(int upperThreshold) {
        this.upperThreshold = upperThreshold;
        adjustRange();
    }

    /**
     * Gets the upper threshold for this list
     */
    public int getUpperThreshold() {
        return upperThreshold;
    }

    /**
     * A convenience method to allow access to the {@link Evaluator}
     * that was provided on construction.
     */
    public Evaluator<E> getEvaluator() {
        return evaluator;
    }

    /** {@inheritDoc} */
    public boolean contains(Object object) {
        // Fast fail if the object isn't within the thresholds
        // Note: this technically breaks the contract for contains.
        // evaluator.evaluate(object) may throw a ClassCastException
        if(!withinRange((E)object)) return false;
        return source.contains(object);
    }

    /** {@inheritDoc} */
    public int indexOf(Object object) {
        // Fast fail if the object isn't within the thresholds
        // Note: this technically breaks the contract for indexOf.
        // evaluator.evaluate(object) may throw a ClassCastException
        if(!withinRange((E)object)) return -1;
        return source.indexOf(object);
    }

    /** {@inheritDoc} */
    public int lastIndexOf(Object object) {
        // Fast fail if the object isn't within the thresholds
        // Note: this technically breaks the contract for lastIndexOf.
        // evaluator.evaluate(object) may throw a ClassCastException
        if(!withinRange((E)object)) return -1;
        return source.lastIndexOf(object);
    }

    /**
     * Test if the specified object is within the range of this {@link ThresholdList}.
     */
    private boolean withinRange(E object) {
        int objectEvaluation = evaluator.evaluate(object);
        return objectEvaluation >= lowerThreshold && objectEvaluation <= upperThreshold;
    }

    /** {@inheritDoc} */
    public void setRange(int startIndex, int endIndex) {
        // this implementation is slightly inconsistent with the superclass
        // because the super treats endIndex as exclusive wheras we treat
        // endIndex as inclusive
        this.lowerThreshold = sourceIndexToThreshold(startIndex);
        this.upperThreshold = sourceIndexToThreshold(endIndex);
        adjustRange();
    }

    /** {@inheritDoc} */
    public void setTailRange(int startIndex, int endIndex) {
        // this implementation is slightly inconsistent with the superclass
        // because the super treats endIndex as exclusive wheras we treat
        // endIndex as inclusive
        this.lowerThreshold = sourceIndexToThreshold(source.size() - startIndex);
        this.upperThreshold = sourceIndexToThreshold(source.size() - endIndex);
        adjustRange();
    }

    /**
     * Given an index into the source {@link EventList}, get the
     * threshold value for that index.
     */
    private int sourceIndexToThreshold(int sourceIndex) {
        if(sourceIndex < 0) {
            return Integer.MIN_VALUE;
        } else if(sourceIndex < source.size()) {
            return evaluator.evaluate(source.get(sourceIndex));
        } else {
            return Integer.MIN_VALUE;
        }
    }

    /** {@inheritDoc} */
    public int getStartIndex() {
        return sortedSource.sortIndex(new Integer(lowerThreshold));
    }

    /** {@inheritDoc} */
    public int getEndIndex() {
        // search for the upperThreshold value
        int index = sortedSource.lastSortIndex(new Integer(upperThreshold));

        // if the upperThreshold exists in the sortedSource, convert the exclusive index to an inclusive index
        if (index < sortedSource.size() && evaluator.evaluate(sortedSource.get(index)) == upperThreshold)
            index++;

        return index;
    }

    /** {@inheritDoc} */
    public void dispose() {
        sortedSource.dispose();
        super.dispose();
    }

    /**
     * Provide an integer value for a given {@link Object} in a
     * {@link ThresholdList}.
     */
    public interface Evaluator<E> {
        /**
         * Returns an integer value for an {@link Object} to be used to
         * compare that object against a threshold.  This value is
         * not relative to any other object unlike a {@link Comparator}.
         */
        public int evaluate(E object);
    }

    /**
     * A ThresholdComparator is a simple helper class that wraps
     * an {@link Evaluator} with a <code>Comparator</code> to
     * be used for sorting of the <code>ThresholdList</code>.
     */
    static final class ThresholdComparator<E> implements Comparator<E> {

        /** the underlying evaluator */
        private Evaluator<E> evaluator = null;

        /**
         * Creates a new ThresholdComparator
         */
        ThresholdComparator(Evaluator<E> evaluator) {
            this.evaluator = evaluator;
        }

        /**
         * Compares two <code>Object</code>s, and compares them using the result
         * given when each <code>Object</code> is evaluated using the underlying
         * {@link Evaluator}.
         *
         * <p>This method is dual-mode as in the case of the Objects passed being
         * <code>Integer</code>s, it returns the value of
         * <code>((Integer)alpha).intValue() - ((Integer)beta).intValue()</code>.
         * This is necessary so that a threshold value can be compared against an
         * <code>Object</code>, and vice versa.  This can cause problems however
         * if the underlying {@link Evaluator} were to return the negation
         * of an <code>Integer</code>.
         */
        public int compare(E alpha, E beta) {
            int alphaValue;
            if(alpha instanceof Integer) alphaValue = ((Integer)alpha).intValue();
            else alphaValue = evaluator.evaluate(alpha);

            int betaValue;
            if(beta instanceof Integer) betaValue = ((Integer)beta).intValue();
            else betaValue = evaluator.evaluate(beta);

            if(alphaValue > betaValue) return 1;
            else if(alphaValue < betaValue) return -1;
            else return 0;
        }

        /** {@inheritDoc} */
        public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;

            final ThresholdComparator that = (ThresholdComparator) o;

            if(!evaluator.equals(that.evaluator)) return false;

            return true;
        }

        /** {@inheritDoc} */
        public int hashCode() {
            return evaluator.hashCode();
        }
    }
}