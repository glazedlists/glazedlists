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
import java.text.ParseException;
import ca.odell.glazedlists.util.impl.ByteChannelReader;
import ca.odell.glazedlists.util.impl.ByteChannelWriter;
// logging
import java.util.logging.*;

/**
 * The CTPConnection is base class for building a client or server implementation
 * of Chunked Transfer Protocol. This protocol is a subset of HTTP/1.1 with special
 * interest to chunked encoding.
 *
 * <p>Although HTTP/1.1 is designed such that all compliant implementations can
 * interoperate, this is not the case for CTPConnection. This specialized HTTP/1.1
 * client is only capable of interoperating with other clients that implement the
 * same subset of HTTP/1.1. Known limitations of this HTTP/1.1 implementation:
 * <li>it can read and write only chunked-encoding
 * <li>it can only read and write a single URI, "/glazedlists"
 * <li>as a client, it sends only the headers, "Host", "Transfer-Encoding"
 * <li>as a server, it sends only the header, "Transfer-Encoding"
 * <li>it interprets only the header, "Transfer-Encoding".
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class CTPConnection {
    // client:
    // AWAITING_CONNECT
    //   --[connect]-->
    //     CLIENT_CONSTRUCTING_REQUEST
    //     --[send request]-->
    //       CLIENT_AWAITING_RESPONSE
    //         --[received response]-->
    //           READY
    // server:
    // AWAITING_CONNECT
    //   --[connect]-->
    //     SERVER_AWAITING_REQUEST
    //       --[received request]-->
    //         SERVER_CONSTRUCTING_RESPONSE
    //           --[sent response]-->
    //             READY
    //
    // requested shutdown:
    // READY
    //   --[close()]-->
    //     --[sent close chunk]-->
    //       --[cleanup]-->
    //         CLOSED_PERMANENTLY
    //
    // received shutdown:
    // READY
    //   --[received close chunk]-->
    //     RECEIVED_CLOSE
    //       --[cleanup]-->
    //         CLOSED_PERMANENTLY
    //
    // IO error:
    // READY
    //   --[IO error]-->
    //     --[cleanup]-->
    //       CLOSED_PERMANENTLY
    //

    /** logging */
    private static Logger logger = Logger.getLogger(CTPConnection.class.toString());
    
    /** track the current state of this protocol */
    private static final int STATE_SERVER_AWAITING_CONNECT = 0;
    private static final int STATE_CLIENT_AWAITING_CONNECT = 1;
    private static final int STATE_CLIENT_CONSTRUCTING_REQUEST = 2;
    private static final int STATE_SERVER_AWAITING_REQUEST = 3;
    private static final int STATE_SERVER_CONSTRUCTING_RESPONSE = 4;
    private static final int STATE_CLIENT_AWAITING_RESPONSE = 5;
    private static final int STATE_READY = 6;
    private static final int STATE_RECEIVED_CLOSE = 7;
    private static final int STATE_CLOSED_PERMANENTLY = 8;
    
    /** standard HTTP response headers, see HTTP/1.1 RFC, 6.1.1 */
    private static final int RESPONSE_OK = 200;
    private static final int RESPONSE_ERROR = 500;

    /** the current state of this protocol */
    private int state = -1;
    
    /** the key to this protocol's channel */
    private SelectionKey selectionKey = null;
    
    /** the channel where communication occurs */
    private SocketChannel socketChannel = null;
    
    /** parse the input channel */
    private ByteChannelReader parser;

    /** write the output channel */
    private ByteChannelWriter writer;

    /** the handler to delegate data interpretation to */
    private CTPHandler handler;
    
    /** the remote host */
    private String host = "";
    
    /** the only URI allowed by CTPConnection */
    private static final String CTP_URI = "/glazedlists";
    
    /** if our source is not chunked, we have to break up chunks arbitrarily */
    private boolean sourceChunked = false;
    
    /**
     * Creates a new CTPConnection.
     *
     * @param selectionKey the connection managed by this higher-level protocol.
     */
    private CTPConnection(SelectionKey selectionKey, CTPHandler handler) {
        if(selectionKey == null) throw new IllegalArgumentException();
        
        this.selectionKey = selectionKey;
        this.handler = handler;
        this.socketChannel = (SocketChannel)selectionKey.channel();
        this.parser = new ByteChannelReader(socketChannel);
        this.writer = new ByteChannelWriter(socketChannel, selectionKey);
    }
    
    /**
     * Create a new CTPConnection for use as a client.
     */
    static CTPConnection client(String host, SelectionKey selectionKey, CTPHandler handler) {
        CTPConnection client = new CTPConnection(selectionKey, handler);
        client.state = STATE_CLIENT_AWAITING_CONNECT;
        client.host = host;
        return client;
    }
    
    /**
     * Create a new CTPConnection for use as a server.
     */
    static CTPConnection server(SelectionKey selectionKey, CTPHandler handler) {
        CTPConnection server = new CTPConnection(selectionKey, handler);
        server.state = STATE_SERVER_AWAITING_CONNECT;
        server.host = ((InetSocketAddress)server.socketChannel.socket().getRemoteSocketAddress()).getAddress().getHostAddress();
        return server;
    }

    /**
     * Handles the incoming bytes.
     */
    synchronized void handleRead() {
        // whether the read was fully satisfied by the amount of data
        boolean satisfied = true;
        
        // continually handle incoming data
        while(satisfied) {
            // read a request
            if(state == STATE_SERVER_AWAITING_REQUEST) {
                satisfied = handleRequest();
                
            // read a response
            } else if(state == STATE_CLIENT_AWAITING_RESPONSE) {
                satisfied = handleResponse();
                
            // read a chunk
            } else if(state == STATE_READY) {
                satisfied = handleChunk();
                
            // we don't know what to read
            } else {
                throw new IllegalStateException("Cannot handle read from state " + state);
            }
        }
    }
    
    /**
     * When we can write, flush the output stream.
     */
    synchronized void handleWrite() {
        try {
            writer.flush();
        } catch(IOException e) {
            close(e);
        }
    }
    
    /**
     * When connected, prepare the higher-level connection.
     */
    synchronized void handleConnect() {
        // finish up the connect() process
        try {
            socketChannel.finishConnect();
        } catch(IOException e) {
            close(e);
            return;
        }
            
        // the connection is successful for a client
        if(state == STATE_CLIENT_AWAITING_CONNECT) {
            logger.fine("Opened connection to " + this);
            selectionKey.interestOps(SelectionKey.OP_READ);
            state = STATE_CLIENT_CONSTRUCTING_REQUEST;
            sendRequest(CTP_URI, Collections.EMPTY_MAP);
            
        // the connection is successful for a server
        } else if(state == STATE_SERVER_AWAITING_CONNECT) {
            logger.fine("Accepted connection from " + this);
            selectionKey.interestOps(SelectionKey.OP_READ);
            state = STATE_SERVER_AWAITING_REQUEST;
            
        // invalid state
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Sends the request header to the server.
     *
     * @param code an HTTP response code such as 200 (OK). See HTTP/1.1 RFC, 6.1.1
     * @param headers a Map of HTTP request headers. See HTTP/1.1 RFC, 6.2. This can
     *      be null to indicate no headers.
     */
    private void sendRequest(String uri, Map headers) {
        if(state != STATE_CLIENT_CONSTRUCTING_REQUEST) throw new IllegalStateException();
        
        try {
            // write the request line
            writer.write("POST ");
            writer.write(uri);
            writer.write(" HTTP/1.1\r\n");
            
            // write all the headers provided, plus some extras
            Map responseHeaders = new TreeMap();
            responseHeaders.putAll(headers);
            responseHeaders.put("Transfer-Encoding", "chunked");
            responseHeaders.put("Host", host);
            writeHeaders(responseHeaders);
            writer.write("\r\n");
            writer.requestFlush();
            
            // we're waiting for the response
            state = STATE_CLIENT_AWAITING_RESPONSE;
            
        } catch(IOException e) {
            close(e);
        }
    }
     
    /**
     * Handle a request by parsing the contents of the input buffer. If the input
     * buffer does not contain a complete request, this will return. If it does
     * contain a request:
     *   <li>the request will be processed
     *   <li>the buffer will be advanced, and
     *   <li>the state will be updated
     *
     * @return whether the expected data was read fully. This returns false if there
     *      was an insufficient amount of data to satisfy the request.
     */
    private boolean handleRequest() {
        if(state != STATE_SERVER_AWAITING_REQUEST) throw new IllegalStateException();
        
        try {
            // if the entire header has not loaded, load more
            if(parser.indexOf("\\r\\n\\r\\n") == -1) return false;

            // parse the status line
            parser.consume("POST( )+");
            String uri = parser.readUntil("( )+");
            parser.consume("HTTP\\/1\\.1 *");
            parser.consume("\\r\\n");
            
            // parse the headers
            Map headers = readHeaders();
            handleHeaders(headers);
            parser.consume("\\r\\n");
            
            // handle the request
            if(CTP_URI.equals(uri)) {
                state = STATE_SERVER_CONSTRUCTING_RESPONSE;
                sendResponse(RESPONSE_OK, Collections.EMPTY_MAP);
                return true;
            } else {
                return close(new Exception("Could not find URI \"" + uri + "\""));
            }

        } catch(ParseException e) {
            return close(e);
        } catch(IOException e) {
            return close(e);
        }
    }
    
    /**
     * Sends the response header to the client.
     *
     * @param code an HTTP response code such as 200 (OK). See HTTP/1.1 RFC, 6.1.1
     * @param headers a Map of HTTP response headers. See HTTP/1.1 RFC, 6.2. This can
     *      be null to indicate no headers.
     */
    private void sendResponse(int code, Map headers) {
        if(state != STATE_SERVER_CONSTRUCTING_RESPONSE) throw new IllegalStateException();
        
        try {
            // write the status line
            if(code == RESPONSE_OK) {
                writer.write("HTTP/1.1 200 OK\r\n");
            } else if(code == RESPONSE_ERROR) {
                writer.write("HTTP/1.1 500 Error\r\n");
            } else {
                throw new IllegalArgumentException("Unsupported code: " + code);
            }
    
            // write all the headers provided, plus some extras
            Map responseHeaders = new TreeMap();
            responseHeaders.putAll(headers);
            responseHeaders.put("Transfer-Encoding", "chunked");
            writeHeaders(responseHeaders);
            writer.write("\r\n");
            writer.requestFlush();
            
            // we're ready
            state = STATE_READY;
            handler.connectionReady(this);

        } catch(IOException e) {
            close(e);
        }
    }
    
    /**
     * Handle a response by parsing the contents of the input buffer. If the input
     * buffer does not contain a complete response, this will return. If it does
     * contain a response:
     *   <li>the response will be processed
     *   <li>the buffer will be advanced, and
     *   <li>the state will be updated
     *
     * @return whether the expected data was read fully. This returns false if there
     *      was an insufficient amount of data to satisfy the request.
     */
    private boolean handleResponse() {
        if(state != STATE_CLIENT_AWAITING_RESPONSE) throw new IllegalStateException();
        
        try {
            // if the entire header has not loaded, load more
            if(parser.indexOf("\\r\\n\\r\\n") == -1) return false;
            
            // parse the status line
            parser.consume("HTTP\\/1\\.1( )+");
            String codeString = parser.readUntil("( )+");
            int code = Integer.parseInt(codeString);
            String description = parser.readUntil("\\r\\n");
            
            // parse the headers
            Map headers = readHeaders();
            handleHeaders(headers);
            parser.consume("\\r\\n");
            
            // handle the response
            if(code == RESPONSE_OK) {
                state = STATE_READY;
                handler.connectionReady(this);
                return true;
            } else {
                return close(null);
            }

        } catch(ParseException e) {
            return close(e);
        } catch(NumberFormatException e) {
            return close(e);
        } catch(IOException e) {
            return close(e);
        }
    }

    /**
     * Sends the specified chunk of data immediately. This chunk should be able to
     * be cleanly concatenated with the previous and following chunks without
     * problem by the reader.
     *
     * @param data A non-empty ByteBuffer containing the bytes for this chunk. The
     *      relevant bytes start at data.position() and end at data.limit(). This
     *      buffer needs to be valid for the duration of this method call, but
     *      may be modified afterwards.
     */
    public synchronized void sendChunk(ByteBuffer data) {
        if(state != STATE_READY) throw new IllegalStateException();
        
        try {
            String chunkSizeInHex = Integer.toString(data.remaining(), 16);
            writer.write(chunkSizeInHex);
            writer.write("\r\n");
            writer.write(data);
            writer.write("\r\n");
            writer.requestFlush();
            
        } catch(IOException e) {
            close(e);
        }
    }

    /**
     * Handle a chunk by parsing the contents of the input buffer. If the input
     * buffer does not contain a complete chunk, this will return. If it does
     * contain a chunk:
     *   <li>the chunk will be processed
     *   <li>if the chunk is empty, the connection will be closed
     *
     * <p>It is possible that the source stream is not chunked, as a consequence
     * of an interfering proxy server. In this case, we arbitrarily form our own
     * chunks which are as large as possible.
     *
     * @return whether the expected data was read fully. This returns false if there
     *      was an insufficient amount of data to satisfy the request.
     */
    private boolean handleChunk() {
        try {
            if(sourceChunked) {
                // if the chunk size has not loaded, load more
                int chunkEndIndex = parser.indexOf("\\r\\n");
                if(chunkEndIndex == -1) return false;
                
                // calculate the chunk size
                String chunkSizeInHex = parser.readUntil("(\\;[^\\r\\n]*)?\\r\\n", false);
                int chunkSize = Integer.parseInt(chunkSizeInHex, 16);
                
                // if the full chunk has not loaded, load more
                int bytesRequired = chunkEndIndex + 2 + chunkSize + 2;
                if(bytesRequired > parser.bytesAvailable()) {
                    return false;
                }
            
                // load the chunk
                parser.consume("[^\\r\\n]*\\r\\n");
                ByteBuffer chunkBuffer = parser.readBytes(chunkSize);
                parser.consume("\\r\\n");
                
                // handle the chunk
                if(chunkSize != 0) {
                    handler.receiveChunk(this, chunkBuffer);
                    return true;
                } else {
                    return close();
                }

            } else {
                int bytesAvailable = parser.bytesAvailable();
                ByteBuffer chunkBuffer = parser.readBytes(bytesAvailable);
            
                // handle the simulated chunk
                if(bytesAvailable != 0) {
                    handler.receiveChunk(this, chunkBuffer);
                    return true;
                } else {
                    return false;
                }
            }
            
        } catch(NumberFormatException e) {
            return close(e);
        } catch(ParseException e) {
            return close(e);
        } catch(IOException e) {
            return close(e);
        }
    }

    /**
     * Gets this protocol as a String for debugging.
     */
    public String toString() {
        return host;
    }

    /**
     * Closes the connection to the client. As specified by the HTTP/1.1 RFC,
     * this sends a single empty chunk and closes the TCP/IP connection.
     */
    public synchronized boolean close() {
        return close(null);
    }
    
    /**
     * Close the connection to the client. 
     *
     * <p>The closing behaviour is dictated by the current state of the connection
     * and the reason for closing.
     * <li>If the reason is an IOException, the socket is closed immediately
     * <li>Otherwise a "goodbye" message is sent to notify the other party of the close
     * <li>If the state is READY, the goodbye is a single 0-byte chunk
     * <li>If the state is SERVER_AWAITING_REQUEST, the goodbye is a request error
     * <li>If the state is RECEIVED_CLOSE, no goodbye message is sent
     *
     * @return This method returns false because the connection is always in an
     *      unreadable and unwritable state after a close.
     */
    private boolean close(Exception reason) {
        // if this is already closed, we're done
        if(state == STATE_CLOSED_PERMANENTLY) return false;
        
        // close is not a result of a connection error, so say goodbye
        if(reason == null || !(reason instanceof IOException)) {
            
            // if we haven't yet responded, respond now
            if(state == STATE_SERVER_AWAITING_REQUEST) {
                state = STATE_SERVER_CONSTRUCTING_RESPONSE;
                sendResponse(RESPONSE_ERROR, Collections.EMPTY_MAP);
            }

            // if we've already responded, send an empty chunk
            if(state == STATE_READY) {
                sendChunk(ByteBuffer.wrap(new byte[0]));
            }
        }
        
        // try to flush what we have left
        try {
            writer.flush();
        } catch(IOException e) {
            // if this flush failed, there's nothing we can do
        }

        // close the socket
        handler.connectionClosed(this, reason);
        state = STATE_CLOSED_PERMANENTLY;
        try {
            socketChannel.close();
            selectionKey.cancel();
        } catch(IOException e) {
            // if this close failed, there's nothing we can do
        }

        // log the close
        if(reason != null) {
            logger.log(Level.FINE, "Closed connection to " + this + " due to " + reason, reason);
        } else {
            logger.log(Level.FINE, "Closed connection to " + this);
        }
        
        // all done
        return false;
    }
    
    /**
     * Writes the specified set of headers, one per line in standard HTTP form.
     */
    private void writeHeaders(Map headers) throws IOException {
        for(Iterator i = headers.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry mapEntry = (Map.Entry)i.next();
            writer.write(mapEntry.getKey().toString());
            writer.write(": ");
            writer.write(mapEntry.getValue().toString());
            writer.write("\r\n");
        }
    }
    
    /**
     * Reads the headers, one per line in standard HTTP form.
     */
    private Map readHeaders() throws IOException, ParseException {
        Map headers = new TreeMap();
        while(true) {
            if(parser.indexOf("\\r\\n") == 0) break;
            String key = parser.readUntil("\\:( )*");
            String value = parser.readUntil("\\r\\n");
            headers.put(key, value);
        }
        return headers;
    }

    /**
     * Handles the Map of headers. This adjusts the state of the CTPConnection
     * in response to the headers.
     *
     * <p>Currently, this recognizes the following headers:
     * <li>Transfer-Encoding, if "chunked", then this expects chunked HTTP transfer.
     */
    private void handleHeaders(Map headers) {
        if("chunked".equals(headers.get("Transfer-Encoding"))) {
            sourceChunked = true;
        } else {
            sourceChunked = false;
        }
    }
}
