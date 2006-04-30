/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Parses IssueZilla issue as described by their XML.
 * <p/>
 * <p>Parsing supports DTD revision 1.2 only and may not work with prior or
 * later versions of the issuezilla XML format.
 * <p/>
 * <p>Dates are currently unsupported and will always have a value of "null".
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 * @see <a href="https://glazedlists.dev.java.net/issues/issuezilla.dtd">Issuezilla DTD</a>
 */
public class IssuezillaXMLParser {

    /** the date format for "issue_when" is supposed to be 'yyyy-MM-dd HH:mm' but is actually 'yyyy-MM-dd HH:mm:ss' */
    private static final SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /** the date format for "delta_ts" is supposed to be 'yyyy-MM-dd HH:mm' but is actually 'yyyyMMddHHmmss' */
    private static final SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyyMMddHHmmss");

    // hardcode the servers in California
    static {
        dateFormat1.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        dateFormat2.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
    }

    private static Set<String> ISSUE_SIMPLE_FIELDS = new HashSet<String>();
    static {
        ISSUE_SIMPLE_FIELDS.add("issue_id");
        ISSUE_SIMPLE_FIELDS.add("issue_status");
        ISSUE_SIMPLE_FIELDS.add("priority");
        ISSUE_SIMPLE_FIELDS.add("resolution");
        ISSUE_SIMPLE_FIELDS.add("component");
        ISSUE_SIMPLE_FIELDS.add("version");
        ISSUE_SIMPLE_FIELDS.add("rep_platform");
        ISSUE_SIMPLE_FIELDS.add("assigned_to");
        ISSUE_SIMPLE_FIELDS.add("delta_ts");
        ISSUE_SIMPLE_FIELDS.add("subcomponent");
        ISSUE_SIMPLE_FIELDS.add("reporter");
        ISSUE_SIMPLE_FIELDS.add("target_milestone");
        ISSUE_SIMPLE_FIELDS.add("issue_type");
        ISSUE_SIMPLE_FIELDS.add("creation_ts");
        ISSUE_SIMPLE_FIELDS.add("qa_contact");
        ISSUE_SIMPLE_FIELDS.add("status_whiteboard");
        ISSUE_SIMPLE_FIELDS.add("issue_file_loc");
        ISSUE_SIMPLE_FIELDS.add("votes");
        ISSUE_SIMPLE_FIELDS.add("op_sys");
        ISSUE_SIMPLE_FIELDS.add("short_desc");
        // optional
        ISSUE_SIMPLE_FIELDS.add("keywords");
        ISSUE_SIMPLE_FIELDS.add("cc");
    }

    private static SortedSet<String> DESCRIPTION_SIMPLE_FIELDS = new TreeSet<String>();
    static {
        DESCRIPTION_SIMPLE_FIELDS.add("who");
        DESCRIPTION_SIMPLE_FIELDS.add("issue_when");
        DESCRIPTION_SIMPLE_FIELDS.add("thetext");
    }

    private static SortedSet<String> ATTACHMENT_SIMPLE_FIELDS = new TreeSet<String>();
    static {
        ATTACHMENT_SIMPLE_FIELDS.add("mimetype");
        ATTACHMENT_SIMPLE_FIELDS.add("attachid");
        ATTACHMENT_SIMPLE_FIELDS.add("date");
        ATTACHMENT_SIMPLE_FIELDS.add("desc");
        ATTACHMENT_SIMPLE_FIELDS.add("ispatch");
        ATTACHMENT_SIMPLE_FIELDS.add("filename");
        ATTACHMENT_SIMPLE_FIELDS.add("submitter_id");
        ATTACHMENT_SIMPLE_FIELDS.add("submitting_username");
        ATTACHMENT_SIMPLE_FIELDS.add("data");
        ATTACHMENT_SIMPLE_FIELDS.add("attachment_iz_url");
    }

    private static SortedSet<String> ACTIVITY_SIMPLE_FIELDS = new TreeSet<String>();
    static {
        ACTIVITY_SIMPLE_FIELDS.add("user");
        ACTIVITY_SIMPLE_FIELDS.add("when");
        ACTIVITY_SIMPLE_FIELDS.add("field_name");
        ACTIVITY_SIMPLE_FIELDS.add("field_desc");
        ACTIVITY_SIMPLE_FIELDS.add("oldvalue");
        ACTIVITY_SIMPLE_FIELDS.add("newvalue");
    }

