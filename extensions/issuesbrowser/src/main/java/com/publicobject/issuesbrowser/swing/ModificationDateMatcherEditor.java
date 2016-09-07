package com.publicobject.issuesbrowser.swing;

import ca.odell.glazedlists.Filterator;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.nachocalendar.NachoDateRangeMatcherEditor;
import com.publicobject.issuesbrowser.Issue;

import java.util.Date;
import java.util.List;

/**
 * This FilterComponent allows the user to specify a range of dates which
 * filters the Issues by their delta date. Only Issues modified within the
 * specified range are considered a match.
 *
 * @author James Lemieux
 */
public class ModificationDateMatcherEditor extends NachoDateRangeMatcherEditor<Issue> implements FilterComponent<Issue> {
    /** A Filterator which extracts the delta date from Issue objects. */
    private static final Filterator<Date,Issue> ISSUE_DELTA_DATE_FILTERATOR = new IssueDeltaDateFilterator();

    public ModificationDateMatcherEditor() {
        super(ModificationDateMatcherEditor.ISSUE_DELTA_DATE_FILTERATOR);
    }

    @Override
    public String toString() {
        return "Modification Date";
    }

    @Override
    public MatcherEditor<Issue> getMatcherEditor() {
        return this;
    }

    /**
     * This Filterator extracts the delta date from each Issue object.
     */
    private static final class IssueDeltaDateFilterator implements Filterator<Date,Issue> {
        @Override
        public void getFilterValues(List<Date> baseList, Issue element) {
            baseList.add(element.getDeltaTimestamp());
        }
    }
}