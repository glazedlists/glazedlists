/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.demo;

import java.util.*;
import java.io.*;
// parse XML using SAX
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
// glazed lists
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.swing.*;

/**
 * An issue models a work effort either due to an existing problem or a desired
 * enhancement. This issue is identical to an IssueZilla issue as described by their
 * XML.
 *
 * <p>Parsing supports DTD revision 1.2 only and may not work with prior or
 * later versions of the issuezilla XML format.
 *
 * <p>Dates are currently unsupported and will always have a value of "null". 
 *
 * @see <a href="https://glazedlists.dev.java.net/issues/issuezilla.dtd">Issuezilla DTD</a>
 * 
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class Issue implements TextFilterable, Comparable {
    
    // mandatory issue fields
    private Integer id = null;
    private String status = null;
    private String priority = null;
    private String resolution = null;
    private String component = null;
    private String version = null;
    private String repPlatform = null;
    private String assignedTo = null;
    private Date deltaTimestamp = null;
    private String subcomponent = null;
    private String reporter = null;
    private String targetMilestone = null;
    private String issueType = null;
    private Date creationTimestamp = null;
    private String qaContact = null;
    private String statusWhiteboard = null;
    private String votes = null;
    private String operatingSystem = null;
    private String shortDescription = null;
    // optional fields
    private List keywords = new ArrayList();
    private List blocks = new ArrayList();
    private List dependsOn = new ArrayList();
    private List cc = new ArrayList();
    // issue lifecycle fields
    private List descriptions = new ArrayList();
    private List attachments = new ArrayList();
    private List activities = new ArrayList();
    
    private static SortedSet ISSUE_SIMPLE_FIELDS = new TreeSet();
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
        ISSUE_SIMPLE_FIELDS.add("votes");
        ISSUE_SIMPLE_FIELDS.add("op_sys");
        ISSUE_SIMPLE_FIELDS.add("short_desc");
        // optional
        ISSUE_SIMPLE_FIELDS.add("keywords");
        ISSUE_SIMPLE_FIELDS.add("blocks");
        ISSUE_SIMPLE_FIELDS.add("dependson");
        ISSUE_SIMPLE_FIELDS.add("cc");
    }
    private static SortedSet DESCRIPTION_SIMPLE_FIELDS = new TreeSet();
    static {
        DESCRIPTION_SIMPLE_FIELDS.add("who");
        DESCRIPTION_SIMPLE_FIELDS.add("issue_when");
        DESCRIPTION_SIMPLE_FIELDS.add("thetext");
    }
    private static SortedSet ATTACHMENT_SIMPLE_FIELDS = new TreeSet();
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
    private static SortedSet ACTIVITY_SIMPLE_FIELDS = new TreeSet();
    static {
        ACTIVITY_SIMPLE_FIELDS.add("user");
        ACTIVITY_SIMPLE_FIELDS.add("when");
        ACTIVITY_SIMPLE_FIELDS.add("field_name");
        ACTIVITY_SIMPLE_FIELDS.add("field_desc");
        ACTIVITY_SIMPLE_FIELDS.add("oldvalue");
        ACTIVITY_SIMPLE_FIELDS.add("newvalue");
    }
    
    /**
     * ID of this issue (unique key).
     */
    public Integer getId() {
        return id;
    }
    /**
     * Current status of this issue.
     */
    public String getStatus() {
        return status;
    }
    /**
     * Priority (severity) assigned to issue.
     */
    public String getPriority() {
        return priority;
    }
    /**
     * The issue's resolution, if any
     */
    public String getResolution() {
        return resolution;
    }
    /**
     * Product against which issue is reported.
     */
    public String getComponent() {
        return component;
    }
    /**
     * Version associated with component.
     */
    public String getVersion() {
        return version;
    }
    /**
     * Platform issue reported against (e.g. linux, etc.).
     */
    public String getRepPlatform() {
        return repPlatform;
    }
    /**
     * Email of person issue currently assigned to.
     */
    public String getAssignedTo() {
        return assignedTo;
    }
    /**
     * Last modified timestamp ('yyyy-mm-dd hh:mm:ss').
     */
    public Date getDeltaTimestamp() {
        return deltaTimestamp;
    }
    /**
     * Component of component issue reported against.
     */
    public String getSubcomponent() {
        return subcomponent;
    }
    /**
     * Email of initial issue reporter.
     */
    public String getReporter() {
        return reporter;
    }
    /**
     * Milestone for this issue's resolution.
     */
    public String getTargetMilestone() {
        return targetMilestone;
    }
    /**
     * Nature of issue, e.g. defect, task, etc.
     */
    public String getIssueType() {
        return issueType;
    }
    /**
     * Issue creation timestamp ('yyyy-mm-dd hh:mm:ss').
     */
    public Date getCreationTimestamp() {
        return creationTimestamp;
    }
    /**
     * Email of the QA contact for this issue.
     */
    public String getQAContact() {
        return qaContact;
    }
    /**
     * Free text 'whiteboard' for issue comments.
     */
    public String getStatusWhiteboard() {
        return statusWhiteboard;
    }
    /**
     * current votes for issue.
     */
    public String getVotes() {
        return votes;
    }
    /**
     * Operating system issue reported against.
     */
    public String getOperatingSystem() {
        return operatingSystem;
    }
    /**
     * Short description of issue.
     */
    public String getShortDescription() {
        return shortDescription;
    }
    /**
     * List of keywords for this issue. 
     */
    public List getKeywords() {
        return keywords;
    }
    /**
     * List of local issue IDs blocked by this one.
     */
    public List getBlocks() {
        return blocks;
    }
    /**
     * List of local issue IDs that depend on this one.
     */
    public List getDependsOn() {
        return dependsOn;
    }
    /**
     * List of email addresses of interested parties.s
     */
    public List getCC() {
        return cc;
    }
    /**
     * Data from the longdescs table for this issue id.  Essentially
     * the log of additional comments.       
     */
    public List getDescriptions() {
        return descriptions;
    }
    /**
     * 
     */
    public List getAttachments() {
        return attachments;
    }
    /**
     * 
     */
    public List getActivities() {
        return activities;
    }
    /**
     * 
     */
    public String toString() {
        return "Issue " + id + ": " + shortDescription;
    }
    
    /**
     * Compares two issues by ID. 
     */
    public int compareTo(Object other) {
        if(other == null) return -1;
        Issue otherIssue = (Issue)other;
        return id.compareTo(otherIssue.id);
    }
    
    /**
     * Gets the strings to filter this issue by.
     */
    public void getFilterStrings(List baseList) {
        baseList.add(status);
        baseList.add(priority);
        baseList.add(resolution);
        baseList.add(component);
        baseList.add(version);
        baseList.add(repPlatform);
        baseList.add(assignedTo);
        baseList.add(subcomponent);
        baseList.add(reporter);
        baseList.add(targetMilestone);
        baseList.add(issueType);
        baseList.add(qaContact);
        baseList.add(statusWhiteboard);
        baseList.add(operatingSystem);
        baseList.add(shortDescription);
    }

    /**
     * Parses the Issuezilla XML document on the specified input stream into a List
     * of Issues.
     */
    public static List parseIssuezillaXML(InputStream source) throws IOException {
        try {
            IssueHandler issueReader = new IssueHandler();
            SAXParserFactory.newInstance().newSAXParser().parse(source, issueReader);
            return issueReader.getIssues();
        } catch(SAXException e) {
            e.printStackTrace();
            throw new IOException("Parsing failed " + e.getMessage());
        } catch(ParserConfigurationException e) {
            e.printStackTrace();
            throw new IOException("Parsing failed " + e.getMessage());
        }
    }
    
    /**
     * When executed, this opens a file specified on the command line, parses
     * it for Issuezilla XML and writes the issues to the command line.
     */
    public static void main(String[] args) {
        List issues = new ArrayList();
        try {
            issues = parseIssuezillaXML(new FileInputStream(args[0]));
        } catch(IOException e) {
            e.printStackTrace();
            return;
        }

        for(Iterator i = issues.iterator(); i.hasNext(); ) {
            System.out.println(i.next());
        }
    }

    
    /**
     * An additional comment.       
     */
    static class Description {
        private String who = null;
        private Date when = null;
        private String text = null;
        /**
         * Email of person posting long_desc.
         */
        public String getWho() {
            return who;
        }
        /**
         * Timestamp when long_desc added ('yyy-mm-dd hh:mm')
         */
        public Date getWhen() {
            return when;
        }
        /**
         * Free text that comprises the long desc.
         */
        public String getText() {
            return text;
        }
    }
    /**
     * Data pertaining to the issue's activity record.
     */
    static class Activity {
        private String user = null;
        private Date when = null;
        private String field = null;
        private String fieldDescription = null;
        private String oldValue = null;
        private String newValue = null;
        /**
         * user who performed the action
         */
        public String getUser() {
            return user;
        }
        /**
         * date the described change was made
         */
        public Date getWhen() {
            return when;
        }
        /**
         * name of db field (in fielddefs) 
         */
        public String getField() {
            return field;
        }
        /**
         * description of the database field
         */
        public String getFieldDescription() {
            return fieldDescription;
        }
        /**
         * value changed from
         */
        public String getOldValue() {
            return oldValue;
        }
        /**
         * value changed to 
         */
        public String getNewValue() {
            return newValue;
        }
    }

    /**
     * Data pertaining to attachments.  NOTE - some of these fields
     * are currently unimplemented (ispatch, filename, etc.).
     */
    static class Attachment {
        private String mimeType = null;
        private String attachId = null;
        private Date date = null;
        private String description = null;
        private String isPatch = null;
        private String filename = null;
        private String submitterId = null;
        private String submitterUsername = null;
        private String data = null;
        private String attachmentIzUrl = null;
        /**
         * Mime type for the attachment.
         */
        public String getMimeType() {
            return mimeType;
        }
        /**
         * A unique id for this attachment.
         */
        public String getAttachId() {
            return attachId;
        }
        /**
         * Timestamp of when added 'yyyy-mm-dd hh:mm'
         */
        public Date getDate() {
            return date;
        }
        /**
         * Short description for attachment.
         */
        public String getDescription() {
            return description;
        }
        /**
         * Whether attachment is a patch file.
         */
        public String getIsPatch() {
            return isPatch;
        }
        /**
         * Filename of attachment.
         */
        public String getFilename() {
            return filename;
        }
        /**
         * Issuezilla ID of attachement submitter.
         */
        public String getSubmitterId() {
            return submitterId;
        }
        /**
         * username of attachement submitter.
         */
        public String getSubmitterUsername() {
            return submitterUsername;
        }
        /**
         * Encoded attachment.
         */
        public String getData() {
            return data;
        }
        /**
         * URL to attachment in iz.
         */
        public String getAttachmentIzUrl() {
            return attachmentIzUrl;
        }
    }
    
    /**
     * The IssueHandler does the real parsing.
     */
    static class IssueHandler extends AbstractSimpleElementHandler {
        private List issues = new ArrayList();
        private Issue currentIssue;
        private AbstractSimpleElementHandler simpleElementHandler = null;
        
        public IssueHandler() {
            super(null, "issue", Issue.ISSUE_SIMPLE_FIELDS);
            parent = this;
        }
        
        /**
         * Gets the list of issues parsed by this handler.
         */
        public List getIssues() {
            return issues;
        }
        
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            // ignore issuezilla tags
            if(qName.equals("issuezilla")) {
                // ignore
            // create a new issue
            } else if(currentIssue == null) {
                if(qName.equals("issue")) {
                    currentIssue = new Issue();
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
                issues.add(currentIssue);
                currentIssue = null;
            // ignore issuezilla tags
            } else if(qName.equals("issuezilla")) {
                // ignore
            // handle all other cases in an error
            } else {
                addException(this + " encountered unexpected end of element \"" + qName + "\"");
            }
        }
        public void addFieldAndValue(String currentField, String value) {
            if(currentField.equals("issue_id")) currentIssue.id = Integer.valueOf(value); 
            else if(currentField.equals("issue_status")) currentIssue.status = value; 
            else if(currentField.equals("priority")) currentIssue.priority = value;
            else if(currentField.equals("resolution")) currentIssue.resolution = value;
            else if(currentField.equals("component")) currentIssue.component = value;
            else if(currentField.equals("version")) currentIssue.version = value;
            else if(currentField.equals("rep_platform")) currentIssue.repPlatform = value;
            else if(currentField.equals("assigned_to")) currentIssue.assignedTo = value;
            else if(currentField.equals("delta_ts")) currentIssue.deltaTimestamp = null;
            else if(currentField.equals("subcomponent")) currentIssue.subcomponent = value;
            else if(currentField.equals("reporter")) currentIssue.reporter = value;
            else if(currentField.equals("target_milestone")) currentIssue.targetMilestone = value;
            else if(currentField.equals("issue_type")) currentIssue.issueType = value;
            else if(currentField.equals("creation_ts")) currentIssue.creationTimestamp = null;
            else if(currentField.equals("qa_contact")) currentIssue.qaContact = value;
            else if(currentField.equals("status_whiteboard")) currentIssue.statusWhiteboard = value;
            else if(currentField.equals("votes")) currentIssue.votes = value;
            else if(currentField.equals("op_sys")) currentIssue.operatingSystem = value;
            else if(currentField.equals("short_desc")) currentIssue.shortDescription = value;
            else if(currentField.equals("keywords")) currentIssue.keywords.add(value);
            else if(currentField.equals("blocks")) currentIssue.blocks.add(value);
            else if(currentField.equals("dependson")) currentIssue.dependsOn.add(value);
            else if(currentField.equals("cc")) currentIssue.cc.add(value);
            else parent.addException(this + " encountered unexpected element " + currentField);
        }
        public void endSimpleElement() {
            if(simpleElementHandler.hostElement.equals("long_desc")) {
                DescriptionHandler descriptionHandler = (DescriptionHandler)simpleElementHandler;
                currentIssue.getDescriptions().add(descriptionHandler.description);
                simpleElementHandler = null;
            } else if(simpleElementHandler.hostElement.equals("attachment")) {
                AttachmentHandler attachmentHandler = (AttachmentHandler)simpleElementHandler;
                currentIssue.getAttachments().add(attachmentHandler.attachment);
                simpleElementHandler = null;
            } else if(simpleElementHandler.hostElement.equals("activity")) {
                ActivityHandler activityHandler = (ActivityHandler)simpleElementHandler;
                currentIssue.getActivities().add(activityHandler.activity);
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
     * A simple class for parsing issuezilla element blocks that contain no
     * smaller blocks within.
     */
    static abstract class AbstractSimpleElementHandler extends DefaultHandler {
        protected String currentField = null;
        protected IssueHandler parent;
        protected String hostElement = null;
        private Set acceptableFields = null;
        private StringBuffer currentValue;
        protected AbstractSimpleElementHandler(IssueHandler parent, String hostElement, Set acceptableFields) {
            this.parent = parent;
            this.hostElement = hostElement;
            this.acceptableFields = acceptableFields;
        }
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
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
                addFieldAndValue(currentField, currentValue.toString());
                currentField = null;
                currentValue = null;
            } else {
                parent.addException(this + " expected end of element " + currentField + " but found end of " + qName);
            }
        }
        public void characters(char[] ch, int start, int length) {
            if(currentField == null) return;
            currentValue.append(ch, start, length);
        }
        protected abstract void addFieldAndValue(String currentField, String value);
        public String toString() {
            return "<" + hostElement + ">";
        }
    }
    
    /**
     * Handles nested description tags.
     */
    static class DescriptionHandler extends AbstractSimpleElementHandler {
        public Description description;
        public DescriptionHandler(IssueHandler parent) {
            super(parent, "long_desc", Issue.DESCRIPTION_SIMPLE_FIELDS);
            description = new Description();
        }
        public void addFieldAndValue(String currentField, String value) {
            if(currentField.equals("who")) description.who = value; 
            else if(currentField.equals("issue_when")) description.when = null; 
            else if(currentField.equals("thetext")) description.text = value;
            else parent.addException(this + " encountered unexpected element " + currentField);
        }
    }
    
    /**
     * Handles nested attachment tags.
     */
    static class AttachmentHandler extends AbstractSimpleElementHandler {
        public Attachment attachment;
        public AttachmentHandler(IssueHandler parent) {
            super(parent, "attachment", Issue.ATTACHMENT_SIMPLE_FIELDS);
            attachment = new Attachment();
        }
        public void addFieldAndValue(String currentField, String value) {
            if(currentField.equals("mimetype")) attachment.mimeType = value; 
            else if(currentField.equals("attachid")) attachment.attachId = value; 
            else if(currentField.equals("date")) attachment.date = null;
            else if(currentField.equals("desc")) attachment.description = value;
            else if(currentField.equals("ispatch")) attachment.isPatch = value;
            else if(currentField.equals("filename")) attachment.filename = value;
            else if(currentField.equals("submitter_id")) attachment.submitterId = value;
            else if(currentField.equals("submitting_username")) attachment.submitterUsername = value;
            else if(currentField.equals("data")) attachment.data = value;
            else if(currentField.equals("attachment_iz_url")) attachment.attachmentIzUrl = value;
            else parent.addException(this + " encountered unexpected element " + currentField);
        }
    }
    
    
    /**
     * Handles nested activity tags.
     */
    static class ActivityHandler extends AbstractSimpleElementHandler {
        public Activity activity;
        public ActivityHandler(IssueHandler parent) {
            super(parent, "activity", Issue.ACTIVITY_SIMPLE_FIELDS);
            activity = new Activity();
        }
        public void addFieldAndValue(String currentField, String value) {
            if(currentField.equals("user")) activity.user = value; 
            else if(currentField.equals("when")) activity.when = null; 
            else if(currentField.equals("field_name")) activity.field = value;
            else if(currentField.equals("field_desc")) activity.fieldDescription = value;
            else if(currentField.equals("oldvalue")) activity.oldValue = value;
            else if(currentField.equals("newvalue")) activity.newValue = value;
            else parent.addException(this + " encountered unexpected element " + currentField);
        }
    }
}
