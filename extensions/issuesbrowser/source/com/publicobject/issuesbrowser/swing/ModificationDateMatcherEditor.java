package com.publicobject.issuesbrowser.swing;

import com.publicobject.issuesbrowser.Issue;
import ca.odell.glazedlists.nachocalendar.NachoDateRangeMatcherEditor;
import ca.odell.glazedlists.Filterator;
import ca.odell.glazedlists.matchers.MatcherEditor;

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

    public String toString() {
        return "Modification Date";
    }

    public MatcherEditor<Issue> getMatcherEditor() {
        return this;
    }

    /**
     * This Filterator extracts the delta date from each Issue object.
     */
    private static final class IssueDeltaDateFilterator implements Filterator<Date,Issue> {
        public void getFilterValues(List<Date> baseList, Issue element) {
            baseList.add(element.getDeltaTimestamp());
        }
    }
}