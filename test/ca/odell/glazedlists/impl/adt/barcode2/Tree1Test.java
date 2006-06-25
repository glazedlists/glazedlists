// Copyright 2006 Google Inc. All Rights Reserved.

package ca.odell.glazedlists.impl.adt.barcode2;

import junit.framework.TestCase;

import java.util.*;

import ca.odell.glazedlists.GlazedListsTests;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class Tree1Test extends TestCase {

    /** test values */
    private static List<String> colors = GlazedListsTests.stringToList("A");
    private static ListToByteCoder<String> coder = new ListToByteCoder<String>(Tree1Test.colors);
    private static byte allColors = Tree1Test.coder.colorsToByte(GlazedListsTests.stringToList("ABC"));

    /**
     * Make sure we can have a few unsorted elements in an otherwise ordered tree.
     */
    public void testUnsortedElementInSortedTree() {
        Tree1<String> tree = new Tree1<String>(Tree1Test.coder);
        Element<String> e = tree.addInSortedOrder(Tree1Test.allColors, "E", 1);
        Element<String> g = tree.addInSortedOrder(Tree1Test.allColors, "G", 1);
        Element<String> i = tree.addInSortedOrder(Tree1Test.allColors, "I", 1);
        Element<String> k = tree.addInSortedOrder(Tree1Test.allColors, "K", 1);
        Element<String> m = tree.addInSortedOrder(Tree1Test.allColors, "M", 1);
        Element<String> o = tree.addInSortedOrder(Tree1Test.allColors, "O", 1);

        k.setSorted(false);
        k.set("A");

        Element<String> h = tree.addInSortedOrder(Tree1Test.allColors, "H", 1);
        Element<String> n = tree.addInSortedOrder(Tree1Test.allColors, "N", 1);

        List<String> asList = new Tree1AsList<String>(tree);

        assertEquals(GlazedListsTests.stringToList("EGHIAMNO"), asList);
    }
}
