/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl;

// to match icons to the current look and feel
import javax.swing.UIManager;
// for looking up icon files in jars
import java.net.URL;
// for keeping a map of resources
import java.util.*;
// to provide icons
import javax.swing.ImageIcon;
import javax.swing.Icon;

/**
 * A Factory to provide access to sort-arrow icons for table
 * headers which match the host Look and Feel.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public final class SortIconFactory {

    /** a map of look and feels to resource paths for icons */
    private static final String resourceRoot = "resources";
    private static final String defaultResourcePath = "aqua";
    private static final Map<String,String> lookAndFeelResourcePathMap = new HashMap<String,String>();
    static {
        lookAndFeelResourcePathMap.put("Mac OS X Aqua", "aqua");
        lookAndFeelResourcePathMap.put("Metal/Steel", "metal");
        lookAndFeelResourcePathMap.put("Metal/Ocean", "ocean");
        lookAndFeelResourcePathMap.put("Classic Windows", "windows");
        lookAndFeelResourcePathMap.put("Windows XP", "windowsxp");
        lookAndFeelResourcePathMap.put("WinLAF", "windowsxp");
    }

    /** the icons to use for indicating sort order */
    private static Icon[] defaultIcons = null;
    private static String[] iconFileNames = new String[] {
        "unsorted.png", "primary_sorted.png", "primary_sorted_reverse.png",
        "primary_sorted_alternate.png", "primary_sorted_alternate_reverse.png",
        "secondary_sorted.png", "secondary_sorted_reverse.png",
        "secondary_sorted_alternate.png", "secondary_sorted_alternate_reverse.png"
    };

    /**
     * A dummy constructor to prevent instantiation of this class
     */
    private SortIconFactory() {
        throw new UnsupportedOperationException();
    }

    /**
     * Loads the set of icons that match the current UIManager.
     */
    public static Icon[] loadIcons() {
        // if we've already loaded the default icons, return them
        if(defaultIcons != null) return defaultIcons;

        // detect the current look & feel
        String lookAndFeelName = UIManager.getLookAndFeel().getName();
        if(lookAndFeelName.equals("Metal")) lookAndFeelName = PLAFDetector.getMetalTheme();
        else if(lookAndFeelName.equals("Windows")) lookAndFeelName = PLAFDetector.getWindowsTheme();
        String resourcePath = lookAndFeelResourcePathMap.get(lookAndFeelName);
        if(resourcePath == null) resourcePath = defaultResourcePath;

        // save and return the default icons
        defaultIcons = loadIcons(resourceRoot + "/" + resourcePath);
        return defaultIcons;
    }

    /**
     * Loads the set of icons from the specified path.
     */
    public static Icon[] loadIcons(String path) {
        // use the classloader to look inside this jar file
        ClassLoader jarLoader = SortIconFactory.class.getClassLoader();

        // load each icon as a resource from the source .jar file
        Icon[] pathIcons = new Icon[iconFileNames.length];
        for(int i = 0; i < pathIcons.length; i++) {
            URL iconLocation = jarLoader.getResource(path + "/" + iconFileNames[i]);
            if(iconLocation != null)
                pathIcons[i] = new ImageIcon(iconLocation);
        }

        // return the loaded result
        return pathIcons;
    }
}
