/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.net;

// NIO is used for CTP
import java.util.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.io.*;

/**
 * The CTPClientProtocol is a clientside implementation of Chunked Transfer
 * Protocol. The clientside implementation must generate outgoing POST requests
 * and accept the server's coded response but is otherwise identical to the server.
 */
final class CTPClientProtocol extends CTPProtocol {

    /** a buffer to read into and out of */
    private ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

    /**
     * Creates a new CTPServerProtocol.
     *
     * @param host the target connection.
     * @param selectionKey the connection managed by this higher-level protocol.
     */
    CTPClientProtocol(String host, SelectionKey selectionKey, CTPHandler handler) {
        super(selectionKey, handler);
    }

    /**
     * Sends the response header to the client.
     *
     * @param code an HTTP response code such as 200 (OK). See HTTP/1.1 RFC, 6.1.1
     * @param headers a Map of HTTP response headers. See HTTP/1.1 RFC, 6.2. This can
     *      be null to indicate no headers.
     */
    public void sendRequest(String uri, Map headers) throws CTPException {
    }

    /**
     * Handles the incoming bytes.
     */
    void handleRead() throws IOException {
        SocketChannel socketChannel = (SocketChannel)selectionKey.channel();
        buffer.clear();
        
        // read until we have exhausted the channel
        int count = 0;
        while((count = socketChannel.read(buffer)) > 0) {
            // make buffer readable
            buffer.flip();
            
            // send the data, it might not go all at once
            while(buffer.hasRemaining()) {
                socketChannel.write(buffer);
            }
            
            // empty buffer
            buffer.clear();
        }
        
        // handle EOF, close channel. This invalidates the key
        if(count < 0) {
            socketChannel.close();
        }
    }
}

