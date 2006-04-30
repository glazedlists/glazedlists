/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser.swing;

import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import ca.odell.glazedlists.TextFilterator;

import javax.swing.*;

import com.publicobject.issuesbrowser.Issue;
import com.publicobject.issuesbrowser.Description;

import java.util.List;
import java.util.Iterator;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class TextFilterComponent implements FilterComponent<Issue> {

    private static final TextFilterator<Issue> ISSUE_TEXT_FILTERATOR = new IssueTextFilterator();

    private JTextField filterEdit = new JTextField(15);
    private TextComponentMatcherEditor<Issue> textComponentMatcherEditor = new TextComponentMatcherEditor<Issue>(filterEdit, ISSUE_TEXT_FILTERATOR);

    public JComponent getComponent() {
        return filterEdit;
    }

    public MatcherEditor<Issue> getMatcherEditor() {
        return textComponentMatcherEditor;
    }

    public String toString() {
        return "Text Filter";
    }

    private static class IssueTextFilterator implements TextFilterator<Issue> {
        public void getFilterStrings(List<String> baseList, Issue i) {
            // the displayed strings
            baseList.add(i.getId().toString());
            baseList.add(i.getIssueType());
            baseList.add(IssuesBrowser.TABLE_DATE_FORMAT.format(i.getCreationTimestamp()));
            baseList.add(i.getPriority().toString());
            baseList.add(IssuesBrowser.TABLE_DATE_FORMAT.format(i.getDeltaTimestamp()));
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
}