/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser;

import java.util.Comparator;

/**
 * Compare {@link Issue} objects by status.
 */
public class IssueStatusComparator implements Comparator<Issue> {
    public int compare(Issue o1, Issue o2) {
        return o1.getStatus().compareTo(o2.getStatus());
    }
}