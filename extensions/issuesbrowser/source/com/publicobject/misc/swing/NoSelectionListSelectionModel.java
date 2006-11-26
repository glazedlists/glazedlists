/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.misc.swing;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;

/**
 * Disable column selection by providing a ListSelectionModel never has
 * any selection.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class NoSelectionListSelectionModel extends DefaultListSelectionModel {
    public int getMinSelectionIndex() {
        return -1;
    }
    public int getMaxSelectionIndex() {
        return -1;
    }
    public int getAnchorSelectionIndex() {
        return -1;
    }
    public int getLeadSelectionIndex() {
        return -1;
    }
    public boolean isSelectedIndex(int index) {
        return false;
    }
}
