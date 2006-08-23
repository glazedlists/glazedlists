/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser;

import java.util.Date;

/**
 * Reference to an Issue's duplicate(s).
 *
 * @author James Lemieux
 */
public class PeerIssue {
    private String issueId;
    private String who;
    private Date when;

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