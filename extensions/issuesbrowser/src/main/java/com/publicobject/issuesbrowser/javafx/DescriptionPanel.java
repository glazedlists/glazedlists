/* Glazed Lists                                                 (c) 2003-2014 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser.javafx;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import com.publicobject.issuesbrowser.Description;
import com.publicobject.issuesbrowser.Issue;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.scene.web.WebView;

import java.util.Iterator;

/**
 * The details for a particular issue listed out. This also includes a link
 * component, to view the issue in a WebView.
 *
 * @author Holger Brands
 */
public final class DescriptionPanel {

    /** Issue-Property. */
    private SimpleObjectProperty<Issue> issueProperty = new SimpleObjectProperty<Issue>();

    private WebView webView = new WebView();

    public DescriptionPanel() {
        issueProperty.addListener(new ChangeListener<Issue>() {
            @Override
            public void changed(ObservableValue<? extends Issue> paramObservableValue,
                    Issue oldIssue, Issue newIssue) {
                updateDescription(newIssue);
            }
        });

        webView.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
            @Override
            public void changed(ObservableValue ov, State oldState, State newState) {
                if (newState == Worker.State.SUCCEEDED) {
                    EventListener listener = new EventListener() {
                        @Override
                        public void handleEvent(Event ev) {
                            String domEventType = ev.getType();
                            //System.err.println("EventType: " + domEventType);
                            if (domEventType.equals("click")) {
                                String href = ((Element)ev.getTarget()).getAttribute("href");
                                System.err.println("clicked: " + href);
                                //////////////////////
                                // here do what you want with that clicked event
                                // and the content of href
                                //////////////////////
                            }
                        }
                    };

                    Document doc = webView.getEngine().getDocument();
                    NodeList nodeList = doc.getElementsByTagName("a");
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        ((EventTarget) nodeList.item(i)).addEventListener("click", listener, false);
                        //((EventTarget) nodeList.item(i)).addEventListener(EVENT_TYPE_MOUSEOVER, listener, false);
                        //((EventTarget) nodeList.item(i)).addEventListener(EVENT_TYPE_MOUSEOVER, listener, false);
                    }
                }
            }
        });
    }

    private void updateDescription(Issue issue) {
        String result;
        if (issue == null) {
            result = "";
        } else {
            final StringBuffer htmlText = new StringBuffer("<h3><a href=");
            htmlText.append(issue.getURL());
            htmlText.append(">View Issue ");
            htmlText.append(issue.getId());
            htmlText.append("</a></h3>");
            for (Iterator<Description> d = issue.getDescriptions().iterator(); d.hasNext();) {
                Description description = d.next();
                writeDescription(htmlText, description);
            }
            result = htmlText.toString();
        }
        showHtmlTest(result);
    }

    private void showHtmlTest(String htmlTest) {
        webView.getEngine().loadContent(htmlTest);
    }

    public WebView getWebView() {
        return webView;
    }

    public Property<Issue> issueProperty() {
        return issueProperty;
    }

    public Issue getIssue() {
        return issueProperty.get();
    }

    public void setIssue(Issue issue) {
        issueProperty.set(issue);
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
