/**
 * Glazed Lists
 * http://opensource.odellengineeringltd.com/glazedlists/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.util;

// for specifying a sorting algorithm
import java.util.Comparator;
// for iterating through the values
import java.util.Iterator;


/**
 * Models a tree which keeps its elements in sorted order. The
 * sorted tree knows about any parent trees so a value can be removed
 * from the leaf up.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class SortedTree {
    

    public static void main(String[] args) {
        
        //String[] strings = new String[] { "Jesse", "David", "Bruce", "Wilson" };
        String[] strings = new String[] { "M", "N", "O", "P", "Q", "R", "S", "T", "U", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "V", "W", "X", "Y", "Z" };

        IndexedTree alphaTree = new IndexedTree(new ComparableComparator());

        alphaTree.validate();
        alphaTree.validate();

        for(int i = 0; i < strings.length; i++) {
            alphaTree.addByNode(strings[i]);
            System.out.println(alphaTree);
            alphaTree.validate();
        }
        
        for(int i = strings.length - 1; i > 0; i--) {
            IndexedTreeNode node = alphaTree.getNode(new java.util.Random().nextInt(i));
            node.removeFromTree();
            System.out.print("" + node.getValue());
            for(Iterator j = alphaTree.iterator(); j.hasNext();) {
                System.out.print(" " + ((IndexedTreeNode)j.next()).getValue());
            }
            System.out.println("");
            alphaTree.validate();
        }
    }
}
