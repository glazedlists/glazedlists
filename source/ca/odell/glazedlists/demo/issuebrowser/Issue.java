/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.demo.issuebrowser;

import java.util.*;
// glazed lists
import ca.odell.glazedlists.*;


/**
 * An issue models a work effort either due to an existing problem or a desired
 * enhancement.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class Issue implements TextFilterable, Comparable {

    // mandatory issue fields
    private Integer id = null;
    private Integer statusCode = null;
    private String status = null;
    private Priority priority = null;
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
    private String fileLocation = null;
    private String votes = null;
    private String operatingSystem = null;
    private String shortDescription = null;
    private PeerIssue isDuplicate = null;
    // optional fields
    private List keywords = new ArrayList();
    private List blocks = new ArrayList();
    private List cc = new ArrayList();
    // issue rich fields
    private List descriptions = new ArrayList();
    private List attachments = new ArrayList();
    private List activities = new ArrayList();
    private List duplicates = new ArrayList();
    private List dependsOn = new ArrayList();

    /**
     * Creates a new empty issue.
     */
    public Issue() {
        // do nothing
    }

    /**
     * Creates a new issue that uses the specified issue as a template.
     */
    public Issue(Issue template) {
        id = template.id;
        statusCode = template.statusCode;
        status = template.status;
        priority = template.priority;
        resolution = template.resolution;
        component = template.component;
        version = template.version;
        repPlatform = template.repPlatform;
        assignedTo = template.assignedTo;
        deltaTimestamp = template.deltaTimestamp;
        subcomponent = template.subcomponent;
        reporter = template.reporter;
        targetMilestone = template.targetMilestone;
        issueType = template.issueType;
        creationTimestamp = template.creationTimestamp;
        qaContact = template.qaContact;
        statusWhiteboard = template.statusWhiteboard;
        votes = template.votes;
        operatingSystem = template.operatingSystem;
        shortDescription = template.shortDescription;
        keywords.addAll(template.keywords);
        blocks.addAll(template.blocks);
        dependsOn.addAll(template.dependsOn);
        cc.addAll(template.cc);
        descriptions.addAll(template.descriptions);
        attachments.addAll(template.attachments);
        activities.addAll(template.activities);
    }

    /**
     * ID of this issue (unique key).
     */
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Status code of this issue (load status).
     */
    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Current status of this issue.
     */
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Priority (severity) assigned to issue.
     */
    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    /**
     * The issue's resolution, if any
     */
    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    /**
     * Product against which issue is reported.
     */
    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    /**
     * Version associated with component.
     */
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Platform that the issue was reported against.
     */
    public String getRepPlatform() {
        return repPlatform;
    }

    public void setRepPlatform(String repPlatform) {
        this.repPlatform = repPlatform;
    }

    /**
     * Email of person issue currently assigned to.
     */
    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    /**
     * Last modified timestamp ('yyyy-mm-dd hh:mm:ss').
     */
    public Date getDeltaTimestamp() {
        return deltaTimestamp;
    }

    public void setDeltaTimestamp(Date deltaTimestamp) {
        this.deltaTimestamp = deltaTimestamp;
    }

    /**
     * Component of component issue reported against.
     */
    public String getSubcomponent() {
        return subcomponent;
    }

    public void setSubcomponent(String subcomponent) {
        this.subcomponent = subcomponent;
    }

    /**
     * Email of initial issue reporter.
     */
    public String getReporter() {
        return reporter;
    }

    public void setReporter(String reporter) {
        this.reporter = reporter;
    }

    /**
     * Milestone for this issue's resolution.
     */
    public String getTargetMilestone() {
        return targetMilestone;
    }

    public void setTargetMilestone(String targetMilestone) {
        this.targetMilestone = targetMilestone;
    }

    /**
     * Nature of issue.  This refers to whether the issue is a defect, task,
     * enhancement, etc.
     */
    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    /**
     * Issue creation timestamp ('yyyy-mm-dd hh:mm:ss').
     */
    public Date getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(Date creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    /**
     * Email of the QA contact for this issue.
     */
    public String getQAContact() {
        return qaContact;
    }

    public void setQAContact(String qaContact) {
        this.qaContact = qaContact;
    }

    /**
     * Free text 'whiteboard' for issue comments.
     */
    public String getStatusWhiteboard() {
        return statusWhiteboard;
    }

    public void setStatusWhiteboard(String statusWhiteboard) {
        this.statusWhiteboard = statusWhiteboard;
    }

    /**
     * current votes for issue.
     */
    public String getVotes() {
        return votes;
    }

    public void setVotes(String votes) {
        this.votes = votes;
    }

    /**
     * URL related to issue
     */
    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }


    /**
     * Operating system issue reported against.
     */
    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    /**
     * Short description of issue.
     */
    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    /**
     * List of keywords for this issue.
     */
    public List getKeywords() {
        return keywords;
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
     * Get the attachments to this issue.
     */
    public List getAttachments() {
        return attachments;
    }

    /**
     * Get the activity upon this issue.
     */
    public List getActivities() {
        return activities;
    }

    /**
     * Other issues which were closed as a duplicate of this issue.
     */
    public List getDuplicates() {
        return duplicates;
    }

    /**
     * List of local issue IDs that depend on this one.
     */
    public List getDependsOn() {
        return dependsOn;
    }

    /**
     * List of local issue IDs blocked by this one.
     */
    public List getBlocks() {
        return blocks;
    }

    /**
     * The issue which this issue was closed as a duplicate of.
     */
    public PeerIssue getDuplicate() {
        return isDuplicate;
    }
    public void setDuplicate(PeerIssue peerIssue) {
        this.isDuplicate = isDuplicate;
    }

    /**
     * Write this issue for debugging.
     */
    public String toString() {
        return "Issue " + id + ": " + getPriority().getRating();
    }

    /**
     * Compares two issues by ID.
     */
    public int compareTo(Object other) {
        if (other == null) return -1;
        Issue otherIssue = (Issue) other;
        return id.compareTo(otherIssue.id);
    }

    /**
     * Gets the strings to filter this issue by.
     */
    public void getFilterStrings(List baseList) {
        // the displayed strings
        baseList.add(id);
        baseList.add(issueType);
        baseList.add(priority.toString());
        baseList.add(status);
        baseList.add(resolution);
        baseList.add(shortDescription);

        // extra strings
        baseList.add(component);
        baseList.add(subcomponent);

        // recursively get filter strings from the descriptions
        for (int d = 0; d < getDescriptions().size(); d++) {
            Description description = (Description) getDescriptions().get(d);
            description.getFilterStrings(baseList);
        }
    }

    /**
     * Return a list of issues. This list is generated in an interesting way.
     * First, an XML file is paresed into a list of Issue objects. Those Issue
     * Objects then write out the Java code necessary to create them. Finally
     * that Java code is pasted here.
     *
     * <p>This removes the need to include an XML file and reduces parse time.
     */
    /*public static List loadIssues() {
        try {
            String baseUrl = "https://glazedlists.dev.java.net/issues/xml.cgi";
            return IssuezillaXMLParser.loadIssues(baseUrl, 1, 150);
        } catch(IOException e) {
            e.printStackTrace();
        } catch(SecurityException e) {
            e.printStackTrace();
        }

        return Collections.EMPTY_LIST;
    }*/
}

