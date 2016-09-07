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
    /** serialVersionUID. */
    private static final long serialVersionUID = 0L;

    /** The sequencer which produces the uniform column keys. */
    private final SequenceList.Sequencer<Date> monthSequencer = Sequencers.monthSequencer();

    /** The raw, untransformed column keys presented by this dataset. */
    private EventList rawColumnKeys;

    /**
     * Create a CategoryDataset by exploding the {@link Issue} objects into a
     * giant list of {@link ValueSegment} objects, each of which describes a
     * time segment during which the Issue held a given status.
     *
     * @param issues the source list of {@link Issue} objects
     */
    public OpenIssuesByMonthCategoryDataset(EventList<Issue> issues) {
        this(issues, new CollectionList<Issue,ValueSegment<Date,String>>(issues, new StateChangesCollectionModel()));
    }

    private OpenIssuesByMonthCategoryDataset(EventList<Issue> issues, CollectionList<Issue,ValueSegment<Date,String>> valueSegments) {
        // filter away ValueSegments whose value represents a retired issue (RESOLVED or CLOSED)
        this(new FilterList<ValueSegment<Date,String>>(valueSegments, new OpenIssuesMatcher(new StatusProvider(issues))));
    }

    private OpenIssuesByMonthCategoryDataset(FilterList<ValueSegment<Date,String>> filteredValueSegments) {
        // ensure all changes are delivered on the EDT to this Category Dataset,
        // since we're displaying the chart on a Swing component
        super(GlazedListsSwing.swingThreadProxyList(filteredValueSegments));
    }

    /**
     * This data set uses a pipeline of Glazed Lists to create its column keys.
     * It wraps a raw BasicEventList in a SequenceList in order to generate a
     * sequence of column keys which span the data. It then uses a RangleList
     * to chop off the last value since it is beyond any of the data values to
     * be shown in the chart.
     */
    @Override
    protected List<? extends Comparable> createColumnKeyList() {
        this.rawColumnKeys = new BasicEventList();

        // summarize all Dates added to rawColumnKeys using a Sequence of normalized month Dates
        final SequenceList columnKeySequenceList = new SequenceList(this.rawColumnKeys, Sequencers.monthSequencer());

        // chop off the last element of the sequence list
        final RangeList<Comparable> columnKeyList = new RangeList<Comparable>(columnKeySequenceList);
        columnKeyList.setMiddleRange(0, 1);

        return columnKeyList;
    }

    /** @inheritDoc */
    @Override
    public Number getValue(Comparable rowKey, Comparable columnKey) {
        // the columnKey is expected to be a Date
        final Date start = (Date) columnKey;
        final Date end = this.monthSequencer.next(start);

        return new Integer(this.getCount((String) rowKey, start, end));
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
        for (int i = 0, n = l.size(); i < n; i++)
            if (value == l.get(i))
                return true;

        return false;
    }

    /**
     * When a new {@link ValueSegment} is inserted we process the insertion by
     * determining if:
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
    @Override
    protected void postInsert(ValueSegment<Date,String> segment) {
        final String value = segment.getValue();

        // if the rowkey is not found, add it
        final List<String> rowKeys = getRowKeys();
        if (!contains(rowKeys, value))
            rowKeys.add(value);

        rawColumnKeys.add(segment.getStart());
        rawColumnKeys.add(segment.getEnd());
    }

    /**
     * When an existing {@link ValueSegment} is removed we process the deletion
     * by determining if:
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
    @Override
    protected void postDelete(ValueSegment<Date,String> segment) {
        final String value = segment.getValue();

        // if no more data is associated to the row, remove its rowkey
        if (getCount(value) == 0)
            getRowKeys().remove(value);

        rawColumnKeys.remove(segment.getStart());
        rawColumnKeys.remove(segment.getEnd());
    }

    /**
     * This Model decomposes Issues into a List of ValueSegment objects
     * describing the state changes and when they occurred during the
     * "lifetime" of the Issue.
     */
    private static class StateChangesCollectionModel implements CollectionList.Model<Issue, ValueSegment<Date,String>> {
        @Override
        public List<ValueSegment<Date, String>> getChildren(Issue parent) {
            return parent.getStateChanges();
        }
    }

    /**
     * This Matcher filters away RESOLVED, VERIFIED and CLOSED ValueSegment objects
     * since they do not represent an Issue in an open state.
     */
    private static class OpenIssuesMatcher implements Matcher<ValueSegment<Date,String>> {
        private StatusProvider statusProvider;

        public OpenIssuesMatcher(StatusProvider statusProvider) {
            this.statusProvider = statusProvider;
        }
        @Override
        public boolean matches(ValueSegment<Date, String> item) {
            final String value = item.getValue();
            return statusProvider.statusFor(value).isActive();
        }
    }
}