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
import ca.odell.glazedlists.util.impl.ByteChannelReader;
import ca.odell.glazedlists.util.impl.ByteChannelWriter;
// logging
import java.util.logging.*;
import java.text.ParseException;

/**
 * The CTPServerProtocol is a serverside implementation of Chunked Transfer
 * Protocol. The serverside implementation must handle incoming POST requests
 * by responding with a coded response and is otherwise identical to the client. 
 */
final class CTPServerProtocol extends CTPProtocol {

    /** logging */
    private static Logger logger = Logger.getLogger(CTPServerProtocol.class.toString());

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
            writer.write("HTTP/1.1");
            if(code == CTPHandler.OK) {
                writer.write(new Integer(CTPHandler.OK).toString());
                writer.write(" ");
                writer.write("OK");
            } else if(code == CTPHandler.NOT_FOUND) {
                writer.write(new Integer(CTPHandler.NOT_FOUND).toString());
                writer.write(" ");
                writer.write("Not Found");
            } else {
                throw new IllegalArgumentException("Unsupported code: " + code);
            }
            writer.write("\r\n");
    
            // write all the headers
            for(Iterator i = headers.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry mapEntry = (Map.Entry)i.next();
                writer.write(mapEntry.getKey().toString());
                writer.write(": ");
                writer.write(mapEntry.getValue().toString());
                writer.write("\r\n");
            }
            writer.write("\r\n");
            writer.flush();
            
            // we're done
            state = STATE_READY;

        } catch(IOException e) {
            throw new CTPException(e);
        }
    }

    /**
     * Handles incoming bytes.
     */
    void handleRead() throws IOException {
        // read a request
        if(state == STATE_AWAITING_REQUEST) {
            handleRequest();
            
        } else {
            throw new IllegalStateException();
        }
    }
    
    /**
     * Handle a request by parsing the contents of the input buffer. If the input
     * buffer does not contain a complete request, this will return. If it does
     * contain a request:
     *   <li>the request will be processed
     *   <li>the buffer will be advanced, and
     *   <li>the state will be updated
     */
    private void handleRequest() {
        String uri = null;
        Map headers = new TreeMap();
        
        try {
            // if the entire header has not loaded, load more
            if(parser.indexOf("\\r?\\n\\r?\\n") == -1) {
                logger.finest("Insufficient data thus far " + parser);
                return;
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
            close(e);
        } catch(IOException e) {
            close(e);
        }
        
        // handle the request
        state = STATE_CONSTRUCTING_RESPONSE;
        handler.receiveRequest(this, uri, headers);
    }
}
