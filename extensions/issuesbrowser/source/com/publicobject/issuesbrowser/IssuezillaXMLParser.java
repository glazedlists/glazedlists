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
import java.util.TimeZone;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.matchers.Matchers;

import com.publicobject.misc.xml.*;

/**
 * Parses IssueZilla issues as described by their XML.
 *
 * <p>Parsing supports DTD revision 1.2 only and may not work with prior or
 * later versions of the IssueZilla XML format.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 * @author James Lemieux
 * @see <a href="https://glazedlists.dev.java.net/issues/issuezilla.dtd">Issuezilla DTD</a>
 */
public class IssuezillaXMLParser {

    /** the date format for "issue_when" is documented in the DTD to be 'yyyy-MM-dd HH:mm' but is actually 'yyyy-MM-dd HH:mm:ss' */
    /** the date format for "delta_ts" is documented in the DTD to be 'yyyy-MM-dd HH:mm' but is actually 'yyyyMMddHHmmss' */
    private static final DateFormat[] dateFormats = {new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"), new SimpleDateFormat("yyyyMMddHHmmss")};

    // hardcode the servers in California
    static {
        final TimeZone laTimeZone = TimeZone.getTimeZone("America/Los_Angeles");
        for (int i = 0; i < dateFormats.length; i++)
            dateFormats[i].setTimeZone(laTimeZone);
    }

    private static Parser createParser(Project project) {
        final Parser issueParser = new Parser();
        // configure the Parser for Issues

        // Parsing instructions for Issue
        final XMLTagPath issueTag = new XMLTagPath("issuezilla").child("issue");
        issueParser.addProcessor(issueTag.start(),                           Processors.createNewObject(Issue.class, new Class[] {Project.class}, new Object[] {project}));
        issueParser.addProcessor(issueTag.attribute("status_code"),          Processors.setterMethod(Issue.class, "statusCode", Converters.trimAndIntern()));
        issueParser.addProcessor(issueTag.child("issue_id"),                 Processors.setterMethod(Issue.class, "id", Converters.trimAndIntern()));
        issueParser.addProcessor(issueTag.child("issue_status"),             Processors.setterMethod(Issue.class, "status", Converters.trimAndIntern()));
        issueParser.addProcessor(issueTag.child("priority"),                 Processors.setterMethod(Issue.class, "priority", new PriorityConverter()));
        issueParser.addProcessor(issueTag.child("resolution"),               Processors.setterMethod(Issue.class, "resolution", Converters.trimAndIntern()));
        issueParser.addProcessor(issueTag.child("component"),                Processors.setterMethod(Issue.class, "component", Converters.trimAndIntern()));
        issueParser.addProcessor(issueTag.child("version"),                  Processors.setterMethod(Issue.class, "version", Converters.trimAndIntern()));
        issueParser.addProcessor(issueTag.child("rep_platform"),             Processors.setterMethod(Issue.class, "repPlatform", Converters.trimAndIntern()));
        issueParser.addProcessor(issueTag.child("assigned_to"),              Processors.setterMethod(Issue.class, "assignedTo", Converters.trimAndIntern()));
        issueParser.addProcessor(issueTag.child("delta_ts"),                 Processors.setterMethod(Issue.class, "deltaTimestamp", Converters.date(dateFormats)));
        issueParser.addProcessor(issueTag.child("subcomponent"),             Processors.setterMethod(Issue.class, "subcomponent", Converters.trimAndIntern()));
        issueParser.addProcessor(issueTag.child("reporter"),                 Processors.setterMethod(Issue.class, "reporter", Converters.trimAndIntern()));
        issueParser.addProcessor(issueTag.child("target_milestone"),         Processors.setterMethod(Issue.class, "targetMilestone", Converters.trimAndIntern()));
        issueParser.addProcessor(issueTag.child("issue_type"),               Processors.setterMethod(Issue.class, "issueType", Converters.trimAndIntern()));
        issueParser.addProcessor(issueTag.child("creation_ts"),              Processors.setterMethod(Issue.class, "creationTimestamp", Converters.date(dateFormats)));
        issueParser.addProcessor(issueTag.child("qa_contact"),               Processors.setterMethod(Issue.class, "qAContact", Converters.trimAndIntern()));
        issueParser.addProcessor(issueTag.child("status_whiteboard"),        Processors.setterMethod(Issue.class, "statusWhiteboard", Converters.trim()));
        issueParser.addProcessor(issueTag.child("issue_file_loc"),           Processors.setterMethod(Issue.class, "fileLocation", Converters.trim()));
        issueParser.addProcessor(issueTag.child("votes"),                    Processors.setterMethod(Issue.class, "votes", Converters.trimAndIntern()));
        issueParser.addProcessor(issueTag.child("op_sys"),                   Processors.setterMethod(Issue.class, "operatingSystem", Converters.trimAndIntern()));
        issueParser.addProcessor(issueTag.child("short_desc"),               Processors.setterMethod(Issue.class, "shortDescription", Converters.trimAndIntern()));
        issueParser.addProcessor(issueTag.child("keywords"),                 Processors.addToCollection(Issue.class, "keywords", Converters.trimAndIntern(), Matchers.nonNullAndNonEmptyString()));
        issueParser.addProcessor(issueTag.child("cc"),                       Processors.addToCollection(Issue.class, "cC", Converters.trimAndIntern(), Matchers.nonNullAndNonEmptyString()));
        issueParser.addProcessor(issueTag.end(),                             new AddIssueToTargetListProcessor());

        // Parsing instructions for Description
        final XMLTagPath descriptionTag = issueTag.child("long_desc");
        issueParser.addProcessor(descriptionTag.start(),                     Processors.createNewObject(Description.class));
        issueParser.addProcessor(descriptionTag.child("who"),                Processors.setterMethod(Description.class, "who", Converters.trimAndIntern()));
        issueParser.addProcessor(descriptionTag.child("issue_when"),         Processors.setterMethod(Description.class, "when", Converters.date(dateFormats)));
        issueParser.addProcessor(descriptionTag.child("thetext"),            Processors.setterMethod(Description.class, "text", Converters.trim()));
        issueParser.addProcessor(descriptionTag.end(),                       Processors.addToCollection(Issue.class, "descriptions"));

        // Parsing instructions for Activity
        final XMLTagPath activityTag = issueTag.child("activity");
        issueParser.addProcessor(activityTag.start(),                        Processors.createNewObject(Activity.class));
        issueParser.addProcessor(activityTag.child("user"),                  Processors.setterMethod(Activity.class, "user", Converters.trimAndIntern()));
        issueParser.addProcessor(activityTag.child("when"),                  Processors.setterMethod(Activity.class, "when", Converters.date(dateFormats)));
        issueParser.addProcessor(activityTag.child("field_name"),            Processors.setterMethod(Activity.class, "field", Converters.trimAndIntern()));
        issueParser.addProcessor(activityTag.child("field_desc"),            Processors.setterMethod(Activity.class, "fieldDescription", Converters.trimAndIntern()));
        issueParser.addProcessor(activityTag.child("oldvalue"),              Processors.setterMethod(Activity.class, "oldValue", Converters.trimAndIntern()));
        issueParser.addProcessor(activityTag.child("newvalue"),              Processors.setterMethod(Activity.class, "newValue", Converters.trimAndIntern()));
        issueParser.addProcessor(activityTag.end(),                          Processors.addToCollection(Issue.class, "activities"));

        // Parsing instructions for Attachment
        final XMLTagPath attachmentTag = issueTag.child("attachment");
        issueParser.addProcessor(attachmentTag.start(),                      Processors.createNewObject(Attachment.class));
        issueParser.addProcessor(attachmentTag.child("mimetype"),            Processors.setterMethod(Attachment.class, "mimeType", Converters.trimAndIntern()));
        issueParser.addProcessor(attachmentTag.child("attachid"),            Processors.setterMethod(Attachment.class, "attachId", Converters.trimAndIntern()));
        issueParser.addProcessor(attachmentTag.child("date"),                Processors.setterMethod(Attachment.class, "date", Converters.date(dateFormats)));
        issueParser.addProcessor(attachmentTag.child("desc"),                Processors.setterMethod(Attachment.class, "description", Converters.trim()));
        issueParser.addProcessor(attachmentTag.child("ispatch"),             Processors.setterMethod(Attachment.class, "isPatch", Converters.trim()));
        issueParser.addProcessor(attachmentTag.child("filename"),            Processors.setterMethod(Attachment.class, "filename", Converters.trim()));
        issueParser.addProcessor(attachmentTag.child("submitter_id"),        Processors.setterMethod(Attachment.class, "submitterId", Converters.trimAndIntern()));
        issueParser.addProcessor(attachmentTag.child("submitting_username"), Processors.setterMethod(Attachment.class, "submitterUsername", Converters.trimAndIntern()));
        issueParser.addProcessor(attachmentTag.child("data"),                Processors.setterMethod(Attachment.class, "data", Converters.trim()));
        issueParser.addProcessor(attachmentTag.child("attachment_iz_url"),   Processors.setterMethod(Attachment.class, "attachmentIzUrl", Converters.trim()));
        issueParser.addProcessor(attachmentTag.end(),                        Processors.addToCollection(Issue.class, "attachments"));

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

        loadIssues(issuesList, "https://glazedlists.dev.java.net/issues/xml.cgi", Project.getProjects().get(0));
    }

    /**
     * Loads issues from the specified URL.
     */
    public static void loadIssues(EventList<Issue> target, String baseUrl, Project owner) throws IOException {
        int issuesPerRequest = 100;

        // continuously load issues until there's no more
        while (true) {
            // figure out how many to load
            int currentTotal = target.size();
            int nextTotal = currentTotal + issuesPerRequest;

            // assemble the issue ID argument
            StringBuffer idArg = new StringBuffer();
            for(int i = currentTotal + 1; i <= nextTotal; i++) {
                idArg.append(i);
                if(i < nextTotal) idArg.append(":");
            }

            // prepare a stream
            URL issuesUrl = new URL(baseUrl + "?include_attachments=false&id=" + idArg);
            InputStream issuesInStream = issuesUrl.openStream();

            // parse
            loadIssues(target, issuesInStream, owner);

            // if we couldn't load everything, we've consumed everything
            if (target.size() < nextTotal) return;
        }
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
    private static class PriorityConverter implements Converter<String,Priority> {
        public Priority convert(String value) {
            return Priority.lookupIssuzilla(value.trim());
        }
    }

    /**
     * This Processor adds a completely built Issue to the target EventList.
     * It also performs some late processing of the Issue, namely computing the
     * state changes of the Issue. This Processor only adds the Issue to the
     * target EventList if the status code is 200, indicating that the Issue
     * was loaded successfully.
     */
    private static class AddIssueToTargetListProcessor implements PopProcessor<EventList<Issue>,Issue> {
        private final Date loadingStarted = new Date();

        public void process(EventList<Issue> issues, Issue issue) {
            final String statusCode = issue.getStatusCode();

            // add the issue to the list if it was found okay
            if ("200".equals(statusCode)) {
                // compute the timeline of state changes now that we have loaded the entire Issue
                issue.getStateChanges().addAll(Issue.computeStateChanges(issue, loadingStarted));

                // add the Issue to the list of Issues
                issues.add(issue);
            }
        }
    }
}