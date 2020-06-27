/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser.swing;

import com.publicobject.issuesbrowser.Description;
import com.publicobject.issuesbrowser.Issue;
import com.publicobject.misc.swing.MacCornerScrollPaneLayoutManager;
import com.publicobject.misc.swing.WebStart;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import java.net.URL;
import java.util.Iterator;

/**
 * The details for a particular issue listed out. This also includes a link
 * component, to view the issue in a webbrowser using webstart's
 * BasicService.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
class IssueDescriptionsPanel {

    private JScrollPane scrollPane;
    private JTextPane descriptionsTextPane = new JTextPane();

    private final HyperlinkListener hyperLinkListener = new HyperlinkListener() {
        @Override
        public void hyperlinkUpdate(HyperlinkEvent event) {
            if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                openURLIfPossible(event.getURL());
            }
        }
    };

    public IssueDescriptionsPanel() {
        descriptionsTextPane = new JTextPane();
        descriptionsTextPane.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        descriptionsTextPane.setEditable(false);
        descriptionsTextPane.setContentType("text/html");
        descriptionsTextPane.addHyperlinkListener(hyperLinkListener);

        scrollPane = new JScrollPane(descriptionsTextPane,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        MacCornerScrollPaneLayoutManager.install(scrollPane);

        // prepare the initial state
        setIssue(null);
    }

    /** opens a given URL per Webstart if possible. */
    private void openURLIfPossible(URL url) {
        WebStart webStart = WebStart.tryCreate();
        if (webStart == null) return;
        webStart.openUrl(url);
    }

    public JComponent getComponent() {
        return scrollPane;
    }

    public void setIssue(Issue issue) {
        // update the detail text
        if (issue == null) {
            descriptionsTextPane.setText("");
        } else {
            final StringBuffer htmlText = new StringBuffer("<h3><a href=");
            htmlText.append(issue.getURL());
            htmlText.append(">View Issue ");
            htmlText.append(issue.getId());
            htmlText.append("</a></h3>");
            for (Iterator<Description> d = issue.loadAndGetDescriptions().iterator(); d.hasNext();) {
                Description description = d.next();
                writeDescription(htmlText, description);
            }
            descriptionsTextPane.setText(htmlText.toString());
        }
        descriptionsTextPane.setCaretPosition(0);
    }

    /** helper method that appends the description in HTML form to the given StringBuffer. */
    private static void writeDescription(StringBuffer htmlText, Description description) {
        htmlText.append("<h2>");
        htmlText.append(description.getWho());
        htmlText.append(" - ");
        htmlText.append(Issue.DETAILS_DATE_FORMAT.format(description.getWhen()));
        htmlText.append("</h2>");
        // write the body
//        final String text = description.getText();
        final String text = escapeText(description.getText());
        htmlText.append(text);
    }

    /** helper method to fix messed up description text from Jira. */
    private static String escapeText(String source) {
        String result = source;
        if (!source.contains("<br>") && !source.contains("<br/>") && !source.contains("</br>")) {
            // assume no html text
            result = result.replace("\r\n", "<br/>");
            result = result.replace("\n", "<br/>");
            result = result.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
            result = result.replace(" ", "&nbsp;");
        }
        return result;
    }
}