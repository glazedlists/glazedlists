package ca.odell.glazedlists.demo.issuebrowser;

import java.util.*;
// glazed lists
import ca.odell.glazedlists.*;

/**
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class IssueUserator implements CollectionList.Model {

    public List getChildren(Object parent) {
        Issue issue = (Issue)parent;
        return issue.getAllUsers();
    }
}
