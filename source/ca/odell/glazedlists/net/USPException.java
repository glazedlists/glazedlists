/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.net;

/**
 * A USPException is thrown when there is an error subscribing to an update
 * source.
 */
class USPException extends Exception {
    public USPException(String message) {
        super(message);
    }
    public USPException(Exception cause) {
        super(cause);
    }
}
