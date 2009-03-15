/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser.swing;

import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import com.publicobject.issuesbrowser.Issue;
import com.publicobject.issuesbrowser.IssueTextFilterator;

import javax.swing.*;

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

    @Override
    public String toString() {
        return "Text Filter";
    }

}