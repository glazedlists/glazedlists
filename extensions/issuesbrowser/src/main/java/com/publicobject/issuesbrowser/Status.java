/* Glazed Lists                                                 (c) 2003-2011 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser;

import java.awt.Color;

/**
 * <code>Status</code> is an interface for the status field of an issue.
 *
 * @author Holger Brands
 */
public interface Status {

    /**
     * @return the id of this status
     */
    String getId();

    /**
     * @return the name of this status
     */
    String getName();

    /**
     * @return the color associated with this status
     */
    Color getColor();

    /**
     * @return <code>true</code>, if status repesents an issue, that is not yet done (resolved,
     *         verified or closed)
     */
    boolean isActive();
}
