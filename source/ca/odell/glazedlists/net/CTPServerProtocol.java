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
// logging
import java.util.logging.*;
// parsing
import ca.odell.glazedlists.util.impl.ByteBufferParser;
import java.text.ParseException;

/**
 * The CTPServerProtocol is a serverside implementation of Chunked Transfer
 * Protocol. The serverside implementation must handle incoming POST requests
 * by responding with a coded response and is otherwise identical to the client. 
 */
final class CTPServerProtocol extends CTPProtocol {

    /** logging */
    private static Logger logger = Logger.getLogger(CTPServerProtocol.class.toString());

    /** a buffer to write out of */
    private ByteBuffer outBuffer = ByteBuffer.allocateDirect(1024);
    /** a buffer to read in to */
    private ByteBuffer inBuffer = ByteBuffer.allocateDirect(1024);

    /**
     * Creates a new CTPServerProtocol.
     *
     * @param selectionKey the connection managed by this higher-level protocol.
     */
    public CTPServerProtocol(SelectionKey selectionKey, CTPHandler handler) {
        super(selectionKey, handler);
        
        // wait for the request
        state = STATE_AWAITING_REQUEST;
        
        // we are currently interested in reading
        selectionKey.interestOps(SelectionKey.OP_READ);
    }
    
    /**
     * Sends the response header to the client.
     *
     * @param code an HTTP response code such as 200 (OK). See HTTP/1.1 RFC, 6.1.1
     * @param headers a Map of HTTP response headers. See HTTP/1.1 RFC, 6.2. This can
     *      be null to indicate no headers.
     */
    public void sendResponse(int code, Map headers) throws CTPException {
        if(state != STATE_CONSTRUCTING_RESPONSE) throw new IllegalStateException();
        
        try {
            // write the status line
            outBuffer.put(HTTP_VERSION.getBytes());
            if(code == CTPHandler.OK) {
                outBuffer.put(new Integer(CTPHandler.OK).toString().getBytes());
                outBuffer.put(HTTP_SPACE);
                outBuffer.put(HTTP_REASON_PHRASE_OK);
            } else if(code == CTPHandler.NOT_FOUND) {
                outBuffer.put(new Integer(CTPHandler.NOT_FOUND).toString().getBytes());
                outBuffer.put(HTTP_SPACE);
                outBuffer.put(HTTP_REASON_PHRASE_NOT_FOUND);
            } else {
                throw new IllegalArgumentException("Unsupported code: " + code);
            }
            outBuffer.put(HTTP_CRLF);
    
            // write all the headers
            for(Iterator i = headers.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry mapEntry = (Map.Entry)i.next();
                outBuffer.put(mapEntry.getKey().toString().getBytes());
                outBuffer.put(HTTP_HEADER_SEPARATOR);
                outBuffer.put(mapEntry.getValue().toString().getBytes());
                outBuffer.put(HTTP_CRLF);
            }
            outBuffer.put(HTTP_CRLF);
            
            // fire off the response
            while(outBuffer.hasRemaining()) {
                socketChannel.write(outBuffer);
            }
            
            // empty the buffer
            outBuffer.clear();
        } catch(IOException e) {
            throw new CTPException(e);
        }
    }

    /**
     * Handles incoming bytes.
     */
    void handleRead() throws IOException {
        // fill our buffer with incoming data
        int count = 0;
        ByteBufferParser parser = new ByteBufferParser(inBuffer);
        while((count = socketChannel.read(inBuffer)) > 0) {

            // read a request
            if(state == STATE_AWAITING_REQUEST) {
                String uri = null;
                Map headers = new TreeMap();
                
                // parse the request
                try {
                    
                    // if the entire header has not loaded, load more
                    logger.finest("Current buffer " + inBuffer);
                    inBuffer.flip();
                    if(parser.indexOf("\\r?\\n\\r?\\n") == -1) {
                        logger.finest("Insufficient data thus far " + parser);
                        inBuffer.compact();
                        continue;
                    }
                    
                    // parse the status line
                    parser.consume("POST( )+");
                    uri = parser.readUntil("( )+");
                    parser.consume("HTTP\\/1\\.1 *");
                    parser.consume("\\r?\\n");
                    logger.finest("Reading request " + uri + " from " + this);
                    
                    // parse the headers
                    while(true) {
                        if(parser.indexOf("\\r?\\n") == 0) break;
                        String key = parser.readUntil("\\:( )*");
                        String value = parser.readUntil("\\r?\\n");
                        headers.put(key, value);
                        logger.finest("Read header " + key + ": " + value + " from " + this);
                    }
                    
                } catch(ParseException e) {
                    // handle this better
                    e.printStackTrace();
                }
                
                // handle the request
                state = STATE_CONSTRUCTING_RESPONSE;
                handler.receiveRequest(this, uri, headers);
            } else {
                throw new IllegalStateException();
            }
        }
        
        // handle EOF, close channel. This invalidates the key
        if(count < 0) {
            handleClose(null);
        }
    }
}