    private static SortedSet<String> RELATIONSHIP_SIMPLE_FIELDS = new TreeSet<String>();
    static {
        RELATIONSHIP_SIMPLE_FIELDS.add("issue_id");
        RELATIONSHIP_SIMPLE_FIELDS.add("who");
        RELATIONSHIP_SIMPLE_FIELDS.add("when");
    }

    /**
     * When executed, this opens a file specified on the command line, parses
     * it for Issuezilla XML and writes the issues to the command line.
     */
    public static void main(String[] args) throws IOException {
        if(args.length != 1) {
            System.out.println("Usage: IssuezillaXMLParser <file>");
            return;
        }

        EventList<Issue> issuesList = new BasicEventList<Issue>();
        loadIssues(issuesList, new FileInputStream(args[0]), new Project(null, null));
        System.out.println(issuesList);
    }

    /**
     * Loads issues from the specified URL.
     */
    public static void loadIssues(EventList<Issue> target, String baseUrl, Project owner) throws IOException {
        int issuesPerRequest = 100;

        // continuously load issues until there's no more
        while(true) {
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
            URL issuesUrl = new URL(baseUrl + "?id=" + idArg);
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
        try {
            // configure a SAX parser
            XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            IssuezillaParserSidekick parserSidekick = new IssuezillaParserSidekick();
            xmlReader.setEntityResolver(parserSidekick);
            xmlReader.setErrorHandler(parserSidekick);
            xmlReader.setContentHandler(new IssueHandler(target, owner));

            // parse away
            xmlReader.parse(new InputSource(source));
        } catch(SAXException e) {
            e.printStackTrace();
            throw new IOException("Parsing failed " + e.getMessage());
        } catch(ParserConfigurationException e) {
            e.printStackTrace();
            throw new IOException("Parsing failed " + e.getMessage());
        }
    }

    /**
     * ParserSidekick performs various services for the SaxParser.
     * It doesn't print exceptions caused by InterruptedException since this parser
     * is frequently interrupted intentionally. It also skips DTD validation for
     * a significant performance boost.
     *
     * @see <a href="http://forum.java.sun.com/thread.jspa?forumID=34&threadID=284209">Java Forums</a>
     */
    static class IssuezillaParserSidekick implements EntityResolver, ErrorHandler {
        /**
         * Don't fetch a DTD from a remote webserver.
         */
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            // skip the DTD
            if(systemId.endsWith("issuezilla.dtd")) {
                byte[] emptyDTDBytes = "<?xml version='1.0' encoding='UTF-8'?>".getBytes();
                return new InputSource(new ByteArrayInputStream(emptyDTDBytes));
            } else {
                return null;
            }
        }
        public void error(SAXParseException exception) {
            System.out.println("Error: ");
            exception.printStackTrace();
        }
        public void fatalError(SAXParseException exception) {
            System.out.println("Fatal error: ");
            exception.printStackTrace();
        }
        public void warning(SAXParseException exception) {
            System.out.println("Warning: ");
            exception.printStackTrace();
        }
    }


    /**
     * The IssueHandler does the real parsing.
     */
    static class IssueHandler extends AbstractSimpleElementHandler {
        private EventList<Issue> issues = null;
        private Issue currentIssue;
        private AbstractSimpleElementHandler simpleElementHandler = null;
        private Project owner;
        private final Date loadingStarted = new Date();

        public IssueHandler(EventList<Issue> issues, Project owner) {
            super(null, "issue", ISSUE_SIMPLE_FIELDS);
            this.owner = owner;
            this.issues = issues;
            parent = this;
        }

        /**
         * Gets the list of issues parsed by this handler.
         */
        public List getIssues() {
            return issues;
        }

