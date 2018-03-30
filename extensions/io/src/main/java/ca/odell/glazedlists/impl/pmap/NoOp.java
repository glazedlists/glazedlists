/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.pmap;

// NIO is used for CTP
/**
 * This doesn't do anything! It's useful for flushing pending events with invokeAndWait().
 *
 * @deprecated The io extension and its types are deprecated.
 *             This extension becomes unsupported and will be removed
 *             from the official distribution with the next major release.
 */
@Deprecated
class NoOp implements Runnable {

    /** singleton */
    private static NoOp instance = new NoOp();

    /**
     * Private constructor blocks users from not using the singleton.
     */
    private NoOp() {
        // nothing
    }

    /**
     * Get an instance of NoOp. This is used instead of a conventional constructor
     * because NoOp is a singleton.
     */
    public static NoOp instance() {
        return instance;
    }

    /**
     * Doesn't do anything!
     */
    @Override
    public void run() {
        // nothing
    }
}
