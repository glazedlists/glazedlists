/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import java.util.*;

/**
 * Produces random data for test cases to consume.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class RandomDataFactory {
    
    /** the raw random data */
    private static Random dice = new Random();
    private static String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * Constructs a random string of the specified length.
     */
    public static String nextString(int length) {
        StringBuffer result = new StringBuffer();
        for(int i = 0; i < length; i++) {
            result.append(nextCharacter());
        }
        return result.toString();
    }

    /**
     * Gets a random character.
     */
    public static char nextCharacter() {
        return alphabet.charAt(dice.nextInt(alphabet.length()));
    }

}
