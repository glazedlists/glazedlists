/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.util.impl;

// to implement the Comparator interface
import java.util.Comparator;

/**
 * A Comparator for sorting AgedNodes
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public final class AgedNodeComparator implements Comparator {

    public final int compare(Object o1, Object o2) {
        AgedNode node1 = (AgedNode)o1;
        AgedNode node2 = (AgedNode)o2;
        long difference = node1.getTimestamp() - node2.getTimestamp();
        if(difference < 0) {
            return -1;
        } else if(difference > 0) {
            return 1;
        } else {
            return 0;
        }
    }
}