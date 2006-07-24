/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.gui;

public final class TreeTableSupport {

    private TreeTableSupport() {}

    public static int getDepth(TreeFormat treeTableFormat, Object o) {
        int height = -1;

        while (o != null) {
            o = treeTableFormat.getParent(o);
            height++;
        }

        return height;
    }
}