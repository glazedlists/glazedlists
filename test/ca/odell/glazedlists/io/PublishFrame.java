/*             Glazed Lists  http://publicobject.com/glazedlists/             */                        
/*        Copyright 2003-2005 publicobject.com, O'Dell Engineering Ltd.       */
package ca.odell.glazedlists.io;

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

/**
 * A frame that shows a published list.
 * 
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class PublishFrame implements ActionListener, NetworkListStatusListener {
    
    /** the subscribed data */
    private NetworkList data = null;
    
    /** the network connection */
    private ListPeer peer = null;
    
    /** the resource name of the subscribed data */
    private String resourceName = null;
    
    /** the button to toggle connect */
    private JButton connectButton;
    
    /** add to this list */
    private JTextField textEnter;
    
    /**
     * Creates a new PublishFrame client.
     */
    public PublishFrame(ListPeer peer, String host, int port, String path) throws IOException {
        this.peer = peer;
        
        // publish
        data = peer.publish(new BasicEventList(), path, GlazedListsIO.serializableByteCoder());
        
        // build user interface
        constructStandalone();
        
        // listen for status changes
        data.addStatusListener(this);
    }
    
    /**
     * Constructs the browser as a standalone frame.
     */
    private void constructStandalone() {
        // create a frame with that panel
        JFrame frame = new JFrame("Publish: " + resourceName);
        frame.setSize(200, 300);
        frame.getContentPane().setLayout(new GridBagLayout());
        frame.getContentPane().add(constructView(), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        frame.show();
    }
    
    /**
     * Display a frame for browsing issues.
     */
    private JPanel constructView() {
        // build the table
        EventTableModel tableModel = new EventTableModel(data, new IntegerTableFormat());
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // build the text entry
        textEnter = new JTextField();
        textEnter.addActionListener(this);
        
        // build the buttons
        connectButton = new JButton(data.isConnected() ? "Disconnect" : "Connect");
        connectButton.addActionListener(this);

        // create a panel with a table
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.add(scrollPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
        panel.add(textEnter, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 5, 5, 5), 0, 0));
        panel.add(connectButton, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 5, 5, 5), 0, 0));

        return panel;
    }

    /**
     * Called each time a resource becomes connected.
     */
    public void connected(NetworkList list) {
        connectButton.setText("Disconnect");
    }
    
    /**
     * Called each time a resource's disconnected status changes. This method may
     * be called for each attempt it makes to reconnect to the network.
     */
    public void disconnected(NetworkList list, Exception reason) {
        connectButton.setText("Connect");
    }
    
    /**
     * When a button is pressed. 
     */
    public void actionPerformed(ActionEvent e) {
        peer.print();
        
        if(e.getSource() == connectButton) {
            if(data.isConnected()) {
                data.disconnect();
            } else {
                data.connect();
            }
        } else if(e.getSource() == textEnter) {
            data.add(textEnter.getText());
            textEnter.setText("");
        }
    }
}
