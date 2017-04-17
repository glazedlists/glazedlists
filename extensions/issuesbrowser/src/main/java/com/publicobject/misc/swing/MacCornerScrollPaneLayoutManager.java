/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.misc.swing;

import javax.swing.*;
import java.awt.*;

/**
 * A scrollpane layout that handles the resize box in the bottom right corner.
 *
 * @see <a href="http://publicobject.com/2005/12/fix-your-jscrollpanes-resizable-corner.html">Fix
 *      your JScrollPane's resizable corner under Aqua</a>
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class MacCornerScrollPaneLayoutManager extends ScrollPaneLayout {
    private static final int CORNER_HEIGHT = 14;
    public static void install(JScrollPane scrollPane) {
        if(System.getProperty("os.name").startsWith("Mac")) {
            scrollPane.setLayout(new MacCornerScrollPaneLayoutManager());
        }
    }
    @Override
    public void layoutContainer(Container container) {
        super.layoutContainer(container);
        if(!hsb.isVisible() && vsb != null) {
            Rectangle bounds = new Rectangle(vsb.getBounds());
            bounds.height = Math.max(0, bounds.height - CORNER_HEIGHT);
            vsb.setBounds(bounds);
        }
    }
}