        public void startElement(String uri, String localName, String qName,
            Attributes attributes) {
            // ignore issuezilla tags
            if(qName.equals("issuezilla")) {
                // ignore
            // create a new issue
            } else if(currentIssue == null) {
                if(qName.equals("issue")) {
                    currentIssue = new Issue(owner);

                    // save the status code
                    String statusCode = attributes.getValue("status_code");
                    currentIssue.setStatusCode(statusCode != null ? Integer.valueOf(statusCode) : new Integer(404));

                } else {
                    addException(this + " encountered unexpected element \"" + qName + "\"");
                }
            // attempt to delegate to a specialized handler
            } else if(simpleElementHandler != null) {
                simpleElementHandler.startElement(uri, localName, qName, attributes);
            // handle a sophisticated description field
            } else if(qName.equals("long_desc")) {
                simpleElementHandler = new DescriptionHandler(this);
            // handle a sophisticated attachment field
            } else if(qName.equals("attachment")) {
                simpleElementHandler = new AttachmentHandler(this);
            // handle a sophisticated activity field
            } else if(qName.equals("activity")) {
                simpleElementHandler = new ActivityHandler(this);
            // handle a sophisticated duplicate field
            } else if(qName.equals("has_duplicates")) {
                simpleElementHandler = new RelationshipHandler(this, "has_duplicates");
            } else if(qName.equals("dependson")) {
                simpleElementHandler = new RelationshipHandler(this, "dependson");
            } else if(qName.equals("blocks")) {
                simpleElementHandler = new RelationshipHandler(this, "blocks");
            } else if(qName.equals("is_duplicate")) {
                simpleElementHandler = new RelationshipHandler(this, "is_duplicate");
            // handle all other cases in the base class
            } else {
                super.startElement(uri, localName, qName, attributes);
            }
        }

        public void endElement(String uri, String localName, String qName) {
            // if we are within a simple field
            if(currentField != null) {
                super.endElement(uri, localName, qName);
            // if we can delegate to a specialized handler
            } else if(simpleElementHandler != null) {
                simpleElementHandler.endElement(uri, localName, qName);
            // if this is the end of an issue
            } else if(qName.equals("issue")) {
                // break out if this request is cancelled
                if(Thread.interrupted()) throw new RuntimeException(new InterruptedException());

                // add the issue to the list if it was found okay
                if(currentIssue.getStatusCode() != null && currentIssue.getStatusCode().intValue() == 200) {
                    // compute the timeline of state changes now that we have loaded the entire Issue
                    currentIssue.getStateChanges().addAll(Issue.computeStateChanges(currentIssue, loadingStarted));

                    // add the Issue to the list of Issues
                    issues.add(currentIssue);
                }

                // prepare for the next issue
                currentIssue = null;

            // ignore issuezilla tags
            } else if(qName.equals("issuezilla")) {
                // do nothing

            // handle all other cases in an error
            } else {
                addException(this + " encountered unexpected end of element \"" + qName + "\"");
            }
        }

        public void characters(char[] ch, int start, int length) {
            // if we can delegate to a specialized handler
            if(simpleElementHandler != null) {
                simpleElementHandler.characters(ch, start, length);
            } else {
                super.characters(ch, start, length);
            }
        }

        public void addFieldAndValue(String currentField, String value) {
            try {
                if(currentField == "issue_id") currentIssue.setId(Integer.valueOf(value));
                else if(currentField == "issue_status") currentIssue.setStatus(value.intern());
                else if(currentField == "priority") currentIssue.setPriority(Priority.lookup(value));
                else if(currentField == "resolution") currentIssue.setResolution(value.intern());
                else if(currentField == "component") currentIssue.setComponent(value);
                else if(currentField == "version") currentIssue.setVersion(value);
                else if(currentField == "rep_platform") currentIssue.setRepPlatform(value);
                else if(currentField == "assigned_to") currentIssue.setAssignedTo(value);
                else if(currentField == "delta_ts") currentIssue.setDeltaTimestamp(parse(value));
                else if(currentField == "subcomponent") currentIssue.setSubcomponent(value);
                else if(currentField == "reporter") currentIssue.setReporter(value);
                else if(currentField == "target_milestone") currentIssue.setTargetMilestone(value);
                else if(currentField == "issue_type") currentIssue.setIssueType(value.intern());
                else if(currentField == "creation_ts") currentIssue.setCreationTimestamp(parse(value));
                else if(currentField == "qa_contact") currentIssue.setQAContact(value);
                else if(currentField == "status_whiteboard") currentIssue.setStatusWhiteboard(value);
                else if(currentField == "issue_file_loc") currentIssue.setFileLocation(value);
                else if(currentField == "votes") currentIssue.setVotes(value);
                else if(currentField == "op_sys") currentIssue.setOperatingSystem(value);
                else if(currentField == "short_desc") currentIssue.setShortDescription(value);
                else if(currentField == "keywords") currentIssue.getKeywords().add(value);
                else if(currentField == "cc") currentIssue.getCC().add(value);
                else parent.addException(this + " encountered unexpected element " + currentField);
            } catch (ParseException pe) {
                parent.addException(this + " unable to parse date in field " + currentField + ": " + value);
            }
        }

