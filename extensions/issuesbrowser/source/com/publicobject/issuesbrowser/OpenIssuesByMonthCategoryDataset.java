/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.jfreechart.EventListCategoryDataset;
import ca.odell.glazedlists.jfreechart.ValueSegment;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.swing.GlazedListsSwing;

import java.util.Date;
import java.util.List;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

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

    private static final DateFormat COLUMN_KEY_DATE_FORMATTER = new SimpleDateFormat("MMMMM yyyy");

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

    private OpenIssuesByMonthCategoryDataset(CollectionList<Issue,ValueSegment<Date,String>> valueSegments) {
        // filter away ValueSegments whose value represents a retired issue (RESOLVED or CLOSED)
        this(new FilterList<ValueSegment<Date,String>>(valueSegments, new OpenIssuesMatcher()));
    }

    private OpenIssuesByMonthCategoryDataset(FilterList<ValueSegment<Date,String>> filteredValueSegments) {
        // ensure all changes are delivered on the EDT to this Category Dataset,
        // since we're displaying the chart on a Swing component
        super(GlazedListsSwing.swingThreadProxyList(filteredValueSegments));
    }

    protected SequenceList.Sequencer<? extends Comparable> createSequencer() {
        return Sequencers.monthSequencer();
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

        // if the rowkey is not found, add it
        final List<String> rowKeys = getRowKeys();
        if (!contains(rowKeys, value))
            rowKeys.add(value);

        final List<Comparable> columnKeys = getColumnKeys();
        columnKeys.add(segment.getStart());
        columnKeys.add(segment.getEnd());

//        // get the earliest date from the segment that was inserted
//        final Date date = segment.getStart();
//        // get the columnKeys that already exist
//        final List<ValueSegment<Date,String>> columnKeys = getColumnKeys();
//
//        // determine the earliest date recorded in the existing columns
//        final Date earliestColumnKey = columnKeys.isEmpty() ? new Date() : columnKeys.get(0).getStart();
//
//        // if the newly inserted segment predates the earliest recorded column key
//        if (earliestColumnKey.after(date)) {
//            Calendar cal = Calendar.getInstance();
//            cal.setTime(earliestColumnKey);
//
//            // we must insert enough column keys to include the new segment
//            while (cal.getTime().after(date)) {
//                // create a new segment for the month
//                final MonthSegment newColumnKey = new MonthSegment(cal);
//
//                // add the segment to the list of column keys
//                columnKeys.add(0, newColumnKey);
//
//                // step back to the previous month
//                cal.add(Calendar.MONTH, -1);
//            }
//        }
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

        // if no more data is associated to the row, remove its rowkey
        if (getCount(value) == 0)
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
     * This Matcher filters away RESOLVED and CLOSED ValueSegment objects
     * since they do not represent an Issue in an open state:
     * (NEW, STARTED, REOPENED, VERIFIED, UNCONFIRMED)
     */
    private static class OpenIssuesMatcher implements Matcher<ValueSegment<Date,String>> {
        public boolean matches(ValueSegment<Date, String> item) {
            final String value = item.getValue();
            return value != "RESOLVED" && value != "CLOSED";
        }
    }
}