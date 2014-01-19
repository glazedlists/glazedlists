/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser.swing;

import ca.odell.glazedlists.Filterator;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.nachocalendar.NachoDateRangeMatcherEditor;
import com.publicobject.issuesbrowser.Issue;

import java.util.Date;
import java.util.List;

/**
 * This FilterComponent allows the user to specify a range of dates which
 * filters the Issues by their creation date. Only Issues created within the
 * specified range are considered a match.
 *
 * @author James Lemieux
 */
public class CreationDateMatcherEditor extends NachoDateRangeMatcherEditor<Issue> implements FilterComponent<Issue> {
    /** A Filterator which extracts the creation date from Issue objects. */
    private static final Filterator<Date,Issue> ISSUE_CREATION_DATE_FILTERATOR = new IssueCreationDateFilterator();

    public CreationDateMatcherEditor() {
        super(ISSUE_CREATION_DATE_FILTERATOR);
    }

    @Override
    public String toString() {
        return "Creation Date";
    }

    @Override
    public MatcherEditor<Issue> getMatcherEditor() {
        return this;
    }

    /**
     * This Filterator extracts the creation date from each Issue object.
     */
    private static final class IssueCreationDateFilterator implements Filterator<Date,Issue> {
        @Override
        public void getFilterValues(List<Date> baseList, Issue element) {
            baseList.add(element.getCreationTimestamp());
        }
    }
}