        public void endSimpleElement() {
            if (simpleElementHandler.hostElement.equals("long_desc")) {
                DescriptionHandler descriptionHandler = (DescriptionHandler)simpleElementHandler;
                currentIssue.getDescriptions().add(descriptionHandler.description);
                simpleElementHandler = null;
            } else if (simpleElementHandler.hostElement.equals("attachment")) {
                AttachmentHandler attachmentHandler = (AttachmentHandler)simpleElementHandler;
                currentIssue.getAttachments().add(attachmentHandler.attachment);
                simpleElementHandler = null;
            } else if (simpleElementHandler.hostElement.equals("activity")) {
                ActivityHandler activityHandler = (ActivityHandler)simpleElementHandler;
                currentIssue.getActivities().add(activityHandler.activity);
                simpleElementHandler = null;
            } else if (simpleElementHandler.hostElement.equals("has_duplicates")) {
                RelationshipHandler duplicateHandler = (RelationshipHandler)simpleElementHandler;
                currentIssue.getDuplicates().add(duplicateHandler.peerIssue);
                simpleElementHandler = null;
            } else if (simpleElementHandler.hostElement.equals("dependson")) {
                RelationshipHandler dependsOnHandler = (RelationshipHandler)simpleElementHandler;
                currentIssue.getDependsOn().add(dependsOnHandler.peerIssue);
                simpleElementHandler = null;
            } else if (simpleElementHandler.hostElement.equals("blocks")) {
                RelationshipHandler blocksHandler = (RelationshipHandler)simpleElementHandler;
                currentIssue.getBlocks().add(blocksHandler.peerIssue);
                simpleElementHandler = null;
            } else if (simpleElementHandler.hostElement.equals("is_duplicate")) {
                RelationshipHandler isDuplicateHandler = (RelationshipHandler)simpleElementHandler;
                currentIssue.setDuplicate(isDuplicateHandler.peerIssue);
                simpleElementHandler = null;
            } else {
                addException(this + " encountered unexpected end of element " + simpleElementHandler);
            }
        }

        public void addException(String message) {
            new Exception(message).printStackTrace();
        }
    }

