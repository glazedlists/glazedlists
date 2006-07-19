package com.publicobject.amazonbrowser.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import com.publicobject.amazonbrowser.Item;
import com.publicobject.amazonbrowser.ItemLoader;
import com.publicobject.misc.swing.GradientPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.net.URL;

public class AmazonBrowser implements Runnable {

    /** application appearance */
    public static final Color AMAZON_SEARCH_LIGHT_BLUE = new Color(171, 208, 226);
    public static final Color AMAZON_SEARCH_DARK_BLUE = new Color(54, 127, 168);

    /** an event list to host the items */
    private EventList<Item> itemEventList = new BasicEventList<Item>();

    /** loads items as requested */
    private ItemLoader itemLoader;

    /** the progress bar that tracks the item loading progress */
    private JProgressBar progressBar;

    /** the application window */
    private JFrame frame;

    /**
     * Loads the AmazonBrowser as standalone application.
     */
    public void run() {
        constructStandalone();

        // create the issue loader and start loading issues
        itemLoader = new ItemLoader(itemEventList, progressBar);
        itemLoader.start();
    }

    /**
     * Load the specified icon from the pathname on the classpath.
     */
    private static ImageIcon loadIcon(String pathname) {
        ClassLoader jarLoader = AmazonBrowser.class.getClassLoader();
        URL url = jarLoader.getResource(pathname);
        if (url == null) return null;
        return new ImageIcon(url);
    }

    /**
     * Constructs the browser as a standalone frame.
     */
    private void constructStandalone() {
        frame = new JFrame("Amazon Browser");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(constructView(), BorderLayout.CENTER);
        frame.setVisible(true);
    }

    /**
     * Construct a frame for search and browsing items from Amazon.
     */
    private JPanel constructView() {
        final JLabel searchFieldLabel = new JLabel("Search");
        searchFieldLabel.setFont(new Font("Verdana", Font.BOLD, 14));
        searchFieldLabel.setForeground(Color.WHITE);
        final JTextField searchField = new JTextField(10);
        searchField.addActionListener(new StartNewSearchActionListener());

        progressBar = new JProgressBar();

        final JPanel searchPanel = new GradientPanel(AMAZON_SEARCH_LIGHT_BLUE, AMAZON_SEARCH_DARK_BLUE, true);
        searchPanel.setLayout(new GridBagLayout());
        searchPanel.add(searchFieldLabel,             new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 10, 0, 3), 0, 0));
        searchPanel.add(searchField,                  new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 10), 0, 0));
        searchPanel.add(progressBar,                  new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 10), 0, 0));
        searchPanel.add(Box.createVerticalStrut(65),  new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(BorderLayout.NORTH, searchPanel);
        return panel;
    }

    /**
     * When started via a main method, this creates a standalone issues browser.
     */
    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new AmazonBrowserStarter());
    }

    /**
     * This Runnable contains the logic to start the IssuesBrowser application.
     * It is guaranteed to be executed on the EventDispatch Thread.
     */
    private static class AmazonBrowserStarter implements Runnable {
        public void run() {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // do nothing - fall back to default look and feel
            }

            final AmazonBrowser browser = new AmazonBrowser();
            browser.run();
        }
    }

    private class StartNewSearchActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            final JTextField searchField = (JTextField) e.getSource();
            itemLoader.setKeywords(searchField.getText());
        }
    }
}