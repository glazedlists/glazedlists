/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Show version information for the current .jar file.
 *
 * <p>This requires a some special attributes in the manifest to work:
 * <li><code>Built-By</code>, the person who made this build
 * <li><code>Built-At</code>, the time this build was created
 * <li><code>Contributors</code>, a comma-separated list of developers
 *
 * <p>Plus some standard manifest attributes are used:
 * <li><code>Implementation-Title</code>
 * <li><code>Implementation-URL</code>
 * <li><code>Implementation-Version</code>
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class Main {
    public static void main(String[] args) {
        final String PATH_TO_THIS_CLASS = "/ca/odell/glazedlists/impl/Main.class";

        // find the main attributes from the manifest
        final Map attributes;
        try {
            // get a path to the manifest, or die trying
            String pathToThisClass = Main.class.getResource(PATH_TO_THIS_CLASS).toString();
            int jarDelimiterCharacter = pathToThisClass.lastIndexOf("!");
            if(jarDelimiterCharacter == -1) return;
            String manifestPath = pathToThisClass.substring(0, jarDelimiterCharacter + 1) + "/META-INF/MANIFEST.MF";
            URL manifestUrl = new URL(manifestPath);

            // load the manifest and save the attributes of interest
            Manifest manifest = new Manifest(manifestUrl.openStream());
            attributes = manifest.getMainAttributes();
        } catch(IOException e) {
            return;
        }

        // the title
        String title = (String)attributes.get(Attributes.Name.IMPLEMENTATION_TITLE);
        String titleHtml = "<font size=\"6\">" + title + "</font>";

        // the url
        String urlHtml = (String)attributes.get(Attributes.Name.IMPLEMENTATION_URL);

        // the version
        String versionHtml = (String)attributes.get(Attributes.Name.IMPLEMENTATION_VERSION);

        // when it was built
        StringBuffer builtHtml = new StringBuffer();
        builtHtml.append(attributes.get(new Attributes.Name("Built-By")));
        builtHtml.append("<br>");
        builtHtml.append(attributes.get(new Attributes.Name("Built-At")));
        builtHtml.append("<br>");
        builtHtml.append("Source: ").append(attributes.get(new Attributes.Name("Source-Version")));

        // the contributors
        String contributorsHtml = (String)attributes.get(new Attributes.Name("Contributors"));
        contributorsHtml = contributorsHtml.replaceAll(",\\s*", "<br>");

        // lay it all out on a panel
        JFrame frame = new JFrame(title);
        frame.getContentPane().setLayout(new GridBagLayout());
        frame.getContentPane().add(new JLabel("<html><font color=\"#000000\">" + titleHtml + "</font></html>"),        new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER,    GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0, 0));
        frame.getContentPane().add(new JLabel("<html><font color=\"#000000\">" + urlHtml + "</font></html>"),          new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER,    GridBagConstraints.NONE, new Insets( 0, 10, 10, 10), 0, 0));
        frame.getContentPane().add(new JLabel("<html><font color=\"#999999\">Version:</font>"),                        new GridBagConstraints(0, 2, 1, 1, 0.5, 0.0, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0, 0));
        frame.getContentPane().add(new JLabel("<html><font color=\"#000000\">" + versionHtml + "</font></html>"),      new GridBagConstraints(1, 2, 1, 1, 0.5, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0, 0));
        frame.getContentPane().add(new JLabel("<html><font color=\"#999999\">Built:</font>"),                          new GridBagConstraints(0, 3, 1, 1, 0.5, 0.0, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0, 0));
        frame.getContentPane().add(new JLabel("<html><font color=\"#000000\">" + builtHtml + "</font></html>"),        new GridBagConstraints(1, 3, 1, 1, 0.5, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0, 0));
        frame.getContentPane().add(new JLabel("<html><font color=\"#999999\">Contributors:</font>"),                   new GridBagConstraints(0, 4, 1, 1, 0.5, 0.0, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0, 0));
        frame.getContentPane().add(new JLabel("<html><font color=\"#000000\">" + contributorsHtml + "</font></html>"), new GridBagConstraints(1, 4, 1, 1, 0.5, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0, 0));

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}