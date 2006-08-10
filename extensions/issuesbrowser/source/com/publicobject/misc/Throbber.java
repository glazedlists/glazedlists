/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.misc;

/**
 * Simple animated monitor shows if work is taking place. This can be implemented
 * in any GUI toolkit.
 *
 * @author <a href="jesse@swank.ca">Jesse Wilson</a>
 */
public interface Throbber {

    public void setOn();

    public void setOff();
}