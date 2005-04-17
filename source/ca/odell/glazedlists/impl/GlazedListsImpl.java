/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl;

import java.util.*;

/**
 * A utility class containing all sorts of random things that are useful for
 * implementing Glazed Lists but not for using Glazed Lists.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class GlazedListsImpl {

    /**
     * A dummy constructor to prevent instantiation of this class
     */
    private GlazedListsImpl() {
        throw new UnsupportedOperationException();
    }
    
    // Utility Methods // // // // // // // // // // // // // // // // // // //

    /**
     * Compare the specified objects for equality.
     */
    public static boolean equal(Object a, Object b) {
        if(a == b) return true;
        if(a == null || b == null) return false;
        return a.equals(b);
    }
}