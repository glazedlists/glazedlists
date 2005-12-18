/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser.swing;

import com.publicobject.issuesbrowser.Issue;
import com.publicobject.issuesbrowser.Description;

import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Iterator;

/**
 * The details for a particular issue listed out. This also includes a link
 * component, to view the issue in a webbrowser using webstart's
 * BasicService.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
class IssueDetailsComponent {

    private JPanel toolbarAndDescription;
    private JScrollPane scrollPane;
    private JPanel toolBar = new JPanel();
    private JTextPane descriptionsTextPane = new JTextPane();
    private Style plainStyle;
    private Style whoStyle;
    private LinkAction linkAction;
    private Issue issue = null;

    public IssueDetailsComponent() {
        descriptionsTextPane = new JTextPane();
        descriptionsTextPane.setEditable(false);
        plainStyle = descriptionsTextPane.getStyledDocument().addStyle("plain", null);
        whoStyle = descriptionsTextPane.getStyledDocument().addStyle("boldItalicRed", null);
        StyleConstants.setBold(whoStyle, true);
        StyleConstants.setFontSize(whoStyle, 14);
        scrollPane = new JScrollPane(descriptionsTextPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        linkAction = new LinkAction();
        toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolBar.add(new JButton(linkAction));

        toolbarAndDescription = new JPanel(new BorderLayout());
        toolbarAndDescription.add(toolBar, BorderLayout.NORTH);
        toolbarAndDescription.add(scrollPane, BorderLayout.CENTER);

        // prepare the initial state
        setIssue(null);
    }

    private class LinkAction extends AbstractAction {
        public LinkAction() {
            super("View Issue");
        }
        public void actionPerformed(ActionEvent event) {
            WebStart webStart = WebStart.tryCreate();
            if(webStart == null) return;
            webStart.openUrl(issue.getURL());
        }
    }

    public JComponent getComponent() {
        return toolbarAndDescription;
    }

    public void setIssue(Issue issue) {
        this.issue = issue;

        // update the document
        clear(descriptionsTextPane.getStyledDocument());
        if(issue != null) {
            for(Iterator<Description> d = issue.getDescriptions().iterator(); d.hasNext(); ) {
                Description description = d.next();
                writeDescription(descriptionsTextPane.getStyledDocument(), description);
                if(d.hasNext()) append(descriptionsTextPane.getStyledDocument(), "\n\n", plainStyle);
            }
        }
        descriptionsTextPane.setCaretPosition(0);

        // update the link
        linkAction.setEnabled(issue != null);
    }

    /**
     * Clears the styled document.
     */
    protected void clear(StyledDocument doc) {
        try {
            doc.remove(0, doc.getLength());
        } catch(BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Write a document to a styled document.
     */
    public void writeDescription(StyledDocument doc, Description description) {
        // write who
        append(doc, description.getWho(), whoStyle);
        append(doc, " - ", whoStyle);
        append(doc, IssuesBrowser.DATE_FORMAT.format(description.getWhen()), whoStyle);
        append(doc, "\n", whoStyle);

        // write the body
        append(doc, description.getText(), plainStyle);
    }

    /**
     * Convenience method for appending the specified text to the specified document.
     *
     * @param text   The text to append. The characters "\n" and "\t" are
     *               useful for creating newlines.
     * @param format The format to render text in. This class comes with
     *               a small set of predefined formats accessible only to extending
     *               classes via protected members.
     */
    public static void append(StyledDocument targetDocument, String text, Style format) {
        try {
            int offset = targetDocument.getLength();
            targetDocument.insertString(offset, text, format);
        } catch(BadLocationException e) {
            throw new RuntimeException(e);
        }
    }
}