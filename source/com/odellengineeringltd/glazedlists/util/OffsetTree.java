/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.util;


/**
 * Models a sorted tree that can get and delete objects
 * based on their index in the tree.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class OffsetTree {
    
    
    public static void main(String[] args) {
        
        IndexedTree tree = new IndexedTree();
        System.out.println(tree);
        tree.addByNode(0, "s");
        System.out.println(tree);
        tree.addByNode(1, "s");
        System.out.println(tree);
        tree.addByNode(0, "e");
        System.out.println(tree);
        tree.addByNode(3, " ");
        System.out.println(tree);
        tree.addByNode(4, "W");
        System.out.println(tree);
        tree.addByNode(0, "J");
        System.out.println(tree);
        tree.addByNode(6, "i");
        System.out.println(tree);
        tree.addByNode(7, "l");
        System.out.println(tree);
        tree.addByNode(8, "s");
        System.out.println(tree);
        tree.addByNode(9, "n");
        System.out.println(tree);
        tree.addByNode(9, "o");
        System.out.println(tree);
        tree.addByNode(4, "e");
        System.out.println(tree);
        System.out.println("");

        tree.removeByIndex(8);
        System.out.println(tree);
        tree.removeByIndex(8);
        System.out.println(tree);
        tree.removeByIndex(6);
        System.out.println(tree);
        tree.removeByIndex(6);
        System.out.println(tree);
        tree.removeByIndex(6);
        System.out.println(tree);
        tree.removeByIndex(6);
        System.out.println(tree);
        
        tree.addByNode(6, "!");
        System.out.println(tree);
        tree.addByNode(6, "S");
        System.out.println(tree);
        tree.addByNode(6, "K");
        System.out.println(tree);
        tree.addByNode(6, "C");
        System.out.println(tree);
        tree.addByNode(5, "R");
        System.out.println(tree);
        tree.addByNode(6, "O");
        System.out.println(tree);
        
        
        
    }
}
