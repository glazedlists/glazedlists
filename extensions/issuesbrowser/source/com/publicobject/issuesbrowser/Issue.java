/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser;

import ca.odell.glazedlists.TextFilterable;
import ca.odell.glazedlists.jfreechart.ValueSegment;
import ca.odell.glazedlists.jfreechart.DefaultValueSegment;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * An issue models a work effort either due to an existing problem or a desired
 * enhancement.
 *
 * @author James Lemieux
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class Issue implements Comparable {

    public static final DateFormat TABLE_DATE_FORMAT = DateFormat.getDateInstance(DateFormat.MEDIUM);
    public static final DateFormat DETAILS_DATE_FORMAT = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");

    // the project that this issue is attached to
    private Project owner;

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
    private List<String> keywords = new ArrayList<String>();
    private List<PeerIssue> blocks = new ArrayList<PeerIssue>();
    private List<String> cc = new ArrayList<String>();
    // issue rich fields
    private List<Description> descriptions = new ArrayList<Description>();
    private List<Attachment> attachments = new ArrayList<Attachment>();
    private List<Activity> activities = new ArrayList<Activity>();
    private List<ValueSegment<Date,String>> stateChanges = new ArrayList<ValueSegment<Date,String>>();
    private List<PeerIssue> duplicates = new ArrayList<PeerIssue>();
    private List<PeerIssue> dependsOn = new ArrayList<PeerIssue>();

    private List<String> allUsers = null;

    /**
     * Gets all users related to this issue.
     */
    public List<String> getAllUsers() {
        // init the users list if necessary
        if(allUsers == null) {
            allUsers = new ArrayList<String>();
            if(assignedTo != null) allUsers.add(assignedTo);
            if(reporter != null) allUsers.add(reporter);
            if(qaContact != null) allUsers.add(qaContact);
            for(Iterator<Description> d = descriptions.iterator(); d.hasNext(); ) {
                allUsers.add(d.next().getWho());
            }
        }

        return allUsers;
    }

    /**
     * Creates a new empty issue.
     */
    public Issue(Project owner) {
        this.owner = owner;
    }

    /**
     * Creates a new issue that uses the specified issue as a template.
     */
    public Issue(Project owner, Issue template) {
        this.owner = owner;
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
     * This convenience method processes a fully loaded Issue and produces a
     * List of RangedValues which describe the duration of each Status within
     * the lifetime of the given <code>issue</code>. For example, an Issue
     * might have a series of state changes such as:
     *
     * <p>{NEW: Issue creation Timestamp -> Feb}, {STARTED: Feb -> Apr}, {CLOSED: Apr -> Mar}, {RESOLVED: Mar -> lastDate}
     *
     * <p> and each part of that progression will be a {@link ValueSegment}
     * containing a start and end Date and a status.
     *
     * @param issue the Issue to compute the state changes for
     * @return a List of {@link ValueSegment} objecs describing the duration
     *      of each Status within the lifetime of the <code>issue</code>
     */
    public static List<ValueSegment<Date,String>> computeStateChanges(Issue issue, Date lastDate) {
        // this stores the sequence of state changes in chronological order, like a timeline
        final List<ValueSegment<Date,String>> timeline = new ArrayList<ValueSegment<Date,String>>();

        String state = "NEW";

        // the end Date of the previous ValueSegment
        Date last = issue.getCreationTimestamp();

        // Iterate the issues in chronological (natural) order
        for (Iterator<Activity> i = issue.getActivities().iterator(); i.hasNext();) {
            Activity activity = i.next();

            // if the Activity represents a change in status
            if ("issue_status" == activity.getField()) {
                // create an entry in the timeline
                timeline.add(new DefaultValueSegment<Date,String>(last, activity.getWhen(), state));

                last = activity.getWhen();
                state = activity.getNewValue();
            }
        }

        // create one last entry in the time timeline that ends at the given lastDate
        timeline.add(new DefaultValueSegment<Date,String>(last, lastDate, state));

        return timeline;
    }

    /**
     * ID of this issue (unique key).
     */
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    /**
     * Get the web address of this issue for use with a browser like IE or Firefox.
     */
    public URL getURL() {
        try {
            return new URL(owner.getBaseUri() + "/issues/show_bug.cgi?id=" + getId());
        } catch(MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Status code of this issue (load status).
     */
    public Integer getStatusCode() { return statusCode; }
    public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }

    /**
     * Current status of this issue.
     */
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    /**
     * Priority (severity) assigned to issue.
     */
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    /**
     * The issue's resolution, if any
     */
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }

    /**
     * Product against which issue is reported.
     */
    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }

    /**
     * Version associated with component.
     */
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    /**
     * Platform that the issue was reported against.
     */
    public String getRepPlatform() { return repPlatform; }
    public void setRepPlatform(String repPlatform) { this.repPlatform = repPlatform; }

    /**
     * Email of person issue currently assigned to.
     */
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    /**
     * Last modified timestamp ('yyyyMMddhhmmss').
     */
    public Date getDeltaTimestamp() { return deltaTimestamp; }
    public void setDeltaTimestamp(Date deltaTimestamp) { this.deltaTimestamp = deltaTimestamp; }

    /**
     * Component of component issue reported against.
     */
    public String getSubcomponent() { return subcomponent; }
    public void setSubcomponent(String subcomponent) { this.subcomponent = subcomponent; }

    /**
     * Email of initial issue reporter.
     */
    public String getReporter() { return reporter; }
    public void setReporter(String reporter) { this.reporter = reporter; }

    /**
     * Milestone for this issue's resolution.
     */
    public String getTargetMilestone() { return targetMilestone; }
    public void setTargetMilestone(String targetMilestone) { this.targetMilestone = targetMilestone; }

    /**
     * Nature of issue.  This refers to whether the issue is a defect, task,
     * enhancement, etc.
     */
    public String getIssueType() { return issueType; }
    public void setIssueType(String issueType) { this.issueType = issueType; }

    /**
     * Issue creation timestamp ('yyyy-mm-dd hh:mm:ss').
     */
    public Date getCreationTimestamp() { return creationTimestamp; }
    public void setCreationTimestamp(Date creationTimestamp) { this.creationTimestamp = creationTimestamp; }

    /**
     * Email of the QA contact for this issue.
     */
    public String getQAContact() { return qaContact; }
    public void setQAContact(String qaContact) { this.qaContact = qaContact; }

    /**
     * Free text 'whiteboard' for issue comments.
     */
    public String getStatusWhiteboard() { return statusWhiteboard; }
    public void setStatusWhiteboard(String statusWhiteboard) { this.statusWhiteboard = statusWhiteboard; }

    /**
     * current votes for issue.
     */
    public String getVotes() { return votes; }
    public void setVotes(String votes) { this.votes = votes; }

    /**
     * URL related to issue
     */
    public String getFileLocation() { return fileLocation; }
    public void setFileLocation(String fileLocation) { this.fileLocation = fileLocation; }

    /**
     * Operating system issue reported against.
     */
    public String getOperatingSystem() { return operatingSystem; }
    public void setOperatingSystem(String operatingSystem) { this.operatingSystem = operatingSystem; }

    /**
     * Short description of issue.
     */
    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }

    /**
     * List of keywords for this issue.
     */
    public List<String> getKeywords() { return keywords; }

    /**
     * List of email addresses of interested parties.
     */
    public List<String> getCC() { return cc; }

    /**
     * Data from the longdescs table for this issue id. Essentially
     * the log of additional comments.
     */
    public List<Description> getDescriptions() { return descriptions; }

    /**
     * Get the attachments to this issue.
     */
    public List<Attachment> getAttachments() { return attachments; }

    /**
     * Get the activity upon this issue.
     */
    public List<Activity> getActivities() { return activities; }

    /**
     * Get the List of RangedValues describing the succession of state changes
     * this Issue has experienced.
     */
    public List<ValueSegment<Date, String>> getStateChanges() { return stateChanges; }

    /**
     * Other issues which were closed as a duplicate of this issue.
     */
    public List<PeerIssue> getDuplicates() { return duplicates; }

    /**
     * List of local issue IDs that depend on this one.
     */
    public List<PeerIssue> getDependsOn() { return dependsOn; }

    /**
     * List of local issue IDs blocked by this one.
     */
    public List<PeerIssue> getBlocks() { return blocks; }

    /**
     * The issue which this issue was closed as a duplicate of.
     */
    public PeerIssue getDuplicate() { return isDuplicate; }
    public void setDuplicate(PeerIssue isDuplicate) { this.isDuplicate = isDuplicate; }

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
    public String getWho() { return who; }
    public void setWho(String who) { this.who = who; }

    /**
     * date the described change was made
     */
    public Date getWhen() { return when; }
    public void setWhen(Date when) { this.when = when; }

    /**
     * ID of the duplicate.
     */
    public String getIssueId() { return issueId; }
    public void setIssueId(String issueId) { this.issueId = issueId; }
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
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    /**
     * A unique id for this attachment.
     */
    public String getAttachId() { return attachId; }
    public void setAttachId(String attachId) { this.attachId = attachId; }

    /**
     * Timestamp of when added 'yyyy-mm-dd hh:mm'
     */
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    /**
     * Short description for attachment.
     */
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    /**
     * Whether attachment is a patch file.
     */
    public String getIsPatch() { return isPatch; }
    public void setIsPatch(String isPatch) { this.isPatch = isPatch; }

    /**
     * Filename of attachment.
     */
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    /**
     * Issuezilla ID of attachement submitter.
     */
    public String getSubmitterId() { return submitterId; }
    public void setSubmitterId(String submitterId) { this.submitterId = submitterId; }

    /**
     * username of attachement submitter.
     */
    public String getSubmitterUsername() { return submitterUsername; }
    public void setSubmitterUsername(String submitterUsername) { this.submitterUsername = submitterUsername; }

    /**
     * Encoded attachment.
     */
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    /**
     * URL to attachment in iz.
     */
    public String getAttachmentIzUrl() { return attachmentIzUrl; }
    public void setAttachmentIzUrl(String attachmentIzUrl) { this.attachmentIzUrl = attachmentIzUrl; }
}