/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser;

/**
 * Simple animated monitor shows if work is taking place. This can be implemented
 * in any GUI toolkit.
 *
 * @author <a href="jesse@odel.on.ca">Jesse Wilson</a>
 */
public interface Throbber {

    public void setOn();

    public void setOff();
}