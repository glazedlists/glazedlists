package com.publicobject.amazonbrowser.swing;

import com.publicobject.amazonbrowser.Item;
import com.publicobject.amazonbrowser.ItemLoader;
import com.publicobject.misc.swing.IndeterminateToggler;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.BasicEventList;

public class AmazonBrowser implements Runnable {

    public static final Icon THROBBER_ACTIVE = loadIcon("resources/throbber-active.gif");
    public static final Icon THROBBER_STATIC = loadIcon("resources/throbber-static.gif");

    /** an event list to host the items */
    private EventList<Item> itemEventList = new BasicEventList<Item>();

    /** monitor loading the items */
    private JLabel throbber = null;

    /** loads items as requested */
    private ItemLoader itemLoader;

    /** the application window */
    private JFrame frame;

    /**
     * Loads the AmazonBrowser as standalone application.
     */
    public void run() {
        constructStandalone();

        // create the issue loader and start loading issues
        itemLoader = new ItemLoader(itemEventList, new IndeterminateToggler(throbber, THROBBER_ACTIVE, THROBBER_STATIC));
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
        frame = new JFrame("Issues");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(constructView(), BorderLayout.CENTER);
        frame.setVisible(true);
    }

    /**
     * Display a frame for browsing issues.
     */
    private JPanel constructView() {

        // throbber
        throbber = new JLabel(THROBBER_STATIC);
        throbber.setHorizontalAlignment(SwingConstants.RIGHT);

        return new JPanel();
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
}