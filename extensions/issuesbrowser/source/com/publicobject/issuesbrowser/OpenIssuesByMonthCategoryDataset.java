/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser;

import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.jfreechart.EventListCategoryDataset;
import ca.odell.glazedlists.jfreechart.ValueSegment;
import ca.odell.glazedlists.jfreechart.DefaultValueSegment;
import ca.odell.glazedlists.swing.GlazedListsSwing;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * This CategoryDataset explodes each {@link Issue} object into a List of
 * {@link ValueSegment}s describing its status changes over time.
 *
 * <p>It extracts the unique set of statuses across all {@link Issue}s as the
 * row keys.
 *
 * <p>It creates {@link ValueSegment}s as the column keys with the start and
 * end values representing the beginning and ending of each month the project
 * has been active. This has the effect of producing monthly counts of
 * statuses.
 *
 * @author James Lemieux
 */
public class OpenIssuesByMonthCategoryDataset extends EventListCategoryDataset<String, Date> {

    /**
     * Create a CategoryDataset by exploding the {@link Issue} objects into a
     * giant list of {@link ValueSegment} objects, each of which describes a
     * time segment during which the Issue held a given status.
     *
     * @param issues the source list of {@link Issue} objects
     */
    public OpenIssuesByMonthCategoryDataset(EventList<Issue> issues) {
        this(new CollectionList<Issue,ValueSegment<Date,String>>(issues, new StateChangesCollectionModel()));
    }

    private OpenIssuesByMonthCategoryDataset(CollectionList<Issue,ValueSegment<Date,String>> rangedValues) {
        // ensure all changes are delivered on the EDT to this Category Dataset,
        // since we're displaying the chart on a Swing component
        super(GlazedListsSwing.swingThreadProxyList(rangedValues));
    }

    /**
     * An ultra-fast method to determine if the given <code>list</code>
     * contains the given <code>value</code>. Precise identity is tested for
     * speed reasons, and no Iterator is used because the data is always
     * accessed from the Swing EDT.
     *
     * @return <tt>true</tt> if <code>l</code> contains <code>value</code>;
     *      <tt>false</tt> otherwise
     */
    private boolean contains(List l, Object value) {
        for (int i = 0; i < l.size(); i++)
            if (value == l.get(i))
                return true;

        return false;
    }

    /**
     * When a new {@link ValueSegment} is inserted we discard it if it
     * represents a closed Issue (its status is CLOSED or RESOLVED). If it
     * passes the test, we process the insertion by determining if:
     *
     * <ul>
     *   <li> the segment contains a new status not currently reported by the
     *        list of row keys
     *   <li> the segment is for a time range not currently reported by the
     *        list of column keys
     * </ul>
     *
     * and adjust the row keys and column keys accordingly.
     *
     * @param segment the {@link ValueSegment} that was added to the Dataset
     */
    protected void postInsert(ValueSegment<Date,String> segment) {
        final String value = segment.getValue();

        // if the value of the segment doesn't represent an OPEN issue, skip it
        if (value == "CLOSED" || value == "RESOLVED")
            return;

        // if the rowkey is not found, add it
        final List<String> rowKeys = getRowKeys();
        if (!contains(rowKeys, value))
            rowKeys.add(value);

        // get the earliest date from the segment that was inserted
        final Date date = segment.getStart();
        // get the columnKeys that already exist
        final List<ValueSegment<Date,String>> columnKeys = getColumnKeys();

        // determine the earliest date recorded in the existing columns
        final Date earliestColumnKey = columnKeys.isEmpty() ? new Date() : columnKeys.get(0).getStart();

        // if the newly inserted segment predates the earliest recorded column key
        if (earliestColumnKey.after(date)) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(earliestColumnKey);

            // we must insert enough column keys to include the new segment
            while (cal.getTime().after(date)) {
                // create a new segment for the month
                final MonthSegment newColumnKey = new MonthSegment(cal);

                // add the segment to the list of column keys
                columnKeys.add(0, newColumnKey);

                // step back to the previous month
                cal.add(Calendar.MONTH, -1);
            }
        }
    }

    /**
     * When an existing {@link ValueSegment} is removed we discard it if it
     * represents a closed Issue (its status is CLOSED or RESOLVED). If it
     * passes the test, we process the deletion by determining if:
     *
     * <ul>
     *   <li> it was the last segment with its status and thus we should remove
     *        that status from the list of row keys
     * </ul>
     *
     * and adjust the row keys accordingly.
     *
     * @param segment the {@link ValueSegment} that was added to the Dataset
     */
    protected void postDelete(ValueSegment<Date,String> segment) {
        final String value = segment.getValue();

        // if the value of the segment doesn't represent an OPEN issue, skip it
        if (value == "CLOSED" || value == "RESOLVED")
            return;

        // if no more data is associated to the row, remove its rowkey
        if(getCount(value) == 0)
            getRowKeys().remove(value);
    }

    /**
     * This Model decomposes Issues into a List of ValueSegment objects
     * describing the state changes and when they occurred during the
     * "lifetime" of the Issue.
     */
    private static class StateChangesCollectionModel implements CollectionList.Model<Issue, ValueSegment<Date,String>> {
        public List<ValueSegment<Date, String>> getChildren(Issue parent) {
            return parent.getStateChanges();
        }
    }

    /**
     * Represents a segment of time from the beginning to the end of a month.
     * It is used as a column key for this Dataset, thus it exists in order to
     * provide a pretty {@link #toString()} value.
     */
    private static class MonthSegment extends DefaultValueSegment<Date,String> {
        private static final DateFormat MONTH_DATE_FORMAT = new SimpleDateFormat("MMMMM yyyy");

        private final String description;

        public MonthSegment(Calendar cal) {
            super(getMonthBegin(cal), getMonthEnd(cal), null);
            this.description = MONTH_DATE_FORMAT.format(this.getStart());
        }

        public String toString() {
            return this.description;
        }

        /**
         * A convenience method to adjust the given <code>calendar</code> to the
         * start of the month and return the resulting Date.
         */
        private static Date getMonthBegin(Calendar calendar) {
            calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DATE));
            calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
            calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
            calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND));
            return calendar.getTime();
        }

        /**
         * A convenience method to adjust the given <code>calendar</code> to the
         * end of the month and return the resulting Date.
         */
        private static Date getMonthEnd(Calendar calendar) {
            calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
            calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMaximum(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE));
            calendar.set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND));
            calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND));
            return calendar.getTime();
        }
    }
}