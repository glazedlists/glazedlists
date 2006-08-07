/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.swing;

import javax.swing.*;
import java.awt.*;

/**
 * Figure out the dimensions of our screen.
 *
 * <p>This code is inspired by similar in
 * <code>JPopupMenu.adjustPopupLocationToFitScreen()</code>.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ScreenGeometry {

    final GraphicsConfiguration graphicsConfiguration;
    final boolean aqua;

    public ScreenGeometry(JComponent component) {
        this.aqua = UIManager.getLookAndFeel().getName().indexOf("Aqua") != -1;
        this.graphicsConfiguration = graphicsConfigurationForComponent(component);
    }

    /**
     * Get the best graphics configuration for the specified point and component.
     */
    private GraphicsConfiguration graphicsConfigurationForComponent(Component component) {
        Point point = component.getLocationOnScreen();

        // try to find the graphics configuration for our point of interest
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gd = ge.getScreenDevices();
        for(int i = 0; i < gd.length; i++) {
            if(gd[i].getType() != GraphicsDevice.TYPE_RASTER_SCREEN) continue;
            GraphicsConfiguration defaultGraphicsConfiguration = gd[i].getDefaultConfiguration();
            if(!defaultGraphicsConfiguration.getBounds().contains(point)) continue;
            return defaultGraphicsConfiguration;
        }

        // we couldn't find a graphics configuration, use the component's
        return component.getGraphicsConfiguration();
    }

    /**
     * Get the bounds of where we can put a popup.
     */
    public Rectangle getScreenBounds() {
        Rectangle screenSize = getScreenSize();
        Insets screenInsets = getScreenInsets();

        return new Rectangle(
            screenSize.x + screenInsets.left,
            screenSize.y + screenInsets.top,
            screenSize.width - screenInsets.left - screenInsets.right,
            screenSize.height - screenInsets.top - screenInsets.bottom
        );
    }

    /**
     * Get the bounds of the screen currently displaying the component.
     */
    public Rectangle getScreenSize() {
        // get the screen bounds and insets via the graphics configuration
        if(graphicsConfiguration != null) {
            return graphicsConfiguration.getBounds();
        }

        // just use the toolkit bounds, it's less awesome but sufficient
        return new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
    }

    /**
     * Fetch the screen insets, the off limits areas around the screen such
     * as menu bar, dock or start bar.
     */
    public Insets getScreenInsets() {
        Insets screenInsets;
        if(graphicsConfiguration != null) {
            screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(graphicsConfiguration);
        } else {
            screenInsets = new Insets(0, 0, 0, 0);
        }

        // tweak the insets for aqua, they're reported incorrectly there
        if(aqua) {
            int aquaBottomInsets = 21; // unreported insets, shown in screenshot, https://glazedlists.dev.java.net/issues/show_bug.cgi?id=332
            int aquaTopInsets = 22; // for Apple menu bar, found via debugger

            screenInsets.bottom = Math.max(screenInsets.bottom, aquaBottomInsets);
            screenInsets.top = Math.max(screenInsets.top, aquaTopInsets);
        }

        return screenInsets;
    }
}
