/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.gui;

public final class TreeTableSupport {

    private TreeTableSupport() {}

    public static <E> int getHeight(TreeTableFormat<E> treeTableFormat, E e) {
        int height = -1;

        while (e != null) {
            e = treeTableFormat.getParent(e);
            height++;
        }

        return height;
    }
}