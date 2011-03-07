/* Glazed Lists                                                 (c) 2003-2011 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser;

import ca.odell.glazedlists.EventList;

/**
 * <code>StatusProvider</code> is a helper class to provide the status for a status name. It
 * determines the project from the list of issues.
 *
 * @author Holger Brands
 */
public class StatusProvider {

    private EventList<Issue> issues;

    /**
     * constructor with issue list.
     */
    public StatusProvider(EventList<Issue> issues) {
        this.issues = issues;
    }

    /**
     * Finds a status by name
     *
     * @param name the status name
     * @return the status
     */
    public Status statusFor(String name) {
        return currentProject().getOwner().statusFor(name);
    }

    /**
     * @return the current project of the issues
     */
    private Project currentProject() {
        if ((issues == null) || issues.isEmpty()) {
            return null;
        } else {
            return issues.get(0).getProject();
        }
    }
}