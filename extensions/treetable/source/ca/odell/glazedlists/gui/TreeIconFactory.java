/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.gui;

import javax.swing.*;
import java.util.Map;
import java.util.HashMap;
import java.net.URL;
import java.io.Serializable;
import java.awt.*;

/**
 * A Factory to provide access to tree icons for hierarchy columns in
 * treetables which match the host Look and Feel.
 *
 * @author James Lemieux
 */
public final class TreeIconFactory {

    /** a map of look and feels to resource paths for icons */
    private static final String resourceRoot = "resources";
    private static final String defaultResourcePath = "aqua";
    private static final Map<String,String> lookAndFeelResourcePathMap = new HashMap<String,String>();
    static {
        TreeIconFactory.lookAndFeelResourcePathMap.put("Mac OS X Aqua", "aqua");
        TreeIconFactory.lookAndFeelResourcePathMap.put("Metal/Steel", "metal");
        TreeIconFactory.lookAndFeelResourcePathMap.put("Metal/Ocean", "ocean");
        TreeIconFactory.lookAndFeelResourcePathMap.put("Classic Windows", "windows");
        TreeIconFactory.lookAndFeelResourcePathMap.put("Windows XP", "windowsxp");
        TreeIconFactory.lookAndFeelResourcePathMap.put("WinLAF", "windowsxp");
    }

    /** the icons to use for indicating sort order */
    private static Icon[] defaultIcons = null;
    private static String[] iconFileNames = {
        "expanded.png", "collapsed.png"
    };

    /**
     * A dummy constructor to prevent instantiation of this class
     */
    private TreeIconFactory() {
        throw new UnsupportedOperationException();
    }

    /**
     * Loads the set of icons that match the current UIManager.
     */
    public static Icon[] loadIcons() {
        // if we've already loaded the default icons, return them
        if(TreeIconFactory.defaultIcons != null) return TreeIconFactory.defaultIcons;

        // todo enable reading the icons from the classpath when we start loading images (and no longer user hand-coded Icons)
//        // detect the current look & feel
//        String lookAndFeelName = UIManager.getLookAndFeel().getName();
//        if(lookAndFeelName.equals("Metal")) lookAndFeelName = PLAFDetector.getMetalTheme();
//        else if(lookAndFeelName.equals("Windows")) lookAndFeelName = PLAFDetector.getWindowsTheme();
//        String resourcePath = TreeIconFactory.lookAndFeelResourcePathMap.get(lookAndFeelName);
//        if(resourcePath == null) resourcePath = TreeIconFactory.defaultResourcePath;
//
//        // save and return the default icons
//        TreeIconFactory.defaultIcons = TreeIconFactory.loadIcons(TreeIconFactory.resourceRoot + "/" + resourcePath);
        TreeIconFactory.defaultIcons = new Icon[] {new ExpandedTreeIcon(), new CollapsedTreeIcon()};
        return TreeIconFactory.defaultIcons;
    }

    /**
     * Loads the set of icons from the specified path.
     */
    public static Icon[] loadIcons(String path) {
        // use the classloader to look inside this jar file
        ClassLoader jarLoader = TreeIconFactory.class.getClassLoader();

        // load each icon as a resource from the source .jar file
        Icon[] pathIcons = new Icon[TreeIconFactory.iconFileNames.length];
        for(int i = 0; i < pathIcons.length; i++) {
            URL iconLocation = jarLoader.getResource(path + "/" + TreeIconFactory.iconFileNames[i]);
            if(iconLocation != null)
                pathIcons[i] = new ImageIcon(iconLocation);
        }

        // return the loaded result
        return pathIcons;
    }


    // todo remove these when we start using pngs
    /**
     * The minus sign button icon used in trees
     */
    private static class ExpandedTreeIcon implements Icon, Serializable {

        protected static final int SIZE      = 9;
        protected static final int HALF_SIZE = 4;

        public void paintIcon(Component c, Graphics g, int x, int y) {
            Color backgroundColor = c.getBackground();

            g.setColor(backgroundColor != null ? backgroundColor : Color.WHITE);
            g.fillRect(x, y, SIZE - 1, SIZE - 1);
            g.setColor(Color.GRAY);
            g.drawRect(x, y, SIZE - 1, SIZE - 1);
            g.setColor(Color.BLACK);
            g.drawLine(x + 2, y + HALF_SIZE, x + (SIZE - 3), y + HALF_SIZE);
        }

        public int getIconWidth()  { return SIZE; }
        public int getIconHeight() { return SIZE; }
    }


    /**
     * The plus sign button icon used in trees.
     */
    private static class CollapsedTreeIcon extends ExpandedTreeIcon {
        public void paintIcon(Component c, Graphics g, int x, int y) {
            super.paintIcon(c, g, x, y);
            g.drawLine(x + HALF_SIZE, y + 2, x + HALF_SIZE, y + (SIZE - 3));
        }
    }
}