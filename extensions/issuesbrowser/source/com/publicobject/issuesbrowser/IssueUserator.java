/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser;

import ca.odell.glazedlists.CollectionList;

import java.util.List;

/**
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class IssueUserator implements CollectionList.Model<Issue, String> {
    public List<String> getChildren(Issue issue) {
        return issue.getAllUsers();
    }
}