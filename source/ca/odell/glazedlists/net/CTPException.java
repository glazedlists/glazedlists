/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.net;

// for maps of headers
import java.util.*;

/**
 * A CTPException is thrown when there is an error connecting to the
 * network or parsing a response.
 */
class CTPException extends Exception {
    public CTPException(String message) {
        super(message);
    }
    public CTPException(Exception cause) {
        super(cause);
    }
}
