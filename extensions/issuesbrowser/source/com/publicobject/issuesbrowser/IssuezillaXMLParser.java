/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.matchers.Matchers;
import com.publicobject.misc.xml.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Map;
import java.util.Date;

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
        final XMLTagPath startIssueTag = XMLTagPath.startTagPath("issuezilla issue");
        final XMLTagPath endIssueTag = startIssueTag.end();
        issueParser.addProcessor(startIssueTag,                                 Processors.createNewObject(Issue.class, new Class[] {Project.class}, new Object[] {project}));
        issueParser.addProcessor(endIssueTag.child("issue_id"),                 Processors.setterMethod(Issue.class, "id", Converters.integer()));
        issueParser.addProcessor(endIssueTag.child("issue_status"),             Processors.setterMethod(Issue.class, "status", Converters.trimAndIntern()));
        issueParser.addProcessor(endIssueTag.child("priority"),                 Processors.setterMethod(Issue.class, "priority", new PriorityConverter()));
        issueParser.addProcessor(endIssueTag.child("resolution"),               Processors.setterMethod(Issue.class, "resolution", Converters.trimAndIntern()));
        issueParser.addProcessor(endIssueTag.child("component"),                Processors.setterMethod(Issue.class, "component", Converters.trimAndIntern()));
        issueParser.addProcessor(endIssueTag.child("version"),                  Processors.setterMethod(Issue.class, "version", Converters.trimAndIntern()));
        issueParser.addProcessor(endIssueTag.child("rep_platform"),             Processors.setterMethod(Issue.class, "repPlatform", Converters.trimAndIntern()));
        issueParser.addProcessor(endIssueTag.child("assigned_to"),              Processors.setterMethod(Issue.class, "assignedTo", Converters.trimAndIntern()));
        issueParser.addProcessor(endIssueTag.child("delta_ts"),                 Processors.setterMethod(Issue.class, "deltaTimestamp", Converters.date(dateFormats)));
        issueParser.addProcessor(endIssueTag.child("subcomponent"),             Processors.setterMethod(Issue.class, "subcomponent", Converters.trimAndIntern()));
        issueParser.addProcessor(endIssueTag.child("reporter"),                 Processors.setterMethod(Issue.class, "reporter", Converters.trimAndIntern()));
        issueParser.addProcessor(endIssueTag.child("target_milestone"),         Processors.setterMethod(Issue.class, "targetMilestone", Converters.trimAndIntern()));
        issueParser.addProcessor(endIssueTag.child("issue_type"),               Processors.setterMethod(Issue.class, "issueType", Converters.trimAndIntern()));
        issueParser.addProcessor(endIssueTag.child("creation_ts"),              Processors.setterMethod(Issue.class, "creationTimestamp", Converters.date(dateFormats)));
        issueParser.addProcessor(endIssueTag.child("qa_contact"),               Processors.setterMethod(Issue.class, "qAContact", Converters.trimAndIntern()));
        issueParser.addProcessor(endIssueTag.child("status_whiteboard"),        Processors.setterMethod(Issue.class, "statusWhiteboard", Converters.trim()));
        issueParser.addProcessor(endIssueTag.child("issue_file_loc"),           Processors.setterMethod(Issue.class, "fileLocation", Converters.trim()));
        issueParser.addProcessor(endIssueTag.child("votes"),                    Processors.setterMethod(Issue.class, "votes", Converters.trimAndIntern()));
        issueParser.addProcessor(endIssueTag.child("op_sys"),                   Processors.setterMethod(Issue.class, "operatingSystem", Converters.trimAndIntern()));
        issueParser.addProcessor(endIssueTag.child("short_desc"),               Processors.setterMethod(Issue.class, "shortDescription", Converters.trimAndIntern()));
        issueParser.addProcessor(endIssueTag.child("keywords"),                 Processors.addToCollection(Issue.class, "keywords", Converters.trimAndIntern(), Matchers.nonNullAndNonEmptyString()));
        issueParser.addProcessor(endIssueTag.child("cc"),                       Processors.addToCollection(Issue.class, "cC", Converters.trimAndIntern(), Matchers.nonNullAndNonEmptyString()));
        issueParser.addProcessor(endIssueTag,                                   new AddIssueToTargetListProcessor());

        // Parsing instructions for Description
        final XMLTagPath startDescriptionTag = XMLTagPath.startTagPath("issuezilla issue long_desc");
        final XMLTagPath endDescriptionTag = startDescriptionTag.end();
        issueParser.addProcessor(startDescriptionTag,                           Processors.createNewObject(Description.class));
        issueParser.addProcessor(endDescriptionTag.child("who"),                Processors.setterMethod(Description.class, "who", Converters.trimAndIntern()));
        issueParser.addProcessor(endDescriptionTag.child("issue_when"),         Processors.setterMethod(Description.class, "when", Converters.date(dateFormats)));
        issueParser.addProcessor(endDescriptionTag.child("thetext"),            Processors.setterMethod(Description.class, "text", Converters.trim()));
        issueParser.addProcessor(endDescriptionTag,                             Processors.addToCollection(Issue.class, "descriptions"));

        // Parsing instructions for Activity
        final XMLTagPath startActivityTag = XMLTagPath.startTagPath("issuezilla issue activity");
        final XMLTagPath endActivityTag = startActivityTag.end();
        issueParser.addProcessor(startActivityTag,                              Processors.createNewObject(Activity.class));
        issueParser.addProcessor(endActivityTag.child("user"),                  Processors.setterMethod(Activity.class, "user", Converters.trimAndIntern()));
        issueParser.addProcessor(endActivityTag.child("when"),                  Processors.setterMethod(Activity.class, "when", Converters.date(dateFormats)));
        issueParser.addProcessor(endActivityTag.child("field_name"),            Processors.setterMethod(Activity.class, "field", Converters.trimAndIntern()));
        issueParser.addProcessor(endActivityTag.child("field_desc"),            Processors.setterMethod(Activity.class, "fieldDescription", Converters.trimAndIntern()));
        issueParser.addProcessor(endActivityTag.child("oldvalue"),              Processors.setterMethod(Activity.class, "oldValue", Converters.trimAndIntern()));
        issueParser.addProcessor(endActivityTag.child("newvalue"),              Processors.setterMethod(Activity.class, "newValue", Converters.trimAndIntern()));
        issueParser.addProcessor(endActivityTag,                                Processors.addToCollection(Issue.class, "activities"));

        // Parsing instructions for Attachment
        final XMLTagPath startAttachmentTag = XMLTagPath.startTagPath("issuezilla issue attachment");
        final XMLTagPath endAttachmentTag = startAttachmentTag.end();
        issueParser.addProcessor(startAttachmentTag,                            Processors.createNewObject(Attachment.class));
        issueParser.addProcessor(endAttachmentTag.child("mimetype"),            Processors.setterMethod(Attachment.class, "mimeType", Converters.trimAndIntern()));
        issueParser.addProcessor(endAttachmentTag.child("attachid"),            Processors.setterMethod(Attachment.class, "attachId", Converters.trimAndIntern()));
        issueParser.addProcessor(endAttachmentTag.child("date"),                Processors.setterMethod(Attachment.class, "date", Converters.date(dateFormats)));
        issueParser.addProcessor(endAttachmentTag.child("desc"),                Processors.setterMethod(Attachment.class, "description", Converters.trim()));
        issueParser.addProcessor(endAttachmentTag.child("ispatch"),             Processors.setterMethod(Attachment.class, "isPatch", Converters.trim()));
        issueParser.addProcessor(endAttachmentTag.child("filename"),            Processors.setterMethod(Attachment.class, "filename", Converters.trim()));
        issueParser.addProcessor(endAttachmentTag.child("submitter_id"),        Processors.setterMethod(Attachment.class, "submitterId", Converters.trimAndIntern()));
        issueParser.addProcessor(endAttachmentTag.child("submitting_username"), Processors.setterMethod(Attachment.class, "submitterUsername", Converters.trimAndIntern()));
        issueParser.addProcessor(endAttachmentTag.child("data"),                Processors.setterMethod(Attachment.class, "data", Converters.trim()));
        issueParser.addProcessor(endAttachmentTag.child("attachment_iz_url"),   Processors.setterMethod(Attachment.class, "attachmentIzUrl", Converters.trim()));
        issueParser.addProcessor(endAttachmentTag,                              Processors.addToCollection(Issue.class, "attachments"));

        // Parsing instructions for duplicate PeerIssues
        final XMLTagPath startHasDuplicatesTag = XMLTagPath.startTagPath("issuezilla issue has_duplicates");
        final XMLTagPath endHasDuplicatesTag = startHasDuplicatesTag.end();
        issueParser.addProcessor(startHasDuplicatesTag,                         Processors.createNewObject(PeerIssue.class));
        issueParser.addProcessor(endHasDuplicatesTag.child("issue_id"),         Processors.setterMethod(PeerIssue.class, "issueId", Converters.trimAndIntern()));
        issueParser.addProcessor(endHasDuplicatesTag.child("who"),              Processors.setterMethod(PeerIssue.class, "who", Converters.trimAndIntern()));
        issueParser.addProcessor(endHasDuplicatesTag.child("when"),             Processors.setterMethod(PeerIssue.class, "when", Converters.date(dateFormats)));
        issueParser.addProcessor(endHasDuplicatesTag,                           Processors.addToCollection(Issue.class, "duplicates"));

        // Parsing instructions for a duplicate PeerIssue
        final XMLTagPath startIsDuplicateTag = XMLTagPath.startTagPath("issuezilla issue is_duplicate");
        final XMLTagPath endIsDuplicateTag = startIsDuplicateTag.end();
        issueParser.addProcessor(startIsDuplicateTag,                           Processors.createNewObject(PeerIssue.class));
        issueParser.addProcessor(endIsDuplicateTag.child("issue_id"),           Processors.setterMethod(PeerIssue.class, "issueId", Converters.trimAndIntern()));
        issueParser.addProcessor(endIsDuplicateTag.child("who"),                Processors.setterMethod(PeerIssue.class, "who", Converters.trimAndIntern()));
        issueParser.addProcessor(endIsDuplicateTag.child("when"),               Processors.setterMethod(PeerIssue.class, "when", Converters.date(dateFormats)));
        issueParser.addProcessor(endIsDuplicateTag,                             Processors.setterMethod(Issue.class, "duplicate"));

        // Parsing instructions for a dependent PeerIssue
        final XMLTagPath startDependsOnTag = XMLTagPath.startTagPath("issuezilla issue dependson");
        final XMLTagPath endDependsOnTag = startDependsOnTag.end();
        issueParser.addProcessor(startDependsOnTag,                             Processors.createNewObject(PeerIssue.class));
        issueParser.addProcessor(endDependsOnTag.child("issue_id"),             Processors.setterMethod(PeerIssue.class, "issueId", Converters.trimAndIntern()));
        issueParser.addProcessor(endDependsOnTag.child("who"),                  Processors.setterMethod(PeerIssue.class, "who", Converters.trimAndIntern()));
        issueParser.addProcessor(endDependsOnTag.child("when"),                 Processors.setterMethod(PeerIssue.class, "when", Converters.date(dateFormats)));
        issueParser.addProcessor(endDependsOnTag,                               Processors.addToCollection(Issue.class, "dependsOn"));

        // Parsing instructions for a blocking PeerIssue
        final XMLTagPath startBlocksTag = XMLTagPath.startTagPath("issuezilla issue blocks");
        final XMLTagPath endBlocksTag = startBlocksTag.end();
        issueParser.addProcessor(startBlocksTag,                                Processors.createNewObject(PeerIssue.class));
        issueParser.addProcessor(endBlocksTag.child("issue_id"),                Processors.setterMethod(PeerIssue.class, "issueId", Converters.trimAndIntern()));
        issueParser.addProcessor(endBlocksTag.child("who"),                     Processors.setterMethod(PeerIssue.class, "who", Converters.trimAndIntern()));
        issueParser.addProcessor(endBlocksTag.child("when"),                    Processors.setterMethod(PeerIssue.class, "when", Converters.date(dateFormats)));
        issueParser.addProcessor(endBlocksTag,                                  Processors.addToCollection(Issue.class, "blocks"));

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
        createParser(owner).parse(target, source);
    }

    /**
     * This Converter can lookup Priority objects using Strings.
     */
    private static class PriorityConverter implements Converter {
        public Object convert(String value) {
            return Priority.lookup(value.trim());
        }
    }

    /**
     * This Processor adds a completely built Issue to the target EventList.
     * It also performs some late processing of the Issue, namely computing the
     * state changes of the Issue. This Processor only adds the Issue to the
     * target EventList if the status code is 200, indicating that the Issue
     * was loaded successfully.
     */
    private static class AddIssueToTargetListProcessor implements Processor {
        private static final Processor delegate = Processors.addObjectToTargetList();
        private static final XMLTagPath status_code_attribute = XMLTagPath.startTagPath("issuezilla issue").attribute("status_code");

        private final Date loadingStarted = new Date();

        public void process(XMLTagPath path, Map<XMLTagPath, Object> context) {
            final String status_code = (String) context.get(status_code_attribute);

            // add the issue to the list if it was found okay
            if ("200".equals(status_code)) {
                // locate the newly built object keyed by the starting tag of the current XMLTagPath
                final Issue currentIssue = (Issue) context.get(path.end());

                // compute the timeline of state changes now that we have loaded the entire Issue
                currentIssue.getStateChanges().addAll(Issue.computeStateChanges(currentIssue, loadingStarted));

                // add the Issue to the list of Issues
                delegate.process(path, context);
            }
        }
    }
}