/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// for being a JUnit test case
import junit.framework.*;
import java.util.*;
// NIO
import java.nio.*;
import java.nio.channels.*;
import java.io.*;
// regular expressions
import java.util.regex.*;
import java.text.ParseException;
// logging
import java.util.logging.*;
import java.text.ParseException;

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
