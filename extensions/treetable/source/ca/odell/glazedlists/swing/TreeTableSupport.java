/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.TreeList;

import javax.swing.*;

/**
 * Prototype interface...
 *
 * @author jessewilson
 */
public class TreeTableSupport {

    /**
     * Some things this could do:
     *
     * 1. Install custom renderer that wraps the renderer in place
     * 2. Install custom editor that wraps the editor in place
     * 3. Install keyboard listeners for tree navigation via keyboard
     */
    public TreeTableSupport install(JTable table, TreeList treeList, int modelColumn) {
        throw new UnsupportedOperationException();
    }

    public void dispose() {
        throw new UnsupportedOperationException();
    }
}
