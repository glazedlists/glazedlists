/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.misc.swing;

import ca.odell.glazedlists.impl.PLAFDetector;

import javax.swing.*;
import java.awt.*;

/**
 * Tweak the appearance of a JTable for the current look and feel.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class LookAndFeelTweaks {

    public static void tweakTable(JTable table) {
        String lookAndFeelName = UIManager.getLookAndFeel().getName();

        if(lookAndFeelName.equals("Windows")) {
            String theme = PLAFDetector.getWindowsTheme();

            // on Vista, there's no horizontal grid lines
            if("Windows Vista".equals(theme)) {
                table.setShowHorizontalLines(false);
                table.setShowVerticalLines(true);
                table.setGridColor(new Color(237, 237, 237));
                table.setBackground(Color.WHITE);
            }
        }
    }
}
