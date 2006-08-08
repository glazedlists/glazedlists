/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.swing;

import javax.swing.*;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.PopupMenuEvent;
import java.awt.*;

/**
 * Fix a problem where the JComboBox's popup obscures its editor in the Mac OS X
 * Aqua look and feel.
 *
 * <p>Installing this fix will resolve the problem for Aqua without having
 * side-effects for other look-and-feels. It also supports dynamically changed
 * look and feels.
 *
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=332">bug 332</a>
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public final class ComboBoxPopupLocationFix {

    /** the components being fixed */
    private final JComboBox comboBox;
    private final JPopupMenu popupMenu;

    /** the listener provides callbacks as necessary */
    private final Listener listener = new Listener();

    /**
     * Private constructor so users use the more action-oriented
     * {@link #install} method.
     */
    private ComboBoxPopupLocationFix(JComboBox comboBox) {
        this.comboBox = comboBox;
        this.popupMenu = (JPopupMenu)comboBox.getUI().getAccessibleChild(comboBox, 0);

        popupMenu.addPopupMenuListener(listener);
    }

    /**
     * Install the fix for the specified combo box.
     */
    public static ComboBoxPopupLocationFix install(JComboBox comboBox) {
        if(comboBox == null) throw new IllegalArgumentException();
        return new ComboBoxPopupLocationFix(comboBox);
    }

    /**
     * Uninstall the fix. Usually this is unnecessary since letting the combo
     * box go out of scope is sufficient.
     */
    public void uninstall() {
        popupMenu.removePopupMenuListener(listener);
    }

    /**
     * Reposition the popup immediately before it is shown.
     */
    private class Listener implements PopupMenuListener {
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            final JComponent popupComponent = (JComponent) e.getSource();
            fixPopupLocation(popupComponent);
        }
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            // do nothing
        }
        public void popupMenuCanceled(PopupMenuEvent e) {
            // do nothing
        }
    }

    /**
     * Do the adjustment on the specified popupComponent immediately before
     * it is displayed.
     */
    private void fixPopupLocation(JComponent popupComponent) {
        // we only need to fix Apple's aqua look and feel
        if(popupComponent.getClass().getName().indexOf("apple.laf") != 0) {
            return;
        }

        // put the popup right under the combo box so it looks like a
        // normal Aqua combo box
        Point comboLocationOnScreen = comboBox.getLocationOnScreen();
        int comboHeight = comboBox.getHeight();
        int popupY = comboLocationOnScreen.y + comboHeight;

        // ...unless the popup overflows the screen, in which case we put it
        // above the combobox
        Rectangle screenBounds = new ScreenGeometry(comboBox).getScreenBounds();
        int popupHeight = popupComponent.getPreferredSize().height;
        if(comboLocationOnScreen.y + comboHeight + popupHeight > screenBounds.x + screenBounds.height) {
            popupY = comboLocationOnScreen.y - popupHeight;
        }

        popupComponent.setLocation(comboLocationOnScreen.x, popupY);
    }
}
