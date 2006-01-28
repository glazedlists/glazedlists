/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.ctp;

import java.io.*;
import ca.odell.glazedlists.impl.io.Bufferlo;
import java.io.*;

/**
 * A test program that acts as a client to interface with a CTP server.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class CTPClient {

    /** the connection to act as a client on */
    private CTPConnection connection = null;
    
    /**
     * Creates a new CTPConnectionManager and possibly a connection.
     */
    public void start(int listenPort, String targetHost, int targetPort) throws IOException {
        
        // start the connection manager
        CTPConnectionManager manager = new CTPConnectionManager(new ClientHandlerFactory(), listenPort);
        manager.start();
        
        // connect to the target host
        if(targetHost != null) {
            manager.connect(new ClientHandler(), targetHost, targetPort);
        
            // wait for the connection
            while(true) {
                synchronized(this) {
                    if(connection != null) break;
                }
            }
            
            // read data and write it to the connection
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            while(true) {
                String dataString = in.readLine();
                synchronized(this) {
                    if(connection == null) break;
                }

                if(dataString != null) {
                    System.out.println("read a string of length " + dataString.length());
                    Bufferlo data = new Bufferlo();
                    data.write(dataString);
                    connection.sendChunk(data);
                } else {
                    connection.close();
                }
            }
        }
    }
    
    /**
     * Creates a new CTPClient and starts it.
     */
    public static void main(String[] args) {
        if(args.length == 0) {
            System.out.println("Usage: CTPClient <listenport> [<targethost> <targetport>]");
            return;
        }
        
        // parse input
        int listenPort = Integer.parseInt(args[0]);
        String targetHost = null;
        int targetPort = -1;
        if(args.length == 3) {
            targetHost = args[1];
            targetPort = Integer.parseInt(args[2]);
        }
        
        // start it up
        try {
            new CTPClient().start(listenPort, targetHost, targetPort);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Simple handlers display user text as typed.
     */
    class ClientHandler implements CTPHandler {
        public void connectionClosed(CTPConnection source, Exception reason) {
            if(reason == null) System.out.println("CLOSED: " + source);
            else System.out.println("CLOSED " + source + ", REASON=" + reason.getMessage());
            synchronized(CTPClient.this) {
                connection = null;
            }
        }
        public void connectionReady(CTPConnection source) {
            System.out.println("READY: " + source);
            synchronized(CTPClient.this) {
                connection = source;
            }
        }
        public void receiveChunk(CTPConnection source, Bufferlo data) {
            System.out.println(data.toDebugString());
            System.out.println("DATA: \"" + data.toString() + "\"");
        }
    }
    class ClientHandlerFactory implements CTPHandlerFactory {
        public CTPHandler constructHandler() {
            return new ClientHandler();
        }
    }
}