    /**
     * Parse the specified date.
     */
    private static Date parseLenient(String date) {
        try {
            return parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Parse the specified date.
     */
    private static Date parse(String date) throws ParseException {
        try {
            return dateFormat1.parse(date);
        } catch (ParseException e) {
            // try another format
        }

        try {
            return dateFormat2.parse(date);
        } catch (ParseException e) {
            throw e;
        }
    }


    /**
     * Handles nested description tags.
     */
    static class DescriptionHandler extends AbstractSimpleElementHandler {
        public Description description;
        public DescriptionHandler(IssueHandler parent) {
            super(parent, "long_desc", DESCRIPTION_SIMPLE_FIELDS);
            description = new Description();
        }
        public void addFieldAndValue(String currentField, String value) {
            if(currentField == "who") description.setWho(value);
            else if(currentField == "issue_when") description.setWhen(parseLenient(value));
            else if(currentField == "thetext") description.setText(value);
            else parent.addException(this + " encountered unexpected element " + currentField);
        }
    }


    /**
     * Handles nested attachment tags.
     */
    static class AttachmentHandler extends AbstractSimpleElementHandler {
        public Attachment attachment;
        public AttachmentHandler(IssueHandler parent) {
            super(parent, "attachment", ATTACHMENT_SIMPLE_FIELDS);
            attachment = new Attachment();
        }
        public void addFieldAndValue(String currentField, String value) {
            if(currentField == "mimetype") attachment.setMimeType(value);
            else if(currentField == "attachid") attachment.setAttachId(value);
            else if(currentField == "date") attachment.setDate(parseLenient(value));
            else if(currentField == "desc") attachment.setDescription(value);
            else if(currentField == "ispatch") attachment.setIsPatch(value);
            else if(currentField == "filename") attachment.setFilename(value);
            else if(currentField == "submitter_id") attachment.setSubmitterId(value);
            else if(currentField == "submitting_username") attachment.setSubmitterUsername(value);
            else if(currentField == "data") attachment.setData(value);
            else if(currentField == "attachment_iz_url") attachment.setAttachmentIzUrl(value);
            else parent.addException(this + " encountered unexpected element " + currentField);
        }
    }


    /**
     * Handles nested activity tags.
     */
    static class ActivityHandler extends AbstractSimpleElementHandler {
        public Activity activity;
        public ActivityHandler(IssueHandler parent) {
            super(parent, "activity", ACTIVITY_SIMPLE_FIELDS);
            activity = new Activity();
        }
        public void addFieldAndValue(String currentField, String value) {
            if(currentField == "user") activity.setUser(value);
            else if(currentField == "when") activity.setWhen(parseLenient(value));
            else if(currentField == "field_name") activity.setField(value);
            else if(currentField == "field_desc") activity.setFieldDescription(value);
            else if(currentField == "oldvalue") activity.setOldValue(value);
            else if(currentField == "newvalue") activity.setNewValue(value);
            else parent.addException(this + " encountered unexpected element " + currentField);
        }
    }


    /**
     * Handles nested dependson, blocks, is_duplicate and has_duplicates tags.
     */
    static class RelationshipHandler extends AbstractSimpleElementHandler {
        public PeerIssue peerIssue;
        /**
         * @param type either 'dependson' or 'hasduplicate'
         */
        public RelationshipHandler(IssueHandler parent, String type) {
            super(parent, type, RELATIONSHIP_SIMPLE_FIELDS);
            peerIssue = new PeerIssue();
        }
        public void addFieldAndValue(String currentField, String value) {
            if(currentField == "issue_id") peerIssue.setIssueId(value);
            else if(currentField == "who") peerIssue.setWho(value);
            else if(currentField == "when") peerIssue.setWhen(parseLenient(value));
            else parent.addException(this + " encountered unexpected element " + currentField);
        }
    }

    /**
     * Convert the specified literal String to a String that the Java compiler
     * can parse.
     */
    private static String escapeToJava(String text) {
        StringBuffer result = new StringBuffer();
        for(int c = 0; c < text.length(); c++) {
            char letter = text.charAt(c);
            if (letter == '\"') result.append("\\\"");
            else if (letter == '\\') result.append("\\\\");
            else if (letter == '\n') result.append("\\n");
            else result.append(letter);
        }
        return result.toString();
    }


    /**
     * A simple class for parsing issuezilla element blocks that contain no
     * smaller blocks within.
     */
    static abstract class AbstractSimpleElementHandler extends DefaultHandler {
        protected String currentField = null;
        protected IssueHandler parent;
        protected String hostElement = null;
        private Set acceptableFields = null;
        private StringBuffer currentValue;

        protected AbstractSimpleElementHandler(IssueHandler parent, String hostElement,
            Set acceptableFields) {
            this.parent = parent;
            this.hostElement = hostElement;
            this.acceptableFields = acceptableFields;
        }

        public void startElement(String uri, String localName, String qName,
            Attributes attributes) {
            if(currentField != null) {
                parent.addException(this + " expected end of element " + currentField + " but found " + qName);
            } else if(acceptableFields.contains(qName)) {
                currentField = qName;
                currentValue = new StringBuffer();
            } else {
                parent.addException(this + " encountered unexpected element " + qName);
            }
        }

        public void endElement(String uri, String localName, String qName) {
            if(qName.equals(hostElement)) {
                parent.endSimpleElement();
            } else if(currentField == null) {
                parent.addException(this + " expected new element but found end of " + qName);
            } else if(currentField.equals(qName)) {
                addFieldAndValue(currentField, currentValue.toString().intern());
                currentField = null;
                currentValue = null;
            } else {
                parent.addException(this + " expected end of element " + currentField + " but found end of " + qName);
            }
        }

        public void characters(char[] ch, int start, int length) {
            if (currentField == null) return;
            currentValue.append(ch, start, length);
        }

        protected abstract void addFieldAndValue(String currentField, String value);

        public String toString() {
            return "<" + hostElement + ">";
        }
    }
}