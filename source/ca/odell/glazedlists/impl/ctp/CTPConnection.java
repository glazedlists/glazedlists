/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.ctp;

// NIO is used for CTP
import ca.odell.glazedlists.impl.io.Bufferlo;
import ca.odell.glazedlists.impl.nio.NIOAttachment;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.text.ParseException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

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
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public final class CTPConnection implements NIOAttachment {
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
    static final int STATE_SERVER_AWAITING_CONNECT = 0;
    static final int STATE_CLIENT_AWAITING_CONNECT = 1;
    static final int STATE_CLIENT_CONSTRUCTING_REQUEST = 2;
    static final int STATE_SERVER_AWAITING_REQUEST = 3;
    static final int STATE_SERVER_CONSTRUCTING_RESPONSE = 4;
    static final int STATE_CLIENT_AWAITING_RESPONSE = 5;
    static final int STATE_READY = 6;
    static final int STATE_RECEIVED_CLOSE = 7;
    static final int STATE_CLOSED_PERMANENTLY = 8;
    
    /** standard HTTP response headers, see HTTP/1.1 RFC, 6.1.1 */
    static final int RESPONSE_OK = 200;
    static final int RESPONSE_ERROR = 500;

    /** the current state of this protocol */
    int state = -1;
    
    /** the key to this protocol's channel */
    SelectionKey selectionKey = null;
    
    /** the channel where communication occurs */
    SocketChannel socketChannel = null;
    
    /** parse the input channel */
    private Bufferlo parser;

    /** write the output channel */
    Bufferlo writer;

    /** the handler to delegate data interpretation to */
    CTPHandler handler;
    
    /** the manager that owns this connection */
    CTPConnectionManager manager;
    
    /** the remote host */
    String remoteHost = "remotehost";
    
    /** the local host, as known by the client */
    String localHost = "localhost";
    
    /** the only URI allowed by CTPConnection */
    static final String CTP_URI = "/glazedlists";
    
    /** if our source is not chunked, we have to break up chunks arbitrarily */
    boolean sourceChunked = false;
    
    /**
     * Creates a new CTPConnection.
     *
     * @param selectionKey the connection managed by this higher-level protocol.
     */
    private CTPConnection(SelectionKey selectionKey, CTPHandler handler, CTPConnectionManager manager) {
        if(selectionKey == null) throw new IllegalArgumentException();
        
        this.selectionKey = selectionKey;
        this.handler = handler;
        this.manager = manager;
        this.socketChannel = (SocketChannel)selectionKey.channel();
        this.parser = new Bufferlo();
        this.writer = new Bufferlo();
    }
    
    /**
     * Create a new CTPConnection for use as a client.
     */
    static CTPConnection client(String host, SelectionKey selectionKey, CTPHandler handler, CTPConnectionManager manager) {
        CTPConnection client = new CTPConnection(selectionKey, handler, manager);
        client.state = STATE_CLIENT_AWAITING_CONNECT;
        client.remoteHost = host;
        return client;
    }
    
    /**
     * Create a new CTPConnection for use as a server.
     */
    static CTPConnection server(SelectionKey selectionKey, CTPHandler handler, CTPConnectionManager manager) {
        CTPConnection server = new CTPConnection(selectionKey, handler, manager);
        server.state = STATE_SERVER_AWAITING_CONNECT;
        server.remoteHost = ((InetSocketAddress)server.socketChannel.socket().getRemoteSocketAddress()).getAddress().getHostAddress();
        return server;
    }
    
    /**
     * Gets the hostname that the local party is referred to by the remote party.
     */
    public String getLocalHost() {
        return localHost;
    }
    public int getLocalPort() {
        return socketChannel.socket().getLocalPort();
    }
    /**
     * Gets the hostname that the local party uses to refer to the remote party.
     */
    public String getRemoteHost() {
        return remoteHost;
    }
    public int getRemotePort() {
        return socketChannel.socket().getPort();
    }

    /**
     * Handles the incoming bytes.
     */
    public void handleRead() {
        // read at least a byte of data
        try {
            int bytesIn = parser.readFromChannel(socketChannel);
            if(bytesIn < 0) throw new EOFException("End of stream");
        } catch(IOException e) {
            close(e);
        }
        
        // continually handle incoming data
        boolean satisfied = true;
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
    public void handleWrite() {
        // do the write
        try {
            writer.writeToChannel(socketChannel, selectionKey);
        } catch(IOException e) {
            close(e);
        }
    }
    
    /**
     * When connected, prepare the higher-level connection.
     */
    public void handleConnect() {
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
     * @param uri the web address of the target page
     * @param headers a Map of HTTP request headers. See HTTP/1.1 RFC, 6.2. This can
     *      be null to indicate no headers.
     */
    void sendRequest(String uri, Map headers) {
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
            responseHeaders.put("Host", remoteHost);
            writeHeaders(responseHeaders);
            writer.write("\r\n");
            writer.writeToChannel(socketChannel, selectionKey);
            
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
                close(new Exception("Could not find URI \"" + uri + "\""));
                return false;
            }

        } catch(ParseException e) {
            close(new IOException("Failed to decode HTTP request, " + e.getMessage()));
            return false;
        } catch(IOException e) {
            close(e);
            return false;
        }
    }
    
    /**
     * Sends the response header to the client.
     *
     * @param code an HTTP response code such as 200 (OK). See HTTP/1.1 RFC, 6.1.1
     * @param headers a Map of HTTP response headers. See HTTP/1.1 RFC, 6.2. This can
     *      be null to indicate no headers.
     */
    void sendResponse(int code, Map headers) {
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
            writer.writeToChannel(socketChannel, selectionKey);
            
            // we're ready
            logger.info("Accepted connection from " + this);
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
                logger.info("Established connection to " + this);
                state = STATE_READY;
                handler.connectionReady(this);
                return true;
            } else {
                close(null);
                return false;
            }

        } catch(ParseException e) {
            close(new IOException("Failed to decode HTTP request, " + e.getMessage()));
            return false;
        } catch(NumberFormatException e) {
            close(new IOException("Failed to decode HTTP request, " + e.getMessage()));
            return false;
        } catch(IOException e) {
            close(e);
            return false;
        }
    }

    /**
     * Sends the specified chunk of data immediately. This chunk should be able to
     * be cleanly concatenated with the previous and following chunks without
     * problem by the reader.
     *
     * @param data A non-empty list of non-empty ByteBuffers containing the bytes for this chunk. The
     *      relevant bytes start at data.position() and end at data.limit(). This
     *      buffer needs to be valid for the duration of this method call, but
     *      is safe to modify afterwards.
     */
    public void sendChunk(Bufferlo data) {
        manager.getNIODaemon().invokeAndWait(new SendChunk(this, data));
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
                if(parser.length() < bytesRequired) {
                    return false;
                }
            
                // load the chunk
                parser.consume("[^\\r\\n]*\\r\\n");
                Bufferlo chunkData = parser.consume(chunkSize);
                parser.consume("\\r\\n");
                
                // handle the chunk
                if(chunkData.length() > 0) {
                    handler.receiveChunk(this, chunkData);
                    return true;
                } else {
                    close();
                    return false;
                }

            } else {
                Bufferlo chunkData = parser.consume(parser.length());
            
                // handle the simulated chunk
                if(chunkData.length() > 0) {
                    handler.receiveChunk(this, chunkData);
                    return true;
                } else {
                    return false;
                }
            }
            
        } catch(NumberFormatException e) {
            close(new IOException("Failed to decode HTTP request, " + e.getMessage()));
            return false;
        } catch(ParseException e) {
            close(new IOException("Failed to decode HTTP request, " + e.getMessage()));
            return false;
        }
    }

    /**
     * Gets this protocol as a String for debugging.
     */
    @Override
    public String toString() {
        return localHost + ":" + getLocalPort() + "<->" + remoteHost + ":" + getRemotePort();
    }

    /**
     * Closes the connection to the client. As specified by the HTTP/1.1 RFC,
     * this sends a single empty chunk and closes the TCP/IP connection.
     */
    public synchronized void close() {
        //return close(null);
        close(null);
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
     */
    public void close(Exception reason) {
        manager.getNIODaemon().invokeLater(new CloseConnection(this, reason));
        //return false;
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
        // whether this is a chunked conversation
        if("chunked".equals(headers.get("Transfer-Encoding"))) {
            sourceChunked = true;
        } else {
            sourceChunked = false;
        }
        
        // the name the remote host uses to refer to this host
        String headerLocalHost = (String)headers.get("Host");
        if(headerLocalHost != null) localHost = headerLocalHost;
    }
}