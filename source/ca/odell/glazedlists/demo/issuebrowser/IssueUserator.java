/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.demo.issuebrowser;

import java.util.*;
// glazed lists
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;


/**
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class IssueUserator implements CollectionListModel {

    public List getChildren(Object parent) {
        Issue issue = (Issue)parent;
        return issue.getAllUsers();
    }
}
