/**
 * Glazed Lists
 * http://opensource.odellengineeringltd.com/glazedlists/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.jtextfield;

/**
 * A String completer that uses a cache of String completions,
 * returning the first entry that matches the prefix.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ArrayStringCompleter implements StringCompleter {

    /** the complete Strings */
    private String[] completions;

    /**
     * Creates a new ArrayStringCompleter which uses the supplied array
     * as the list of completed Strings to search through.
     */
    public ArrayStringCompleter(String[] completions) {
        this.completions = completions;
    }
    
    /**
     * Takes a String and return a longer string which has the
     * supplied string as a prefix.
     */
    public String getCompleted(String prefix) {
        if(prefix.length() == 0) return prefix;
        // get all strings which this is a prefix of (and prefixes of this)
        for(int i = 0; i < completions.length; i++) {
            if(completions[i].indexOf(prefix) == 0) {
                return completions[i];
            }
        }
        return prefix;
    }
}
