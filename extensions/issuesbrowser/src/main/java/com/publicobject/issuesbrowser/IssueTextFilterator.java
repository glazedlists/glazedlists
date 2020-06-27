/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser;

import ca.odell.glazedlists.TextFilterator;

import java.util.Iterator;
import java.util.List;

/**
 * Provide text filter strings for {@link Issue} objects.
 *
 * @author <a href="jesse@swank.ca">Jesse Wilson</a>
 */
public class IssueTextFilterator implements TextFilterator<Issue> {
    /** {@inheritDoc} */
    @Override
    public void getFilterStrings(List<String> baseList, Issue i) {
        // the displayed strings
        baseList.add(i.getId());
        baseList.add(i.getIssueType());
        baseList.add(Issue.TABLE_DATE_FORMAT.format(i.getCreationTimestamp()));
        baseList.add(i.getPriority().toString());
        baseList.add(Issue.TABLE_DATE_FORMAT.format(i.getDeltaTimestamp()));
        baseList.add(i.getStatus());
        baseList.add(i.getResolution());
        baseList.add(i.getShortDescription());

        // extra strings
        baseList.add(i.getComponent());
        baseList.add(i.getSubcomponent());

        // filter strings from the descriptions
        for(Iterator<Description> d = i.getDescriptions().iterator(); d.hasNext(); )
            d.next().getFilterStrings(baseList);
    }
}
