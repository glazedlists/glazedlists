/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.net;

import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import java.applet.*;
import java.awt.event.*;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
// glazed lists
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.swing.*;
import ca.odell.glazedlists.io.*;
import ca.odell.glazedlists.impl.io.*;
import ca.odell.glazedlists.util.ByteCoderFactory;

/**
 * A peer that publishes and subscribes to lists.
 * 
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class SwingListPeer {
    
    /**
     * When started via a main method, this creates a standalone issues browser.
     */
    public static void main(String[] args) throws IOException {
        if(args.length != 2 && args.length != 4) {
            System.out.println("Usage: SwingListPeer <localhost> <localport> [<targethost> <targetport>]");
            return;
        }
        
        String localHost = args[0];
        int localPort = Integer.parseInt(args[1]);
        
        ListPeer peer = new ListPeer(localPort);
        peer.start();
        
        // start the publisher
        if(args.length >= 2) {
            new PublishFrame(peer, "/slp", localHost, localPort);
        }
        
        // start the subscriber
        if(args.length >= 4) {
            String targetHost = args[2];
            int targetPort = Integer.parseInt(args[3]);
            new SubscribeFrame(peer, "/slp", targetHost, targetPort);
        }
    }
}
