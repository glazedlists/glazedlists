/**
 * Glazed Lists
 * http://opensource.odellengineeringltd.com/glazedlists/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.util;

// for being a window adapter
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * A simple window adapter that exits the Java process when the host
 * window is closed. Add this window adapter to the main window of
 * applications so that the Java process exits when that Window
 * is closed.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ExitOnCloseHandler extends WindowAdapter {
    
    /**
     * Upon a window closing, exit the JVM.
     */
    public void windowClosing(WindowEvent e) {
        System.exit(0);
    }

    /**
     * Adds a new window close handler to the supplied frame, so that
     * Java exits when the frame is closed.
     */
    public static void addToFrame(JFrame window) {
        window.addWindowListener(new ExitOnCloseHandler());
    }
}
