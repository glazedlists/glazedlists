/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.impl.rbp;

import java.util.*;
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

/**
 * A peer that publishes and subscribes to lists.
 * 
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class SwingListPeer implements ActionListener {
    
    /** the shared data */
    private NetworkList localData = new NetworkList(new BasicEventList(), new SerializableByteCoder());
    private NetworkList remoteData = new NetworkList(new BasicEventList(), new SerializableByteCoder());
    
    /** fields for editing the local list */
    JTextField enterNumber = null;

    /**
     * Creates a new SwingListPeer client.
     */
    public SwingListPeer(String localHost, int localPort, String targetHost, int targetPort) {
        prepareService(localHost, localPort, targetHost, targetPort);
        constructStandalone();
    }
    
    /**
     * Constructs the browser as a standalone frame.
     */
    private void constructStandalone() {
        // create a frame with that panel
        JFrame frame = new JFrame("Swing List Peer");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(640, 480);
        frame.getContentPane().setLayout(new GridBagLayout());
        frame.getContentPane().add(constructView(), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        frame.show();
    }
    
    /**
     * Sets up all service stuff.
     */
    private void prepareService(String localHost, int localPort, String targetHost, int targetPort) {
        String localResourceName = "glazedlists://" + localHost + ":" + localPort + "/integers";
        Peer peer = new Peer(localPort);
        peer.start();
        peer.publish(localData, localResourceName);
        
        if(targetHost != null) {
            String remoteResourceName = "glazedlists://" + targetHost + ":" + targetPort + "/integers";
            peer.subscribe(remoteData, remoteResourceName, targetHost, targetPort);
        }
    }
    
    /**
     * Display a frame for browsing issues.
     */
    private JPanel constructView() {
        // create a panel with a table
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        // integer table
        EventTableModel localTableModel = new EventTableModel(localData, new IntegerTableFormat());
        EventTableModel remoteTableModel = new EventTableModel(remoteData, new IntegerTableFormat());
        JTable localJTable = new JTable(localTableModel);
        JTable remoteJTable = new JTable(remoteTableModel);
        JScrollPane localScrollPane = new JScrollPane(localJTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        JScrollPane remoteScrollPane = new JScrollPane(remoteJTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        enterNumber = new JTextField();
        enterNumber.addActionListener(this);
        

        panel.add(localScrollPane, new GridBagConstraints(0, 0, 1, 1, 0.5, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
        panel.add(remoteScrollPane, new GridBagConstraints(1, 0, 1, 1, 0.5, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 0, 5, 5), 0, 0));
        panel.add(enterNumber, new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 0, 5, 5), 0, 0));

        return panel;
    }
    
    public void actionPerformed(ActionEvent e) {
        localData.add(enterNumber.getText());
        enterNumber.setText("");
    }
    
    /**
     * When started via a main method, this creates a standalone issues browser.
     */
    public static void main(String[] args) {
        if(args.length != 2 && args.length != 4) {
            System.out.println("Usage: SwingListPeer <localhost> <localport> [<targethost> <targetport>]");
            return;
        }
        
        // load the issues and display the browser
        if(args.length == 2) {
            SwingListPeer peer = new SwingListPeer(args[0], Integer.parseInt(args[1]), null, -1);
        } else if(args.length == 4) {
            SwingListPeer peer = new SwingListPeer(args[0], Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]));
        }
    }
}
