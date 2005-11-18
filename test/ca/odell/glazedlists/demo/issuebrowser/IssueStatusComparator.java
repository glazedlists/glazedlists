package ca.odell.glazedlists.demo.issuebrowser;

import java.util.Comparator;

/**
 * Compare {@link Issue} objects by status.
 */
public class IssueStatusComparator implements Comparator<Issue> {
    public int compare(Issue o1, Issue o2) {
        return o1.getStatus().compareTo(o2.getStatus());
    }
}