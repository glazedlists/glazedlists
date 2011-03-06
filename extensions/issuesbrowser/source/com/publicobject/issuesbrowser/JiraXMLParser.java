/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

import com.publicobject.misc.xml.*;

/**
 * <code>JiraXMLParser</code> adapted from {@link IssuezillaXMLParser} to support JIRA issue
 * parsing from its XML view.
 *
 * @author Holger Brands
 */
public class JiraXMLParser {

    private static final int maxIssueCount = 30;

    private static final String JAVA_NET_JIRA_QUERY = "http://java.net/jira/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?jqlQuery=project+%3D+@PROJECT@+ORDER+BY+key+DESC&tempMax=" + maxIssueCount;

    /** the date format for "issue_when" is documented in the DTD to be 'yyyy-MM-dd HH:mm' but is actually 'yyyy-MM-dd HH:mm:ss' */
    /** the date format for "delta_ts" is documented in the DTD to be 'yyyy-MM-dd HH:mm' but is actually 'yyyyMMddHHmmss' */
//    private static final DateFormat[] dateFormats = {new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"), new SimpleDateFormat("yyyyMMddHHmmss")};
    private static final DateFormat[] dateFormats = {new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z (z)", Locale.US), new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US)};

    // hardcode the servers in California
//    static {
//        final TimeZone laTimeZone = TimeZone.getTimeZone("America/Los_Angeles");
//        for (int i = 0; i < dateFormats.length; i++)
//            dateFormats[i].setTimeZone(laTimeZone);
//    }

    private static Parser createParser(Project project) {
        final Parser issueParser = new Parser();
        // configure the Parser for Issues

        // Parsing instructions for Issue
        final XMLTagPath issueTag = new XMLTagPath("rss").child("channel").child("item");
        issueParser.addProcessor(issueTag.start(),                           Processors.createNewObject(Issue.class, new Class[] {Project.class}, new Object[] {project}));
//        issueParser.addProcessor(issueTag.attribute("status_code"),          Processors.setterMethod(Issue.class, "statusCode", Converters.trimAndIntern()));
        issueParser.addProcessor(issueTag.child("key"),                      Processors.setterMethod(Issue.class, "id", Converters.trimAndIntern()));
        issueParser.addProcessor(issueTag.child("status"),                   Processors.setterMethod(Issue.class, "status", Converters.trimAndIntern()));
        issueParser.addProcessor(issueTag.child("priority"),                 Processors.setterMethod(Issue.class, "priority", new JiraPriorityConverter()));
        issueParser.addProcessor(issueTag.child("resolution"),               Processors.setterMethod(Issue.class, "resolution", Converters.trimAndIntern()));
        issueParser.addProcessor(issueTag.child("component"),                Processors.setterMethod(Issue.class, "subcomponent", Converters.trimAndIntern()));
        issueParser.addProcessor(issueTag.child("version"),                  Processors.setterMethod(Issue.class, "version", Converters.trimAndIntern()));
        issueParser.addProcessor(issueTag.child("environment"),              Processors.setterMethod(Issue.class, "repPlatform", Converters.trimAndIntern()));
        issueParser.addProcessor(issueTag.child("assignee"),                 Processors.setterMethod(Issue.class, "assignedTo", Converters.trimAndIntern()));
        issueParser.addProcessor(issueTag.child("updated"),                  Processors.setterMethod(Issue.class, "deltaTimestamp", Converters.date(dateFormats)));
//        issueParser.addProcessor(issueTag.child("subcomponent"),           Processors.setterMethod(Issue.class, "subcomponent", Converters.trimAndIntern()));
        issueParser.addProcessor(issueTag.child("reporter"),                 Processors.setterMethod(Issue.class, "reporter", Converters.trimAndIntern()));
        issueParser.addProcessor(issueTag.child("fixVersion"),               Processors.setterMethod(Issue.class, "targetMilestone", Converters.trimAndIntern()));
        issueParser.addProcessor(issueTag.child("type"),                     Processors.setterMethod(Issue.class, "issueType", Converters.trimAndIntern()));
        issueParser.addProcessor(issueTag.child("created"),                  Processors.setterMethod(Issue.class, "creationTimestamp", Converters.date(dateFormats)));
//        issueParser.addProcessor(issueTag.child("qa_contact"),               Processors.setterMethod(Issue.class, "qAContact", Converters.trimAndIntern()));
//        issueParser.addProcessor(issueTag.child("status_whiteboard"),        Processors.setterMethod(Issue.class, "statusWhiteboard", Converters.trim()));
//        issueParser.addProcessor(issueTag.child("issue_file_loc"),           Processors.setterMethod(Issue.class, "fileLocation", Converters.trim()));
        issueParser.addProcessor(issueTag.child("votes"),                    Processors.setterMethod(Issue.class, "votes", Converters.trimAndIntern()));
//        issueParser.addProcessor(issueTag.child("op_sys"),                   Processors.setterMethod(Issue.class, "operatingSystem", Converters.trimAndIntern()));
        issueParser.addProcessor(issueTag.child("summary"),                  Processors.setterMethod(Issue.class, "shortDescription", Converters.trimAndIntern()));
//        issueParser.addProcessor(issueTag.child("keywords"),                 Processors.addToCollection(Issue.class, "keywords", Converters.trimAndIntern(), Matchers.nonNullAndNonEmptyString()));
//        issueParser.addProcessor(issueTag.child("cc"),                       Processors.addToCollection(Issue.class, "cC", Converters.trimAndIntern(), Matchers.nonNullAndNonEmptyString()));
        issueParser.addProcessor(issueTag.end(),                             new AddIssueToTargetListProcessor());

        // Parsing instructions for Description
        final XMLTagPath descriptionTag = issueTag.child("description");
        issueParser.addProcessor(descriptionTag.start(),                     Processors.createNewObject(Description.class));
//        issueParser.addProcessor(descriptionTag.child("who"),                Processors.setterMethod(Description.class, "who", Converters.trimAndIntern()));
//        issueParser.addProcessor(descriptionTag.child("issue_when"),         Processors.setterMethod(Description.class, "when", Converters.date(dateFormats)));
        issueParser.addProcessor(descriptionTag.body(),                      Processors.setterMethod(Description.class, "text", Converters.trim()));
        issueParser.addProcessor(descriptionTag.end(),                       Processors.addToCollection(Issue.class, "descriptions"));

        // Parsing instructions for Description
        final XMLTagPath commentTag = issueTag.child("comments").child("comment");
        issueParser.addProcessor(commentTag.start(),                        Processors.createNewObject(Description.class));
        issueParser.addProcessor(commentTag.attribute("author"),            Processors.setterMethod(Description.class, "who", Converters.trimAndIntern()));
        issueParser.addProcessor(commentTag.attribute("created"),           Processors.setterMethod(Description.class, "when", Converters.date(dateFormats)));
        issueParser.addProcessor(commentTag.body(),                         Processors.setterMethod(Description.class, "text", Converters.trim()));
        issueParser.addProcessor(commentTag.end(),                          Processors.addToCollection(Issue.class, "descriptions"));

        // Parsing instructions for resolved tag, convert to activity
        final XMLTagPath resolvedTag = issueTag.child("resolved");
        issueParser.addProcessor(resolvedTag.start(),                       Processors.createNewObject(Activity.class, new Class[] {String.class, String.class}, new Object[] {"issue_status", JiraStatus.RESOLVED.getId()}));
        issueParser.addProcessor(resolvedTag.body(),                        Processors.setterMethod(Activity.class, "when", Converters.date(dateFormats)));
        issueParser.addProcessor(resolvedTag.end(),                         Processors.addToCollection(Issue.class, "activities"));

        // Parsing instructions for Activity
//        final XMLTagPath activityTag = issueTag.child("activity");
//        issueParser.addProcessor(activityTag.start(),                        Processors.createNewObject(Activity.class));
//        issueParser.addProcessor(activityTag.child("user"),                  Processors.setterMethod(Activity.class, "user", Converters.trimAndIntern()));
//        issueParser.addProcessor(activityTag.child("when"),                  Processors.setterMethod(Activity.class, "when", Converters.date(dateFormats)));
//        issueParser.addProcessor(activityTag.child("field_name"),            Processors.setterMethod(Activity.class, "field", Converters.trimAndIntern()));
//        issueParser.addProcessor(activityTag.child("field_desc"),            Processors.setterMethod(Activity.class, "fieldDescription", Converters.trimAndIntern()));
//        issueParser.addProcessor(activityTag.child("oldvalue"),              Processors.setterMethod(Activity.class, "oldValue", Converters.trimAndIntern()));
//        issueParser.addProcessor(activityTag.child("newvalue"),              Processors.setterMethod(Activity.class, "newValue", Converters.trimAndIntern()));
//        issueParser.addProcessor(activityTag.end(),                          Processors.addToCollection(Issue.class, "activities"));

        // Parsing instructions for Attachment
        final XMLTagPath attachmentTag = issueTag.child("attachments").child("attachment");
        issueParser.addProcessor(attachmentTag.start(),                      Processors.createNewObject(Attachment.class));
//        issueParser.addProcessor(attachmentTag.child("mimetype"),          Processors.setterMethod(Attachment.class, "mimeType", Converters.trimAndIntern()));
        issueParser.addProcessor(attachmentTag.attribute("id"),              Processors.setterMethod(Attachment.class, "attachId", Converters.trimAndIntern()));
        issueParser.addProcessor(attachmentTag.attribute("created"),         Processors.setterMethod(Attachment.class, "date", Converters.date(dateFormats)));
//        issueParser.addProcessor(attachmentTag.child("desc"),              Processors.setterMethod(Attachment.class, "description", Converters.trim()));
//        issueParser.addProcessor(attachmentTag.child("ispatch"),           Processors.setterMethod(Attachment.class, "isPatch", Converters.trim()));
        issueParser.addProcessor(attachmentTag.attribute("name"),            Processors.setterMethod(Attachment.class, "filename", Converters.trim()));
//        issueParser.addProcessor(attachmentTag.child("submitter_id"),      Processors.setterMethod(Attachment.class, "submitterId", Converters.trimAndIntern()));
        issueParser.addProcessor(attachmentTag.attribute("author"),          Processors.setterMethod(Attachment.class, "submitterUsername", Converters.trimAndIntern()));
//        issueParser.addProcessor(attachmentTag.child("data"),                Processors.setterMethod(Attachment.class, "data", Converters.trim()));
//        issueParser.addProcessor(attachmentTag.child("attachment_iz_url"),   Processors.setterMethod(Attachment.class, "attachmentIzUrl", Converters.trim()));
        issueParser.addProcessor(attachmentTag.end(),                        Processors.addToCollection(Issue.class, "attachments"));

        // TODO...

        // Parsing instructions for duplicante PeerIssues
        final XMLTagPath hasDuplicatesTag = issueTag.child("has_duplicates");
        issueParser.addProcessor(hasDuplicatesTag.start(),                   Processors.createNewObject(PeerIssue.class));
        issueParser.addProcessor(hasDuplicatesTag.child("issue_id"),         Processors.setterMethod(PeerIssue.class, "issueId", Converters.trimAndIntern()));
        issueParser.addProcessor(hasDuplicatesTag.child("who"),              Processors.setterMethod(PeerIssue.class, "who", Converters.trimAndIntern()));
        issueParser.addProcessor(hasDuplicatesTag.child("when"),             Processors.setterMethod(PeerIssue.class, "when", Converters.date(dateFormats)));
        issueParser.addProcessor(hasDuplicatesTag.end(),                     Processors.addToCollection(Issue.class, "duplicates"));

        // Parsing instructions for a duplicate PeerIssue
        final XMLTagPath isDuplicateTag = issueTag.child("is_duplicate");
        issueParser.addProcessor(isDuplicateTag.start(),                     Processors.createNewObject(PeerIssue.class));
        issueParser.addProcessor(isDuplicateTag.child("issue_id"),           Processors.setterMethod(PeerIssue.class, "issueId", Converters.trimAndIntern()));
        issueParser.addProcessor(isDuplicateTag.child("who"),                Processors.setterMethod(PeerIssue.class, "who", Converters.trimAndIntern()));
        issueParser.addProcessor(isDuplicateTag.child("when"),               Processors.setterMethod(PeerIssue.class, "when", Converters.date(dateFormats)));
        issueParser.addProcessor(isDuplicateTag.end(),                       Processors.setterMethod(Issue.class, "duplicate"));

        // Parsing instructions for a dependent PeerIssue
        final XMLTagPath dependsOnTag = issueTag.child("dependson");
        issueParser.addProcessor(dependsOnTag.start(),                       Processors.createNewObject(PeerIssue.class));
        issueParser.addProcessor(dependsOnTag.child("issue_id"),             Processors.setterMethod(PeerIssue.class, "issueId", Converters.trimAndIntern()));
        issueParser.addProcessor(dependsOnTag.child("who"),                  Processors.setterMethod(PeerIssue.class, "who", Converters.trimAndIntern()));
        issueParser.addProcessor(dependsOnTag.child("when"),                 Processors.setterMethod(PeerIssue.class, "when", Converters.date(dateFormats)));
        issueParser.addProcessor(dependsOnTag.end(),                         Processors.addToCollection(Issue.class, "dependsOn"));

        // Parsing instructions for a blocking PeerIssue
        final XMLTagPath blocksTag = issueTag.child("blocks");
        issueParser.addProcessor(blocksTag.start(),                          Processors.createNewObject(PeerIssue.class));
        issueParser.addProcessor(blocksTag.child("issue_id"),                Processors.setterMethod(PeerIssue.class, "issueId", Converters.trimAndIntern()));
        issueParser.addProcessor(blocksTag.child("who"),                     Processors.setterMethod(PeerIssue.class, "who", Converters.trimAndIntern()));
        issueParser.addProcessor(blocksTag.child("when"),                    Processors.setterMethod(PeerIssue.class, "when", Converters.date(dateFormats)));
        issueParser.addProcessor(blocksTag.end(),                            Processors.addToCollection(Issue.class, "blocks"));

        return issueParser;
    }

    /**
     * When executed, this opens a file specified on the command line, parses
     * it for Issuezilla XML and writes the issues to the command line.
     */
    public static void main(String[] args) throws IOException {
        final EventList<Issue> issuesList = new BasicEventList<Issue>();

        loadIssues(issuesList, Project.getProjects().get(1));
    }

    public static void loadIssues(EventList<Issue> target, Project owner) throws IOException {
        final String queryURLTemplate = JAVA_NET_JIRA_QUERY;
        final String queryURL = queryURLTemplate.replace("@PROJECT@", owner.getName());
        final URL issuesUrl = new URL(queryURL);
        final InputStream issuesInStream = issuesUrl.openStream();
        // parse
        loadIssues(target, issuesInStream, owner);
    }

    /**
     * Parses the Issuezilla XML document on the specified input stream into a List
     * of issues. While the parsing is taking place this writes some simple Java
     * commands to reproduce a lightweight version of this list. This is useful
     * to load the issues as code rather than XML.
     */
    public static void loadIssues(EventList<Issue> target, InputStream source, Project owner) throws IOException {
        createParser(owner).parse(source, target);
    }

    /**
     * This Converter can lookup Priority objects using Strings.
     */
    private static class JiraPriorityConverter implements Converter<String,Priority> {
        public Priority convert(String value) {
            return Priority.lookupJira(value.trim());
        }
    }

    /**
     * This Processor adds a completely built Issue to the target EventList.
     * It also performs some late processing of the Issue, namely computing the
     * state changes of the Issue.
     */
    private static class AddIssueToTargetListProcessor implements PopProcessor<EventList<Issue>,Issue> {
        private final Date loadingStarted = new Date();

        public void process(EventList<Issue> issues, Issue issue) {
            // populate the descriptions with useful data when missing
            // it's the case for the main description
            for (Description desc : issue.getDescriptions()) {
                if (desc.getWhen() == null && desc.getWho() == null) {
                    desc.setWhen(issue.getCreationTimestamp());
                    desc.setWho(issue.getReporter());
                }
            }
            // compute the timeline of state changes now that we have loaded the entire Issue
            issue.getStateChanges().addAll(Issue.computeStateChanges(issue, loadingStarted));
            // add the Issue to the list of Issues
            issues.add(issue);
        }
    }
}