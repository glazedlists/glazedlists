/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
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
    private static final Map lookAndFeelResourcePathMap = new HashMap();
    static {
        lookAndFeelResourcePathMap.put("Mac OS X Aqua", "aqua");
        lookAndFeelResourcePathMap.put("Metal/Steel", "metal");
        lookAndFeelResourcePathMap.put("Metal/Ocean", "ocean");
        lookAndFeelResourcePathMap.put("Classic Windows", "windows");
        lookAndFeelResourcePathMap.put("Windows XP", "windowsxp");
    }

    /** the icons to use for indicating sort order */
    private static Icon[] icons = new Icon[] { null, null, null, null, null, null, null, null, null };
    private static String[] iconFileNames = new String[] {
        "unsorted.png", "primary_sorted.png", "primary_sorted_reverse.png",
        "primary_sorted_alternate.png", "primary_sorted_alternate_reverse.png",
        "secondary_sorted.png", "secondary_sorted_reverse.png",
        "secondary_sorted_alternate.png", "secondary_sorted_alternate_reverse.png"
    };

    /**
     * load the icons at classloading time
     */
    static {
        loadIcons();
    }

    /**
     * A dummy constructor to prevent instantiation of this class
     */
    private SortIconFactory() {
        throw new UnsupportedOperationException();
    }

    /**
     * Retrieve the set of sort arrow icons that match the local system's
     * Look and Feel.
     */
    public static Icon[] getIcons() {
        return icons;
    }

    /**
     * Loads the set of icons used to be consistent with the current UIManager.
     */
    private static void loadIcons() {
        String lookAndFeelName = UIManager.getLookAndFeel().getName();
        if(lookAndFeelName.equals("Metal")) lookAndFeelName = PLAFDetector.getMetalTheme();
        else if(lookAndFeelName.equals("Windows")) lookAndFeelName = PLAFDetector.getWindowsTheme();
        String resourcePath = (String)lookAndFeelResourcePathMap.get(lookAndFeelName);
        if(resourcePath == null) resourcePath = defaultResourcePath;

        // use the classloader to look inside this jar file
        ClassLoader jarLoader = SortIconFactory.class.getClassLoader();

        // load each icon as a resource from the source .jar file
        for(int i = 0; i < icons.length; i++) {
            URL iconLocation = jarLoader.getResource(resourceRoot + "/" + resourcePath + "/" + iconFileNames[i]);
            if(iconLocation != null) icons[i] = new ImageIcon(iconLocation);
            else icons[i] = null;
        }
    }

}