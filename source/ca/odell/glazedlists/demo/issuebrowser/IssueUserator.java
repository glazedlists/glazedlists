package ca.odell.glazedlists.demo.issuebrowser;

import java.util.*;
// glazed lists
import ca.odell.glazedlists.*;

/**
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class IssueUserator implements CollectionListModel<Issue,String> {
    public List<String> getChildren(Issue parent) {
        return parent.getAllUsers();
    }
}