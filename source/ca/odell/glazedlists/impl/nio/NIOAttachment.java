/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.nio;

// NIO is used for CTP
/**
 * Handles all sorts of incoming NIO events. An object implementing this interface
 * must be set as the attachment for all SelectionKeys used by the NIODaemon.
 */
public interface NIOAttachment {

    /**
     * Handle a connect-ready key.
     */
    public void handleConnect();

    /**
     * Handle a read-ready key.
     */
    public void handleRead();

    /**
     * Handle a write-ready key.
     */
    public void handleWrite();

    /**
     * Handle a close-ready key.
     */
    public void close(Exception reason);

}