/**
 * Data pertaining to the issue's activity record.
 */
class Activity {
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

    public void setUser(String user) {
        this.user = user;
    }

    /**
     * date the described change was made
     */
    public Date getWhen() {
        return when;
    }

    public void setWhen(Date when) {
        this.when = when;
    }

    /**
     * name of db field (in fielddefs)
     */
    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    /**
     * description of the database field
     */
    public String getFieldDescription() {
        return fieldDescription;
    }

    public void setFieldDescription(String fieldDescription) {
        this.fieldDescription = fieldDescription;
    }

    /**
     * value changed from
     */
    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    /**
     * value changed to
     */
    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }
}


/**
 * Reference to this issue's duplicate.
 */
class PeerIssue {
    private String issueId = null;
    private String who = null;
    private Date when = null;

    /**
     * user who created the duplicate.
     */
    public String getWho() {
        return who;
    }

    public void setWho(String who) {
        this.who = who;
    }

    /**
     * date the described change was made
     */
    public Date getWhen() {
        return when;
    }

    public void setWhen(Date when) {
        this.when = when;
    }

    /**
     * ID of the duplicate.
     */
    public String getIssueId() {
        return issueId;
    }

    public void setIssueId(String issueId) {
        this.issueId = issueId;
    }
}


/**
 * Data pertaining to attachments.  NOTE - some of these fields
 * are currently unimplemented (ispatch, filename, etc.).
 */
class Attachment {
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

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * A unique id for this attachment.
     */
    public String getAttachId() {
        return attachId;
    }

    public void setAttachId(String attachId) {
        this.attachId = attachId;
    }

    /**
     * Timestamp of when added 'yyyy-mm-dd hh:mm'
     */
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Short description for attachment.
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Whether attachment is a patch file.
     */
    public String getIsPatch() {
        return isPatch;
    }

    public void setIsPatch(String isPatch) {
        this.isPatch = isPatch;
    }

    /**
     * Filename of attachment.
     */
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * Issuezilla ID of attachement submitter.
     */
    public String getSubmitterId() {
        return submitterId;
    }

    public void setSubmitterId(String submitterId) {
        this.submitterId = submitterId;
    }

    /**
     * username of attachement submitter.
     */
    public String getSubmitterUsername() {
        return submitterUsername;
    }

    public void setSubmitterUsername(String submitterUsername) {
        this.submitterUsername = submitterUsername;
    }

    /**
     * Encoded attachment.
     */
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    /**
     * URL to attachment in iz.
     */
    public String getAttachmentIzUrl() {
        return attachmentIzUrl;
    }

    public void setAttachmentIzUrl(String attachmentIzUrl) {
        this.attachmentIzUrl = attachmentIzUrl;
    }
}
