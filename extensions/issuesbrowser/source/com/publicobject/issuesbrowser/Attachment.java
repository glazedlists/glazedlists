/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser;

import java.util.Date;

/**
 * Data pertaining to attachments.  NOTE - some of these fields
 * are currently unimplemented (ispatch, filename, etc.).
 *
 * @author James Lemieux
 */
public class Attachment {
    private String mimeType;
    private String attachId;
    private Date date;
    private String description;
    private String isPatch;
    private String filename;
    private String submitterId;
    private String submitterUsername;
    private String data;
    private String attachmentIzUrl;

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
     * Issuezilla ID of attachment submitter.
     */
    public String getSubmitterId() { return submitterId; }
    public void setSubmitterId(String submitterId) { this.submitterId = submitterId; }

    /**
     * username of attachment submitter.
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